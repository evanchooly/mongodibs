<!DOCTYPE html>

<html ng-app="dibsApp">
    <head>
        <script src="webjars/jquery/1.11.0/jquery.js"></script>
        <script src="webjars/bootstrap/2.2.1/js/bootstrap.js"></script>
        <script src="webjars/angularjs/1.3.0-beta.13/angular.js"></script>
        <script src="webjars/angular-ui-bootstrap/0.11.0/ui-bootstrap.js"></script>
        <script src="webjars/angular-ui-bootstrap/0.11.0/ui-bootstrap-tpls.js"></script>
        <script src="assets/js/controllers.js"></script>
        <link rel="stylesheet" type="text/css" href="webjars/bootstrap/2.2.1/css/bootstrap.css">
        <link rel="stylesheet" type="text/css" href="webjars/bootstrap/2.2.1/css/bootstrap-responsive.css">
        <link rel="stylesheet/less" type="text/less" href="assets/css/dibs.less">
        <script src="webjars/less/1.3.1/less.min.js"></script>
    </head>
    
    <body ng-controller="GrabsListCtrl">
        
        <div class="dashboard">
        
            <div id="content">
                <a href="/dibs">Dibs!</a>
                <br/><a href="/notify">Delivery Notifications</a>
                <br/>
                
                Find: <input ng-model="query">
        
                <table class="orders" ng-repeat="order in grabOrders | filter:query">
                    <tr class=order-row">
                        <td class="order-contents" ng-bind-html="sanitize(order)" border="1"></td>
                        <td class="claim-button">
                            <span ng-show="order.claimedBy">This order was claimed by {{order.claimedBy}}</span>
                            <img ng-show="!order.claimedBy" src="assets/images/mine.jpg" ng-click="claim($event, order)" 
                                 alt="Claim!">
                        </td>
                    </tr>
                </table>
        
                <hr/>
            </div>
        </div>
    </body>
</html>