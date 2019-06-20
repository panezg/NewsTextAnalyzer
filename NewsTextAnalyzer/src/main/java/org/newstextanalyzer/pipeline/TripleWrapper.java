package org.newstextanalyzer.pipeline;

import org.newstextanalyzer.NewsArticle;

import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.util.CoreMap;
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
  // NOTE: For convenience using StanfordNLP object
  private SequenceMatchResult<CoreMap> subjectPersonAbout;
  private SequenceMatchResult<CoreMap> subjectReplacement;

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

  public String getScoreAsString() {
    return String.valueOf(score);
  }
  
  public double getScore() {
    return score;
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
 
  public SequenceMatchResult<CoreMap> getSubjectPersonAbout() {
    return subjectPersonAbout;
  }

  public void setSubjectPersonAbout(SequenceMatchResult<CoreMap> subjectPersonAbout) {
    this.subjectPersonAbout = subjectPersonAbout;
  }
  
  public SequenceMatchResult<CoreMap> getSubjectReplacement() {
    return subjectReplacement;
  }

  public void setSubjectReplacement(SequenceMatchResult<CoreMap> subjectReplacement) {
    this.subjectReplacement = subjectReplacement;
  }
}
