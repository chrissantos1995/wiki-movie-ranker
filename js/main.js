$(document).ready(function() {

	var movieBlockTemplate = '<div class="movie"> <img class="poster" src="" alt=""> <div class="text"> <h1 class="title"></h1> <h2 class="subtitle">Directed by <span class="director"></span></h2> <p class="score"><img class="logo" src="http://d3biamo577v4eu.cloudfront.net/static/images/trademark/fresh.png"><span class="rotten-tomatoes"></span></p> <p class="score"><img class="logo" src="https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Metacritic.svg/88px-Metacritic.svg.png"><span class="metacritic"></span></p> </div> </div>';

	function createMovieBlock() {
		return $(movieBlockTemplate);
	}

	function insertMovie($movieBlock) {

		var movies = $("#results").children(".movie");

		var i;
		for(i = 0; i < movies.length; i++) {

			var $currentMovie = $(movies[i]);

			console.log("checking " + $currentMovie.find(".title").text() + " vs. " + $movieBlock.find(".title").text());

			console.log($currentMovie.data("total") + " vs. " + $movieBlock.data("total"));

			if($currentMovie.data("total") < $movieBlock.data("total")) {

				console.log("found total less than ");

				$currentMovie.before($movieBlock);
				return;
			}
		}

		$("#results").append($movieBlock);
	}

	function addMovieFromJsonStr(jsonStr) {

		console.log("Creating movie from json str");

		var jsonObj = JSON.parse(jsonStr);
		var $movieBlock = createMovieBlock();

		console.log("movie block created");

		$movieBlock.find(".title").text(jsonObj.title);
		$movieBlock.find(".director").text(jsonObj.director);
		$movieBlock.find(".rotten-tomatoes").text(jsonObj.rottenTomatoes + "%");
		$movieBlock.find(".metacritic").text(jsonObj.metacritic + "/100");
		$movieBlock.find("img.poster").attr("src",jsonObj.imgSrc);
		$movieBlock.data("total",jsonObj.total);

		insertMovie($movieBlock);
	}

	function getMovieScoreJSON(movieName) {

		$.ajax({

			url : "HttpMovieRanker",
			method : "POST",
			data : {
				movie : movieName
			}

		}).done(function(jsonStr) {

			console.log("String received: " + jsonStr);
			addMovieFromJsonStr(jsonStr);

		});
	}

	$("#movie-form").submit(function(e) {

		e.preventDefault();

		queryString = $("#movie-form-text").val();
		getMovieScoreJSON(queryString);
	});

});