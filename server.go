package main

import (
	"errors"
	"flag"
	"fmt"
	"github.com/fatih/structs"
	"github.com/gorilla/sessions"
	"net/http"
	"os"
	"path"
	"strings"
	"time"
)

/*
global variables for directory information,
Tasks channel for program execution (more info in cmdLine.go),
store for cookie and session handling
TODO: setup some form of secret cookie handling
*/
var (
	empty       Empty
	waitOn      = 1
	UserDir     = "users/"
	templateDir = "templates/"
	progDir     = "executables/"
	Tasks       = make(chan []string, 64)
	Signal      = make(chan os.Signal, 1)
	store       = sessions.NewCookieStore([]byte("something-secret-or-not"))
	//store       *sessions.CookieStore
)

func main() {
	r := NewRouter()
	//store = sessions.NewCookieStore([]byte(RandomString(64)))
	store.Options = &sessions.Options{
		Path: "/",
	}

	// start routine to handle program execution
	go RunCmd()

	// serve static files for stuff like css, js, imgs from public folder
	r.PathPrefix("/").Handler(http.FileServer(http.Dir("./public/")))
	http.Handle("/", r)

	// pass opt flag -port=# to specify an operating port
	flgs := flag.String("port", "8080", "a string")
	//flgs := flag.String("port", "3000", "a string")
	flag.Parse()
	fmt.Println("running on port:", *flgs)
	port := ":" + *flgs

	// signal handling
	//signal.Notify(Signal, os.Interrupt)
	// db initilization
	DBInit()
	FilesInit()

	// start server
	http.ListenAndServe(port, r)
}

/*
Program to handle waiting on any number of processes, feels kind of janky
currently waiting on program execution
@TODO: maybe later, create a way to handle multiple exiting w/ a channel
*/
func Close(sig os.Signal) {
	waitOn--
	// if nothing else to wait on, exit
	if waitOn == 0 {
		os.Exit(0)
	}
	fmt.Println("not closing, waiting for process")
	// put signal back for next process to close
	Signal <- sig
	return
}

func CheckIn(person *User) (err error) {
	var args []interface{}
	args = append(args, time.Now().Unix())
	args = append(args, person.Name)
	err = DBWrite(UpdateUserTime, args)
	return
}

/*
get and fill a user struct from the db,
if no user is returned, return an error
*/
func GetUser(id string) (person *User, err error) {
	person = &User{}
	var args []interface{}
	args = append(args, id)
	err = DBReadRow(QueryUser, args, person)
	// if err or no matching results
	if err != nil {
		fmt.Println("error geting user:", err.Error())
		return nil, err
	}
	if person.Folder == "" {
		return nil, errors.New("No User")
	}
	return
}

/*
check if a user is logged in and verify them, else set them as a temp user
*/
func IsLoggedIn(w http.ResponseWriter, r *http.Request) (person *User, err error) {
	// get username and session key
	// set as temp user is no valid strings found
	person = &User{}
	person.Temp = true
	ses, err := store.Get(r, "user")
	if err != nil {
		fmt.Println("error getting session:", err.Error())
		SendError(w, err.Error())
		return
	}
	if ses.Values["id"] == nil || ses.Values["session"] == nil {
		return
	}
	id := ses.Values["id"].(string)
	session := ses.Values["session"].(string)
	if strings.Trim(session, " ") == "" || strings.Trim(id, " ") == "" {
		return
	}

	// redirect a person to login and clear session info if person not found
	// or if session has expired (> 14 days sense last contact)
	person, err = GetUser(id)
	if err != nil || person == nil || person.Name == "" {
		ses.Values["id"] = nil
		ses.Values["session"] = nil
		_ = ses.Save(r, w)
		http.Redirect(w, r, "/login", 302)
		return
	}
	if person.SessionKey != session || time.Now().Unix()-person.Time > 1209600 {
		ses.Values["id"] = nil
		ses.Values["session"] = nil
		err = ses.Save(r, w)
		if err != nil {
			fmt.Println("error:", err.Error())
		}
		http.Redirect(w, r, "/login", 302)
		return
	}
	CheckIn(person)
	return
}

