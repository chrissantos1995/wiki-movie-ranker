package com.movie.ranker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import java.net.URL;

import redis.clients.jedis.Jedis;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiMovie implements Comparable<WikiMovie> {

	// Global Variables

	public String url;

	public String directorUrl;
	public List<String> castUrls;

	public Integer rottenTomatoesScore;
	public Integer metaCriticScore;

	public static String  wikiBaseUrl = "https://en.wikipedia.org";

	public int directorAwardCount = 0;
	public String posterUrl = "";
	public int releaseDatePoints = 0;
	public String directorName = "";

	// Constructors

	public WikiMovie(String url) throws IOException {
		this.url = url;
		init();
	}

	public void init() throws IOException {

		//download and parse the document
		Document doc = Jsoup.connect(url).get();

		//select the main content text
		Element content = doc.getElementById("mw-content-text");

		//castUrls = getCastUrls(content);

		rottenTomatoesScore = getMovieRating(content, "rotten tomatoes", "(\\d+%)");

		metaCriticScore = getMovieRating(content, "metacritic", "(score of \\d+)");

		directorUrl = getDirectorWikiUrl(url);

		directorAwardCount = getDirectorAwardCount(url);
		releaseDatePoints = releasedDuringOscarSeason(url);
		posterUrl = getMoviePosterUrl(url);
		directorName = getDirectorName(url);
		/*castUrls = getCastUrls(content); */
	}

	// return a List of URLs of the WikiMovie's cast
	public List<String> getCastUrls(Element content) {

		// The list to return
		List<String> castList = new ArrayList<>();

		return castList;
	}

	// returns the movie rating of a wikipedia page, given target string and regex of numerical representation
	private Integer getMovieRating(Element content, String targetString, String ratingRegex) {

		Elements paragraphs = content.select("p");

		for (Element p : paragraphs) { // iterate the paragraphs

			String textContent = p.html().toLowerCase();

			if (textContent.contains(targetString)) {

				int targetSentenceIndex = textContent.indexOf(targetString);

				String targetSentence = getContainingSentence(textContent, targetSentenceIndex);

				String score = firstRegexMatch(ratingRegex, targetSentence);

				if (score == null)
					break;

				return getInteger(score);
			}
		}

		return 55; // Default value
	}

	private Integer getInteger(String str) {
		String intStr = firstRegexMatch("(\\d+)", str);
		return Integer.parseInt(intStr);
	}

	// returns the percent number closest to the index given in a block of text restricted to the sentence containing the index
	private String getContainingSentence(String textContent, int targetSentenceIndex) {

		int startIndex;
		int endIndex;
		char c;

		startIndex = targetSentenceIndex;
		c = textContent.charAt(startIndex);

		while (startIndex >= 1 && c != '.') {
			--startIndex;
			c = textContent.charAt(startIndex);
		}

		endIndex = textContent.indexOf(".", startIndex + 1);

		endIndex = endIndex > 0 ? endIndex : textContent.length();

		return textContent.substring(startIndex, endIndex);
	}

	public static String getDirectorName(String source) throws IOException {
		// Grabs the Movie URL for use in JSoup
		Document doc = Jsoup.connect(source).get();
		// Table found on right hand side of movie wiki page
		Elements movieInfoTable = doc.select(".infobox.vevent");
		// All the rows inside of the table
		Elements rows = movieInfoTable.select("tr");
		// finds director url and appends to base url
		String director = rows.get(2).select("td").select("a").first().text();
		return director;
	}

	public static String getDirectorWikiUrl(String source) throws IOException {
		// Grabs the Movie URL for use in JSoup
		Document doc = Jsoup.connect(source).get();
		// Table found on right hand side of movie wiki page
		Elements movieInfoTable = doc.select(".infobox.vevent");
		// All the rows inside of the table
		Elements rows = movieInfoTable.select("tr");
		// finds director url and appends to base url
		String directorWikiUrl = wikiBaseUrl + rows.get(2).select("td").select("a").first().attr("href");
		return directorWikiUrl;
	}

	public static int getDirectorAwardCount(String source) throws IOException {
		Document doc = Jsoup.connect(getDirectorWikiUrl(source)).get();
		// Given that there is no common format for Director accolades.
		// This code counts particular words that would be expected in 'successful' director page
		// Hypothesis: Higher  frequency of these words -> higher chance of winning oscar
		Elements won = doc.getElementsContainingOwnText(" won ");
		Elements popular = doc.getElementsContainingOwnText(" popular ");
		Elements influential = doc.getElementsContainingOwnText(" influential ");
		Elements award = doc.getElementsContainingOwnText(" award ");
		Elements acclaimed = doc.getElementsContainingOwnText(" acclaimed ");
		Elements historic = doc.getElementsContainingOwnText(" historic ");
		Elements festival = doc.getElementsContainingOwnText(" festival ");
		Elements nominated = doc.getElementsContainingOwnText(" nominated ");
		int sumOfTerms = won.size() + popular.size() + influential.size() + award.size()
		                 + acclaimed.size() + festival.size() + historic.size() + nominated.size();
		return sumOfTerms;
	}

	// Returns the first match of a regex string
	private String firstRegexMatch(String regex, String text) {

		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(text);

		if (mat.find())
			return mat.group(1);

		return null;
	}


	public static int releasedDuringOscarSeason(String source) throws IOException {
		// Checks to see if a movie was released during September - December. If so, return true.
		Document doc = Jsoup.connect(source).get();
		// Table found on right hand side of movie wiki page
		Elements movieInfoTable = doc.select(".infobox.vevent");
		// All the rows inside of the table
		Elements rows = movieInfoTable.select("tr");
		// finds director url and appends to base url
		int releaseDateIndex = rows.size() - 6;
		try {
			String dateString = rows.get(releaseDateIndex).select("td").first().select("li").first().ownText();
			String dateStringReal = dateString.replaceAll("[^A-Za-z0-9]", " ");
			String month = dateStringReal.split(" ")[0];
			int datePoints = releaseDateHelper(month);
			return datePoints;
		} catch (Exception e) {
			int datePoints = 0;
			return datePoints;
		}
		// String Str = new String(dateString);
		// String[] hi = Str.trim().split("\\s+");

	}

	public static int releaseDateHelper(String monthUpper) {
		String month = monthUpper.toLowerCase();
		int datePoints = 0;
		switch (month) {
			case "september" : 
				datePoints = 5;
				break;
			case "october" :
				datePoints = 15;
				break;
			case "november" :
				datePoints = 20;
				break;
			case "decemeber" :
				datePoints = 10;
				break;		
			case "january" :
				datePoints = -20;
				break;
			case "february" :
				datePoints = -5;
				break;
			default: 
				datePoints = 0;
                break;
		}
		return datePoints;
	}

	public static String getMoviePosterUrl(String source) throws IOException {
		// Grabs the Movie URL for use in JSoup
		Document doc = Jsoup.connect(source).get();
		// Table found on right hand side of movie wiki page
		Elements movieInfoTable = doc.select(".infobox.vevent");
		// All the rows inside of the table
		Elements rows = movieInfoTable.select("tr");
		// finds director url and appends to base url
		String posterUrl = rows.get(1).select("td").select("a").select("img").attr("src");
		String httpPosterUrl = "https:" + posterUrl;
		return httpPosterUrl;
	}

	public int compareTo(WikiMovie that) {

		int thisWikiMovieTotal = this.rottenTomatoesScore + this.metaCriticScore + this.directorAwardCount + this.releaseDatePoints;
		int thatWikiMovieTotal = that.rottenTomatoesScore + that.metaCriticScore + that.directorAwardCount + that.releaseDatePoints;

		return thisWikiMovieTotal - thatWikiMovieTotal;
	}

	public static void main(String[] args) throws IOException {

		WikiMovie wm = new WikiMovie("https://en.wikipedia.org/wiki/Br%C3%BCno");
		WikiMovie wm2 = new WikiMovie("https://en.wikipedia.org/wiki/Birdman_(film)");

		System.out.println("Bruno Rotten tomatoes score: " + wm.rottenTomatoesScore);
		System.out.println("Bruno Metacritic score: " + wm.metaCriticScore);

		System.out.println("Birdman Rotten tomatoes score: " + wm2.rottenTomatoesScore);
		System.out.println("Birdman Metacritic score: " + wm2.metaCriticScore);

		System.out.println("Bruno vs Birdman: " + wm.compareTo(wm2));
	}
}