var dibsApp = angular.module('dibsApp', ['ui.bootstrap']);
var groupOrderUrl = "orders/" + dateString() + "/group";
var singleOrderUrl = "orders/" + dateString() + "/single";
var grabOrderUrl = "orders/" + dateString() + "/upforgrabs";

dibsApp.controller('OrdersListCtrl', function ($scope, $http) {

    $http.get(groupOrderUrl).success(function (data) {
        $scope.groupOrders = data;
    });

    $http.get(singleOrderUrl).success(function (data) {
        $scope.singleOrders = data;
    });

    // Notify that a single order has been received
    $scope.notifySingle = function (order) {
        url = "/notify/order";
        $http.post(url, order.id).success(function (data) {
            $http.get(singleOrderUrl).success(function (data) {
                $scope.singleOrders = data;
            });
        });
    };

    // Notify that a group order has been received
    $scope.notifyGroup = function (vendor) {
        url = "/notify/" + dateString() + "/vendor";
        $http.post(url, vendor).success(function (data) {
            $http.get(groupOrderUrl).success(function (data) {
                $scope.groupOrders = data;
            });
        });
    };
});
//controller

dibsApp.controller('GrabsListCtrl', function ($scope, $http, $sce) {

    $http.get(grabOrderUrl).success(function (data) {
        $scope.grabOrders = data;
    });

    // Notify that a single order has been received
    $scope.claim = function ($event, order) {
        var data = { 'id': order.id, 'email': ''}
        var url = "/claim";
        $http.post(url, data).success(function (data) {
            if(data.claimedBy) {
                order.claimedBy = data.claimedBy
            }
        });
    };
    
    $scope.sanitize = function(order) {
        return $sce.trustAsHtml(order.contents);
    }
});


function dateString() {
    var today = new Date();
    var dateString;

    dateString = today.getFullYear() + '-'
        + ('0' + (today.getMonth() + 1)).slice(-2) + '-'
        + ('0' + today.getDate()).slice(-2);

    return dateString;
};

