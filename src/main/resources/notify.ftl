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

    <body ng-controller="OrdersListCtrl">

        <div class="dashboard">
            <div id="content">
                <a href="/dibs">Dibs!</a>
                <br/><a href="/notify">Delivery Notifications</a>
                <br/>
                
                <tabset>
                    <tab heading="Group Orders">
                        Search: <input ng-model="query">
                        <ul class="orders">
                            <li ng-repeat="vendor in groupOrders | filter:query">
                                <p>{{vendor}}
                                    <button id="{{vendor}}" class="btn btn-default btn-sm" ng-click="notifyGroup(vendor)">Notify!</button>
                                </p>
                            </li>
                        </ul>
                    </tab>

                    <tab heading="Individual Orders">
                        Search: <input ng-model="query">
                        <ul class="orders">
                            <li ng-repeat="order in singleOrders | filter:query">
                                <p>{{order.orderedBy}} - {{order.vendor}}
                                    <button class="btn btn-default btn-sm" ng-click="notifySingle(order)">Notify!</button>
                                </p>
                            </li>
                        </ul>
                    </tab>
                </tabset>
                <hr />
            </div>
        </div>
    </body>
</html>