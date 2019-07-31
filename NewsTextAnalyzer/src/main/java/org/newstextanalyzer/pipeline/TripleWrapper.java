package org.newstextanalyzer.pipeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.newstextanalyzer.NewsArticle;

import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * @author gpanez
 *
 */
public class TripleWrapper {
  static SimpleDateFormat dateTimeComplexFormatter;
  static SimpleDateFormat dateSimpleFormatter;
  
  private ChunkedBinaryExtraction triple;
  private boolean objectMatched;
  private boolean subjectOnlyPerson;
  private NewsArticle newsArticle;
  private double score;
  private String extractedDate;
  private String extractedLocation;
  private Date sourceDate;

  // NOTE: For convenience using StanfordNLP object
  private SequenceMatchResult<CoreMap> subjectPersonAbout;
  private SequenceMatchResult<CoreMap> subjectReplacement;
  
  static {
    dateTimeComplexFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateSimpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
  }

  public TripleWrapper(ChunkedBinaryExtraction triple, NewsArticle newsArticle, double score) {
    this.triple = triple;
    this.objectMatched = false;
    this.subjectOnlyPerson = false;
    this.newsArticle = newsArticle;
    this.score = score;
    this.extractedLocation = null;

    Date referenceDate;
    try {
      referenceDate = dateTimeComplexFormatter.parse(newsArticle.getTime());
      this.sourceDate = referenceDate;
    } catch(ParseException pe) {
      pe.printStackTrace();
      throw new RuntimeException();
    }

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
  
  public Date getSourceDate() {
    return sourceDate;
  }
  
  public String getSourceDateAsString() {
    return dateSimpleFormatter.format(this.sourceDate);
  }

  public void setSourceDate(Date sourceDate) {
    this.sourceDate = sourceDate;
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
  
  public void setSubjectOnlyPerson(boolean subjectOnlyPerson) {
    this.subjectOnlyPerson = subjectOnlyPerson;
  }
  
  public boolean isSubjectOnlyPerson() {
    return this.subjectOnlyPerson;
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
