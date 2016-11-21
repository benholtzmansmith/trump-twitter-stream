angular.module('trump-streaming', ['btford.socket-io', 'uiGmapgoogle-maps'])
 	.factory('mySocket', function (socketFactory) {
 		return socketFactory();
	})
	.controller('TrumpStreaming', function($scope, $http, mySocket) {

		$scope.streamResults = []

		$scope.positiveCircles = []

		$scope.negativeCircles = []

		$scope.map = { center: { latitude: 39, longitude: -98 }, zoom: 3 };

		mySocket.on("tweets", function(r){
			if (r.sentiment == "Positive" && r.geoLocation){
				$scope.positiveCircles.push({
					id:r.id,
					center: {
						latitude: r.geoLocation.latitude,
						longitude: r.geoLocation.longitude
					},
					radius: 100000,
					stroke: {
					    color: '#ff0000',
					    weight: 2,
					    opacity: 1
					},
					fill: {
					    color: '#ff0000',
					    opacity: 0.5
					}
				})
			}
			else if (r.sentiment == "Negative" && r.geoLocation) {
				$scope.positiveCircles.push({
					id:r.id,
					center: {
						latitude: r.geoLocation.latitude,
						longitude: r.geoLocation.longitude
					},
					radius: 100000,
					stroke: {
					    color: '#0000ff',
					    weight: 2,
					    opacity: 1
					},
					fill: {
					    color: '#0000ff',
					    opacity: 0.5
					}
				})
			}
			$scope.streamResults.push(r)
		})
  	});