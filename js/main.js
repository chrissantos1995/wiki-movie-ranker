$(document).ready(function() {

	var movieBlockTemplate = '<div class="movie"> <img src="" alt=""> <div class="text"> <h1 class="title"></h1> <h2 class="subtitle">Directed by <span class="director"></span></h2> <p class="rotten-tomatoes"></p> <p class="metacritic"></p> </div> </div>';

	function createMovieBlock() {
		return $(movieBlockTemplate);
	}

	function movieFromJsonStr(jsonStr) {

		console.log("Creating movie from json str");

		var jsonObj = JSON.parse(jsonStr);
		var $movieBlock = createMovieBlock();

		console.log("movie block created");

		$movieBlock.find(".title").text(jsonObj.title);
		$movieBlock.find(".director").text(jsonObj.director);
		$movieBlock.find(".rotten-tomatoes").text(jsonObj.rottenTomatoes);
		$movieBlock.find(".metacritic").text(jsonObj.metacritic);
		$movieBlock.find("img").attr("src",jsonObj.imgSrc);
		$movieBlock.data("total",jsonObj.total);

		$("#results").append($movieBlock);
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
			movieFromJsonStr(jsonStr);

		});
	}

	$("#movie-form").submit(function(e) {

		e.preventDefault();

		queryString = $("#movie-form-text").val();
		getMovieScoreJSON(queryString);
	});

});