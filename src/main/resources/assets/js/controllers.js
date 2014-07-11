var dibsApp = angular.module('dibsApp', ['ui.bootstrap']);


dibsApp.controller('OrdersListCtrl', function ($scope, $http) {

  var today = new Date();
  var dateString;
  
  dateString = today.getFullYear() + '-'
                + ('0' + (today.getMonth()+1)).slice(-2) + '-'
                + ('0' + today.getDate()).slice(-2);

  var groupOrderUrl = "orders/" + dateString + "/group";
  $http.get(groupOrderUrl).success(function(data) {
    $scope.groupOrders = data;
  });

  var singleOrderUrl = "orders/" + dateString + "/single";
  $http.get(singleOrderUrl).success(function(data) {
    $scope.singleOrders = data;
  });

  //$scope.notify = function() {
  //  $http.get(url).success(function(data) {
  //  $scope.groupOrders = data;
  //});

  //$scope.notify();

});

var TabsDemoCtrl = function ($scope) {
};

