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
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet that returns some example content. TODO: modify this file to handle
 * comments data
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  static final String CONTENT_TYPE = "application/json";
  final List<String> messages = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String json = gson.toJson(messages);
    response.setContentType(CONTENT_TYPE);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String content = request.getParameter("content").trim();
    long timestamp = System.currentTimeMillis();
    if (content.isEmpty()) {
      System.err.println("Empty comment submitted");
      response.sendError(400, "Empty comment submitted");
      return;
    }
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("timestamp", timestamp);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    messages.add(content);
    response.sendRedirect("/#comments");
  }
}
