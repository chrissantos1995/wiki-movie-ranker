$(document).ready(function() {

	var movieBlockTemplate = '<div class="movie"> <img src="" alt=""> <div class="text"> <h1 class="title"></h1> <h2 class="subtitle">Directed by <span class="director"></span></h2> <p class="rotten-tomatoes"></p> <p class="metacritic"></p> </div> </div>';

	function createMovieBlock() {
		return $(movieBlockTemplate);
	}

	function getMovieScoreJSON(movieName) {

		$.ajax({

			url : "HttpMovieRanker",
			method : "POST",
			data : {
				movie : movieName
			}

		}).done(function(data) {

			console.log(data);
		});
	}

	getMovieScoreJSON("boyhood");

});