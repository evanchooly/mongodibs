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
    <link rel="stylesheet" type="text/css" href="assets/css/dibs.css">
</head>

    <body ng-controller="OrdersListCtrl">

      <h1>MongoDiBs</h1>

      <div class="test">

        <div>
        
          <tabset>
            <tab heading="Group Orders">
  
              Find: <input ng-model="query">  
              
              <ul class="orders">
                <li ng-repeat="vendor in groupOrders | filter:query">
                  <p>{{vendor}} <button id="{{vendor}}" class="btn btn-default btn-sm" ng-click="notifyGroup($event)">Notify!</button> </p>
                </li>
              </ul>
  
            </tab>
  
            <tab heading="Individual Orders">
              Search: <input ng-model="query">  
  
              <ul class="orders">
                <li ng-repeat="order in singleOrders | filter:query">
                  <p>{{order.orderedBy}} - {{order.vendor}}
                    <button id="{{order.id}}" class="btn btn-default btn-sm" ng-click="notifySingle($event)">Notify!</button>
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