/*
check if person has a folder already
*/
func SetTempUser(w http.ResponseWriter, r *http.Request) (person *User, err error) {
	person = &User{}
	ses, err := store.Get(r, "user")
	if err != nil {
		RemoveSession(w, r)
		fmt.Println("error getting session:", err.Error())
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	person.Temp = true
	if ses.Values["id"] == nil {
		return
	}
	id := ses.Values["id"].(string)
	if strings.Trim(id, " ") == "" {
		return
	}
	person.Name = id
	person.Folder = id
	return
}

func RemoveSession(w http.ResponseWriter, r *http.Request) {
	ses, err := store.Get(r, "user")
	if err != nil {
		return
	}
	ses.Values["id"] = nil
	ses.Values["session"] = nil
	ses.Save(r, w)
}

func SaveTemp(w http.ResponseWriter, r *http.Request, person *User) (err error) {
	ses, err := store.Get(r, "user")
	if err != nil {
		fmt.Println("error getting session:", err.Error())
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	CreateUserFolder(person)
	person.Time = time.Now().Unix()
	person.SessionKey = RandomString(64)
	person.Temp = true
	person.Hash = "none"
	err = DBWriteMap(InsertUser, structs.Map(person))
	if err != nil {
		fmt.Println("error:", err.Error())
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	ses.Values["id"] = person.Name
	ses.Values["session"] = person.SessionKey
	err = ses.Save(r, w)
	if err != nil {
		fmt.Println("error:", err.Error())
	}
	return
}

/*
log user out by updating db and deleting session info
*/
func Logout(w http.ResponseWriter, r *http.Request) {
	// get current person logged in to update db
	defer RemoveSession(w, r)
	person, _ := IsLoggedIn(w, r)
	if person == nil || person.Name == "" {
		http.Redirect(w, r, "/", 302)
		return
	}

	// wipe out session key from db
	var args []interface{}
	args = append(args, "")
	args = append(args, time.Now().Unix())
	args = append(args, person.Name)
	DBWrite(UpdateUserSession, args)
	http.Redirect(w, r, "/", 302)
	return
}

/*
log someone in from info from post request
check if user exists in db, assign them a random session string and save
user's checkin to db
*/
func Login(w http.ResponseWriter, r *http.Request) {
	ses, err := store.Get(r, "user")
	if err != nil {
		fmt.Println("error getting session:", err.Error())
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	r.ParseForm()
	id := r.FormValue("id")
	if id == "" {
		http.Redirect(w, r, "/login", 302)
	}

	// query db for user
	// make array size 1 w/ an empty element
	person, err := GetUser(id)
	if err != nil || person == nil || person.Name == "" {
		fmt.Println("person:", person)
		http.Redirect(w, r, "/login", 302)
		return
	}

	// save session into db
	session := RandomString(64)
	var args []interface{}
	args = append(args, session)
	args = append(args, time.Now().Unix())
	args = append(args, person.Name)
	err = DBWrite(UpdateUserSession, args)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}

	// save session client side
	ses.Values["id"] = id
	ses.Values["session"] = session
	err = ses.Save(r, w)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	http.Redirect(w, r, "/dashboard", 302)
	return
}

func register(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "home.html"))
	if err != nil {
		fmt.Println("error:", err.Error())
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

/*
home page
*/
func home(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "home.html"))
	if err != nil {
		fmt.Println("error:", err.Error())
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

/*
home page
*/
func registration(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "home.html"))
	if err != nil {
		fmt.Println("error:", err.Error())
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

/*
dashboard page
*/
func dashboard(w http.ResponseWriter, r *http.Request) {
	ses, err := store.Get(r, "user")
	if err != nil {
		fmt.Println("error getting session:", err.Error())
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	file, err := ReadFile(path.Join(templateDir, "dashboard.html"))
	if err != nil {
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
}

/*
login page
*/
func loginPage(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "login.html"))
	if err != nil {
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

func news(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "login.html"))
	if err != nil {
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

func publications(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "publications.html"))
	if err != nil {
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}

func people(w http.ResponseWriter, r *http.Request) {
	file, err := ReadFile(path.Join(templateDir, "people.html"))
	if err != nil {
		w.Write([]byte("error"))
		return
	}
	w.Write(file)
	return
}
