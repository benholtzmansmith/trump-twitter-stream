angular.module('trump-streaming', ['btford.socket-io'])
 	.factory('mySocket', function (socketFactory) {
 		return socketFactory();
	})
	.controller('TrumpStreaming', function($scope, $http, mySocket) {
	    $scope.stream = function(path){
	    	return mySocket.emit("stream","", onSuccess)
		}

		$scope.streamResults = []

		mySocket.on("tweets", function(r){
			if(!$scope.streamResults.incudes){
				$scope.streamResults.push(r)
			}
		})

		var onSuccess = function(response) { 
			console.log(response)
		}
  	});