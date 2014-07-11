var dibsApp = angular.module('dibsApp', ['ui.bootstrap']);


dibsApp.controller('OrdersListCtrl', function ($scope, $http) {

  var Today = new Date();
  var DateString;
  
  DateString = Today.getFullYear() + '-'
                   + ('0' + (Today.getMonth()+1)).slice(-2) + '-'
                   + ('0' + Today.getDate()).slice(-2);

  var url = "orders/" + DateString + "/group";
  $http.get(url).success(function(data) {
    $scope.vendors = data;
  });

  $scope.iorders = [
    { 'vendor': 'Kosher Deluxe'},
    { 'vendor': 'Schnippers Quality Kitchen'}
  ];

  $scope.notify = function() {
    $http.get(url).success(function(data) {
    $scope.vendors = data;
  });
    
  };

  $scope.notify();

});

var TabsDemoCtrl = function ($scope) {
};

