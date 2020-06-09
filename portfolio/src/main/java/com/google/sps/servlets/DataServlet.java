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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
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

  private static final String COMMENT = "Comment";
  private static final String CONTENT = "content";
  private static final String CONTENT_TYPE = "application/json";
  private static final String LANGUAGE = "language";
  private static final String MAX_COMMENTS = "max";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String maxContent = request.getParameter(MAX_COMMENTS);
    int maxComments;
    try {
      maxComments = Integer.parseInt(maxContent);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxContent);
      response.sendError(400, "Invalid parameter \"max\" in request.");
      return;
    }
    String language = request.getParameter(LANGUAGE);
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    List<String> messages = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (messages.size() >= maxComments) {
        break;
      }
      String content = (String) entity.getProperty(CONTENT);
      try {
        Translation translation = translate.translate(content, Translate.TranslateOption.targetLanguage(language));
        String translatedText = translation.getTranslatedText();
        messages.add(translatedText);
      } catch (TranslateException e) {
        System.err.println("Invalid translation options.");
        response.sendError(400, "Invalid translation request.");
        return;
      }
    }
    Gson gson = new Gson();
    String json = gson.toJson(messages);
    response.setContentType(CONTENT_TYPE);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String content = request.getParameter(CONTENT).trim();
    long timestamp = System.currentTimeMillis();
    if (content.isEmpty()) {
      System.err.println("Empty comment submitted");
      response.sendError(400, "Empty comment submitted");
      return;
    }
    Entity commentEntity = new Entity(COMMENT);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("timestamp", timestamp);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/#comments");
  }
}
