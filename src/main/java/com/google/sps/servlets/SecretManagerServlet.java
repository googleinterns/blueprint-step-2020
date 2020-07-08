package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.com.google.gson.Gson;

@WebServlet("/secret-manager")	
public class SecretManagerServlet extends HttpServlet {	

  @Override	
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {	
    String apiKey = System.getProperty("API_KEY").toString();
    response.setContentType("text/html");
    response.getWriter().println(apiKey);
  }
  /*
  public static void sendJson(HttpServletResponse response, Object object) throws IOException {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    response.getWriter().println(json);
  }*/
}
