package org.newstextanalyzer.pipeline;

import org.newstextanalyzer.NewsArticle;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * @author gpanez
 *
 */
public class TripleWrapper {
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
    this.extractedLocation = null;
  }

  public ChunkedBinaryExtraction getTriple() {
    return triple;
  }

  public String getScore() {
    return String.valueOf(score);
  }
  
  public String getExtractedDate() {
    return extractedDate;
    
    //DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //this.extractedDate = formatter.format(date);
  }

  public String getSource() {
    StringBuilder sb = new StringBuilder();
    // sb.append(newsArticle.getNewsPublication());
    // sb.append("; ");
    sb.append(newsArticle.getURL());
    return sb.toString();
  }

  public String getLocation() {
    return extractedLocation;
  }
  
  public NewsArticle getNewsArticle() {
    return newsArticle;
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
