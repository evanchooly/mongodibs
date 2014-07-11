var dibsApp = angular.module('dibsApp', ['ui.bootstrap']);
var groupOrderUrl = "orders/" + dateString() + "/group";
var singleOrderUrl = "orders/" + dateString() + "/single";

dibsApp.controller('OrdersListCtrl', function ($scope, $http) {

  $http.get(groupOrderUrl).success(function(data) {
    $scope.groupOrders = data;
  });
  
  $http.get(singleOrderUrl).success(function(data) {
    $scope.singleOrders = data;
  });

  // Notify that a single order has been received
  $scope.notifySingle = function($event) {
    orderId = $event.target.id;
    url = "/notify/order";
    $http.post(url, orderId).success(function(data) {
      var singleOrderUrl = "orders/" + dateString() + "/single";
      $http.get(singleOrderUrl).success(function(data) {
        $scope.singleOrders = data;
      });
    });
  };

  // Notify that a group order has been received
  $scope.notifyGroup = function($event) {
    vendor = $event.target.id;
    url = "/notify/" + dateString() + "/vendor";
    $http.post(url, vendor).success(function(data) {
      var singleOrderUrl = "orders/" + dateString() + "/single";
      $http.get(singleOrderUrl).success(function(data) {
        $scope.singleOrders = data;
      });
    });
  };

});
//controller


function dateString() {
  var today = new Date();
  var dateString;
  
  dateString = today.getFullYear() + '-'
                + ('0' + (today.getMonth()+1)).slice(-2) + '-'
                + ('0' + today.getDate()).slice(-2);

  return dateString;
};

