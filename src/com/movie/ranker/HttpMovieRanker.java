package com.movie.ranker;

// Import required java libraries
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

// Extend HttpServlet class
public class HttpMovieRanker extends HttpServlet {

  private String message;

  public Map<String, String> movieMap;

  public void initMap() {

    movieMap = new HashMap<>();

    movieMap.put("interstellar", "https://en.wikipedia.org/wiki/Interstellar_(film)");
    movieMap.put("drive", "https://en.wikipedia.org/wiki/Drive_(2011_film)");
    movieMap.put("birdman", "https://en.wikipedia.org/wiki/Birdman_(film)");
    movieMap.put("carter", "https://en.wikipedia.org/wiki/John_Carter_(film)");
    movieMap.put("titanic", "https://en.wikipedia.org/wiki/Titanic_(1997_film)");
    movieMap.put("gravity", "https://en.wikipedia.org/wiki/Gravity_(film)");
    movieMap.put("bruno", "https://en.wikipedia.org/wiki/Br%C3%BCno");
    movieMap.put("star wars the force awakens", "https://en.wikipedia.org/wiki/Star_Wars:_The_Force_Awakens");
    movieMap.put("gone girl", "https://en.wikipedia.org/wiki/Gone_Girl_(film)");
    movieMap.put("boyhood", "https://en.wikipedia.org/wiki/Boyhood_(film)");
  }

  public void init() throws ServletException {
    // Do required initialization
    message = "Hello World";
    initMap();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    // Set response content type
    response.setContentType("text/html");

    message = request.getParameter("movie");

    WikiMovie wm = new WikiMovie(movieMap.get(message));

    String movieJsonString = getMovieJsonString(wm);

    // Actual logic goes here.
    PrintWriter out = response.getWriter();
    out.println(movieJsonString);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    doPost(request,response);
  }
  
  public String getMovieJsonString(WikiMovie wm) {

    JSONObject jsonObj = new JSONObject(); 

    jsonObj.put("title",wm.title);
    jsonObj.put("director",wm.directorName);
    jsonObj.put("rottenTomatoes",wm.rottenTomatoesScore);
    jsonObj.put("metacritic",wm.metaCriticScore);
    jsonObj.put("imgSrc",wm.posterUrl);
    jsonObj.put("total",wm.rottenTomatoesScore + wm.metaCriticScore + wm.directorAwardCount + wm.releaseDatePoints);

    return jsonObj.toJSONString();
  }

  public void destroy() {
    // do nothing.
  }
}