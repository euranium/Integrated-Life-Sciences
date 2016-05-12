/*
Dashboard.js
Runs the "front end" of the dashboard
Dynamically populates the dashboard page
*/

var app = angular.module('dashboard', ['ngSanitize', 'ui.router', 'chart.js']);

app.config([
'$stateProvider',
'$urlRouterProvider',
function($stateProvider, $urlRouterProvider) {

  $stateProvider
    .state('default', {
      url: '/default',
      templateUrl: '/html/default.html'
    });

  $stateProvider
    .state('result', {
      url: '/result/{id}',
      templateUrl: '/html/result.html',
      controller: 'ResultCtrl'
    });

    $stateProvider
      .state('model', {
        url: '/model/{id}',
        templateUrl: function ($stateParams){
          return "/html/models/" + $stateParams.id + ".html";
        },
        controller: 'ModelCtrl'
      });

  $urlRouterProvider.otherwise('default');
}]);



app.directive('file', function() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
          obj: '=',
          index: '='
        },
        templateUrl: '/html/file.html'
    };
});

app.controller('ResultCtrl', [
'$scope',
'$stateParams',
'$http',
function($scope, $stateParams, $http){
  $scope.obj = {}
  $scope.message = "";
  $http.get('/api/results/query?name=' + $stateParams.id).success(function(data) {
    if(data.hasOwnProperty('Error')){
      $scope.message = "Invalid url ¯\\\_(ツ)_/¯";
    }
    else{
      $scope.obj = htmlify(data, "modEvo");
    }
  });

}]);

app.controller('ModelCtrl', [
'$scope',
'$rootScope',
'$stateParams',
'$http',
function($scope, $rootScope, $stateParams, $http){

  $scope.working = false;

  //for modevo example graph in form
  $scope.types = [1, 2, 3];
  $scope.graphType = 1;
  $scope.constant = -3;
  var g = buildData(1);
  $scope.labels = g.labels;
  $scope.series = ['Function 1'];
  $scope.data = g.data;
  $scope.opt = {
    bezierCurve: true,
    showXLabels: 25,
    responsive: true
  };

   $scope.onClick = function (points, evt) {
     console.log(points, evt);
   };

   $scope.loadData = function(){
     var c = typeof $scope.constant;
     if(c === 'number'){
       $scope.data = buildData($scope.graphType, $scope.constant).data;
     }
   }

   //Sends the form data to the server to compute a result--*/
   //Gets called when the submit button is clicked----------*/
   //Builds argument list from inputs with 'sending' class--*/
   $scope.send = function(name) {
     if($scope.working){
       return;
     }
     $scope.working = true;
     console.log("sending for prog: " + name);

     args = getInput();

     obj = {
       Name: name,
       Input: args,
     };

     $http({
         method: 'POST',
         url: 'api/submit',
         data: JSON.stringify(obj),
         headers: {
           'Content-Type': 'application/x-www-form-urlencoded'
         }
       })
       .success(function(data) {
         console.log(data.Name);
         console.log("Data sent");
         $rootScope.$broadcast('lemonade', 1);
         $scope.working = false;
       })
       .error(function(data){
         console.log("error!");
       });

   }



}]);

app.controller('SideBarCtrl', [
  '$scope',
  '$rootScope',
  '$http',
  '$compile',
  '$sce',
  '$interval',
  '$location',
  function($scope, $rootScope, $http, $compile, $sce, $interval, $location) {


    ////////////////////////////////////////////////////////////////////////////
    //Initialization
    ////////////////////////////////////////////////////////////////////////////
    $scope.software = [];
    $scope.results = [];
    $scope.result = {};

    //$scope.example.data = buildData();
    $scope.prog = "modEvo"; //hard coded for now, parse from url in future

    //Call backend to get list of all software----------------*/
    $http.get('/api/listsoftware').success(function(data) {
      $scope.software = data;
    });

    //Call backend to get list of all results----------------*/
    $http.get('/api/results').success(function(data) {
      if(data != "null"){
        $scope.results = data;
      }
      else{
        $scope.results = [];
      }
    });

    ////////////////////////////////////////////////////////////////////////////
    //Angular Functions
    ////////////////////////////////////////////////////////////////////////////

    //Charting utility function--------------------------------*/
    $scope.onClick = function(points, evt) {
      console.log(points, evt);
    };

    //Loads the submission template for the given software-----*/
    //compiles the returned data to the dom--------------------*/
    $scope.loadSoftware = function(name, event) {
      fixSelection($(event.target));
      $location.path("/model/" + name);

      // $http.get('/api/template/query?name=' + name).success(function(data) {
      //   $('#dash').html($compile(data)($scope)); //$sce.trustAsHtml(data);
      // });
    };

    //Gets called wheend api, which returns the files--------*/
    //Parse the files, and compile them to the dom-----------*/
    //Name is the string seen on the dashboard menu with date*/
    $scope.loadResult = function(name, event) {
      fixSelection($(event.target));
      $location.path("/result/" + name);
    };

    $rootScope.$on('lemonade', function(event, msg){
        $http.get('/api/results').success(function(data) {
          if(data != "null"){
            $scope.results = data;
          }
          else{
            $scope.results = [];
          }
        });
    });

  }
]);

//Non angular functions

function fixSelection(element){
  $(".selected").each(function(i, obj){
    if($(obj).hasClass("selected")){
      $(obj).removeClass("selected");
    }
  })

  element.addClass("selected");
}

//returns an object with data f-3ield and labels field
function buildData(type, c){
  var final = {};
  var data = [];
  var outer = []
  var labels = []

  if(type == 1){
    for(var i = -8.0; i <= 8.0; i+= 0.5){
      labels.push(String(i));
      var ans = i/Math.pow((1+i*i), 0.5);
      data.push(ans)
    }
  }
  else if(type == 2){
    for(var i = -8.0; i <= 8.0; i+= 0.5){
      labels.push(String(i));
      var ans = i/(1+Math.abs(i));
      data.push(ans)
    }
  }
  else if(type == 3){
    for(var i = -8.0; i <= 8.0; i+= 0.5){
      labels.push(String(i));
      var ans = 1/(1+Math.exp(c*i));
      data.push(ans)
    }
  }

  outer.push(data);
  final.data = outer;
  final.labels = labels;
  return final;
}
