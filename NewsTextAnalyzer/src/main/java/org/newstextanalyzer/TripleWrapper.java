package org.newstextanalyzer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class TripleWrapper {
  /*
   * private String time; private String id; private String URL; private
   * NewsPublication newsPublication;
   */

  private ChunkedBinaryExtraction triple;
  private boolean objectMatched;
  private NewsArticle newsArticle;
  private double score;
  private String extractedDate;
  private String extractedLocation;

  public TripleWrapper(ChunkedBinaryExtraction triple, NewsArticle newsArticle, double score) {
    this.triple = triple;
    this.objectMatched = false;
    this.newsArticle = newsArticle;
    this.score = score;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date date;
    try {
      date = dateFormat.parse(newsArticle.getTime());
    } catch (ParseException e) {
      e.printStackTrace();
      throw new  RuntimeException();
    }
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    this.extractedDate = formatter.format(date);
    this.extractedLocation = null;
  }

  public ChunkedBinaryExtraction getTriple() {
    return triple;
  }

  public String getScore() {
    return String.valueOf(score);
  }
  
  public String getDate() {
    if (extractedDate == null) {
      return newsArticle.getTime();
    } else {
      return extractedDate;
    }
  }

  public String getSource() {
    StringBuilder sb = new StringBuilder();
    sb.append(newsArticle.getNewsPublication());
    sb.append("; ");
    sb.append(newsArticle.getURL());
    return sb.toString();
  }

  public String getLocation() {
    return extractedLocation;
  }

  public String toString() {
    return "";
  }
  
  public void setExtractedDate(String extractedDate) {
    this.extractedDate = extractedDate;
  }
  
  public void setExtractedLocation(String extractedLocation) {
    this.extractedLocation = extractedLocation;    
  }
  
  public void setObjectMatched(boolean objectMatched) {
    this.objectMatched = objectMatched;
  }
  
  public boolean isObjectMatched() {
    return objectMatched;
  }
}
