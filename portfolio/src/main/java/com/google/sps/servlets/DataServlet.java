// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    ArrayList<String> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String email = (String) entity.getProperty("email");
      String content = (String) entity.getProperty("content");
      String comment = "(" + email + ") " + content;
      
      comments.add(comment);
    }
    
    String json = new Gson().toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment-input");
    boolean upperCase = Boolean.parseBoolean(request.getParameter("upper-case"));

    // Convert the text to upper case.
    if (upperCase) {
      comment = comment.toUpperCase();
    }

    if (comment.length() != 0) {
      UserService userService = UserServiceFactory.getUserService();
      boolean isLoggedIn = userService.isUserLoggedIn() ? true : false;
      // Ensure user is logged in when this function is called
      if (!isLoggedIn) {
        throw new AuthenticationException("User must be logged in to post a comment!");
      }
      String userEmail = userService.getCurrentUser().getEmail();
      

      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("content", comment);
      commentEntity.setProperty("timestamp", System.currentTimeMillis());
      commentEntity.setProperty("email", userEmail);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
