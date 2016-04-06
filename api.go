package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	//"os/exec"
	"path"
)

var ()

func APIListSoftware(w http.ResponseWriter, r *http.Request) {
	list, err := ListDir(progDir)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	b, err := json.Marshal(list)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	fmt.Println(string(b[:]))
	w.Write(b)
	return
}

func APITemplate(w http.ResponseWriter, r *http.Request) {
	// get folder name
	u := r.URL.Query()
	if len(u["name"]) <= 0 {
		w.Write([]byte("No Query"))
		return
	}
	q := u["name"][0]
	if q == "" {
		w.Write([]byte(""))
		return
	}
	p := path.Join(progDir, q, q+".tmpl")
	if !CheckFile(p) {
		w.Write([]byte("No File found"))
		return
	}
	file, err := ReadFile(p)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	w.Write([]byte(file))
	return
}

func APISubmitForm(w http.ResponseWriter, r *http.Request) {
	r.ParseForm()
	person, err := IsLoggedIn(w, r)
	if err != nil || person.user_name == "" {
		return
	}
	// get program name
	name := r.Form["name"][0]
	if name == "" {
		w.Write([]byte("No name"))
		return
	}
	if name == "" || !IsExec(name) {
		w.Write([]byte("not exec or no name" + name))
		return
	}
	typ := r.Form["type"][0]
	if typ == "" {
		w.Write([]byte("No Type"))
		return
	}
	dir := path.Join(UserDir, person.hash, RandomString(12))
	fmt.Println("copying to:", dir)
	err = CopyDir(path.Join("executables", name), dir)
	if err != nil {
		fmt.Println("error:", err.Error())
		w.Write([]byte("Error processing\n"))
	}
	var args []string
	if typ == "java" {
		input := Sort(r.Form)
		// set arguments and path
		args = append(args, "java")
		args = append(args, name)
		args = append(args, input...)
		args = append(args, dir)
		fmt.Println(args)
		Tasks <- (args)
		//Tasks <- exec.Command("java", args...)
		fmt.Println("done")
		// TODO: change this
		//Tasks <- exec.Command("mv", "meanTraitOneValues_GeneralModel_1.txt", dir)
		//Tasks <- exec.Command("mv", "meanTraitTwoValues_GeneralModel_1.txt", dir)
		//Tasks <- exec.Command("mv", "speciesInputs_GeneralModel_1.txt", dir)
		http.Redirect(w, r, "/dashboard", 302)
		return
	} else {
		http.Redirect(w, r, "/dashboard", 302)
	}
}

func APIListResults(w http.ResponseWriter, r *http.Request) {
	//person, err := IsLoggedIn(w, r)
	//if err != nil {
	//w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
	//return
	//}
	//list, err := ListDir(path.Join(UserDir, person.hash))
	list, err := ListDir(path.Join(UserDir, "aaa"))
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	b, err := json.Marshal(list)
	if err != nil {
		w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
		return
	}
	fmt.Println(string(b[:]))
	w.Write(b)
	return
}

// hard coded results page right now
// expecting /query?name=folder
func APIGetResults(w http.ResponseWriter, r *http.Request) {
	//person, err := IsLoggedIn(w, r)
	//if err != nil {
	//w.Write([]byte(fmt.Sprintf("Error: %s\n", err.Error())))
	//return
	//}
	r.ParseForm()
	q := r.Form["name"][0]
	if q == "" || !IsResult("aaa", q) {
		w.Write([]byte(""))
		return
	}
	//result, err := ReadFile(path.Join(UserDir, person.hash, folder, q, "*.txt"))
	result := ReadFileType(path.Join(UserDir, "aaa", q), ".txt")
	w.Write([]byte(result))
}
