package org.newstextanalyzer;

import java.io.File;

public abstract class NewsArticle {
  private String time;
  private String id;
  private String URL;
  private String title;
  private String body;
  private NewsPublication newsPublication;
  
  public enum NewsPublication {
    THE_GUARDIAN
  }
  
  public NewsArticle(File file) throws Exception {
    construct(file);
  }
  
  public abstract void construct(File file) throws Exception;
  
  protected void populate(String time, String id, String URL, String title, String body, NewsPublication newsPublication) {
    this.time = time;
    this.id = id;
    this.URL = URL;
    this.title = title;
    this.body = body;
    this.newsPublication = newsPublication;
  }

  public String getTime() {
    return time;
  }

  public String getId() {
    return id;
  }

  public String getURL() {
    return URL;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public NewsPublication getNewsPublication() {
    return newsPublication;
  }
  
  
}
