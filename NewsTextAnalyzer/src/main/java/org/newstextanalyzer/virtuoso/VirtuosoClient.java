package org.newstextanalyzer.virtuoso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ext.com.google.common.base.CaseFormat;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.newstextanalyzer.pipeline.TripleWrapper;
import org.newstextanalyzer.sentiment.Tweet;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

public class VirtuosoClient {
  public static final String URI = "http://myuri/";
  public static final String DBO_URI = "http://dbpedia.org/ontology/";
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LOCAL_GRAPH = "<http://test1>";

  private static VirtuosoClient instance;

  private VirtGraph set;
  private Map<String, Integer> predicateRoots;
  private int owlTimeCountInstance;
  
  private char[] oldChars = new char[5];

  public static VirtuosoClient getInstance() {
    if (instance == null) {
      instance = new VirtuosoClient();
    }
    return instance;
  }

  private VirtuosoClient() {
    set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");
    predicateRoots = new HashMap<>();
    owlTimeCountInstance = 1;
  }

  public void clean() {
    String str = "CLEAR GRAPH ".concat(LOCAL_GRAPH);
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();
  }

  public void saveTriple(TripleWrapper tripleWrapper) {
    ChunkedBinaryExtraction triple = tripleWrapper.getTriple();

    // NOTE: Calculating the subject
    StringBuilder sbTemp = new StringBuilder();
    String finalSubject = tripleWrapper.getSubjectReplacement() != null
        ? tripleWrapper.getSubjectReplacement().group().toString()
        : triple.getArgument1().toString();
    sbTemp.append("<").append(URI).append(toCamelCaseURIReady(finalSubject, true)).append(">");
    String subject = sbTemp.toString();

    // NOTE: Calculating main predicate
    String predicateRoot = toCamelCaseURIReady(triple.getRelation().toString(), false);
    int count = 0;
    if (predicateRoots.containsKey(predicateRoot)) {
      count = predicateRoots.get(predicateRoot);
      count++;
    } else {
      count = 1;
    }
    
    predicateRoots.put(predicateRoot, count);
    sbTemp = new StringBuilder();
    sbTemp.append("<").append(URI).append(predicateRoot).append("#").append(count).append(">");
    String predicate = sbTemp.toString();

    // NOTE: Calculating the main object
    String object = "";
    if (tripleWrapper.isObjectMatched()) {
      sbTemp = new StringBuilder();
      //sbTemp.append("<").append(URI).append(toCamelCaseURIReady(triple.getArgument2().toString().replace("'", "\'"), true)).append(">");
      sbTemp.append("<").append(URI).append(toCamelCaseURIReady(triple.getArgument2().toString(), true)).append(">");
      object = sbTemp.toString();
    } else {
      object = removeOrEscapeString(triple.getArgument2().toString());
    }

    // NOTE: Calculating insert SPARQL statement
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX m: <").append(URI).append("> ");
    // TODO: Pending check
    sb.append("PREFIX dbo: <").append(DBO_URI).append("> ");
    sb.append("PREFIX time: <").append(OWL_TIME_URI).append("> ");
    sb.append("PREFIX tz-w: <").append(WORLD_TIME_ZONE_URI).append("> ");

    sb.append("INSERT INTO GRAPH ").append(LOCAL_GRAPH).append(" { ");
    sb.append(subject).append(" ").append(predicate).append(" ");
    if (tripleWrapper.isObjectMatched()) {
      sb.append(object).append(" . ");
    } else {
      //sb.append("\"").append(object.replace("\"", "")).append("\" . ");
      sb.append("\"").append(object).append("\" . ");
    }
    // Predicate list for predicate via ideification
    // Ideification - predicate root
    sb.append(predicate).append(" m:singletonPropertyOf ").append("<").append(URI).append(predicateRoot).append("> ; ");
    // Ideification - predicate source
    sb.append("m:hasSource ").append("\"").append(tripleWrapper.getSource()).append("\" ; ");
    // Ideification - predicate source date
    sb.append("m:xsdSourceDate ").append("\"").append(tripleWrapper.getSourceDateAsString()).append("\"^^xsd:date ; ");
    
    // Ideification - predicate date
    boolean consistentDate = true;
    Date instantDate = null;
    if (tripleWrapper.getExtractedDate() != null) {
      try {
        // NOTE: Check if it is a consistent date by trying to parse it
        instantDate = new SimpleDateFormat("yyyy-MM-dd").parse(tripleWrapper.getExtractedDate());
      } catch (ParseException pe) {
        consistentDate = false;
      }
      if (consistentDate) {
        sb.append("m:xsdDate ").append("\"").append(tripleWrapper.getExtractedDate()).append("\"^^xsd:date ; ");
        // TODO: Pending check
        sb.append("a dbo:Event ; ");
      } else {
        sb.append("m:strDate ").append("\"").append(tripleWrapper.getExtractedDate()).append("\" ; ");
      }
    }
    // Ideification - predicate score
    sb.append("m:confidenceScore ").append(tripleWrapper.getScoreAsString()).append(" ; ");
    // Ideification - predicate raw text
    String rawSubject = removeOrEscapeString(triple.getArgument1().toString());
    sb.append("m:rawSubject ").append("\"").append(rawSubject).append("\" ; ");
    // Ideification - predicate raw predicate
    String rawPredicate = removeOrEscapeString(triple.getRelation().toString());
    sb.append("m:rawPredicate ").append("\"").append(rawPredicate).append("\" ; ");
    // Ideification - predicate raw object    
    String rawObject = removeOrEscapeString(triple.getArgument2().toString());
    sb.append("m:rawObject ").append("\"").append(rawObject).append("\" ; ");
    // Ideification - triple token list
    StringBuilder sbTokenList = new StringBuilder();
    sbTokenList.append(triple.getArgument1().toString()).append(" ").append(triple.getRelation().toString()).append(" ").append(triple.getArgument2().toString());
    String tokenList = removeOrEscapeString(sbTokenList.toString());
    sb.append("m:tokenList ").append("\"").append(tokenList).append("\" ; ");
    String hasSourceSentence = removeOrEscapeString(triple.getSentence().toString());
    sb.append("m:hasSourceSentence ").append("\"").append(hasSourceSentence).append("\" . ");

    if (tripleWrapper.getExtractedDate() != null) {
      if (consistentDate) {
        // OWL-Time
        // NOTE: More info on
        // https://www.w3.org/TR/2006/WD-owl-time-20060927/#ref-datetime
        sb.append(predicate).append(" a ").append("time:Instant ; ");
        Calendar cal = Calendar.getInstance();
        cal.setTime(instantDate);
        String tempDesRes = "m:temporalDescription" + owlTimeCountInstance;
        sb.append("time:inDateTime ").append(tempDesRes).append(" . ");
        sb.append(tempDesRes).append(" a ").append("time:DateTimeDescription ; ");
        sb.append("time:unitType ").append("time:unitDay ; ");
        sb.append("time:day ").append(cal.get(Calendar.DAY_OF_MONTH)).append(" ; ");
        sb.append("time:dayOfWeek ").append("time:").append(getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))).append(" ; ");
        sb.append("time:dayOfYear ").append(cal.get(Calendar.DAY_OF_YEAR)).append(" ; ");
        sb.append("time:week ").append(cal.get(Calendar.WEEK_OF_YEAR)).append(" ; ");
        // NOTE: Calendar returns 0 for January, so + 1
        sb.append("time:month ").append(cal.get(Calendar.MONTH) + 1).append(" ; ");
        sb.append("time:year ").append(cal.get(Calendar.YEAR)).append(" ; ");
        // NOTE: Check http://www.w3.org/2006/timezone-world for review of timezones
        sb.append("time:timeZone ").append("tz-w:ZTZ . ");
        owlTimeCountInstance++;
      }
    }
    sb.append("}");

    try {
      VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
      vur.exec();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(sb.toString());
      throw new RuntimeException();
    }
  }

  public String toCamelCaseURIReady(String s, boolean upper) {
    //String s2 = s.replace("’", "").replace("|", "").replace(",", "").replace(" ","_").replace("\"", "");
    //String s2 = s.replace(" ", "_").replace("\"", "").replace("`", "").replace("'", "").replace("|", "");
    
    final int length = s.length();
    if (oldChars.length < length) {
      oldChars = new char[length];
    }
    s.getChars(0, length, oldChars, 0);
    int newLen = 0;
    for (int j = 0; j < length; j++) {
      char ch = oldChars[j];
      //<>#|\^[]{};/?:@&=+$,
      // NOTE: Converts spaces to underscores to leverage case format
      // Removes quotes to not break the SPARQL query
      // Removes not allowed characters to prevent invalid URLs
      // Pending adding % $ + =
      if (ch == ' ' ) {
        oldChars[newLen] = '_';
        newLen++;
      } else if (!(ch == '"' || ch == '`' || ch == '\'' || ch == '|' || ch == '<' || ch == '>' || ch == '#'
          || ch == '\\' || ch == '^' || ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == ';' || ch == '/'
          || ch == '?' || ch == ':' || ch == '@' || ch == '&' || ch == ',')) {
        oldChars[newLen] = ch;
        newLen++;
      }
    }
    String s2 = new String(oldChars, 0, newLen);
    return upper ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s2)
        : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s2);
  }
  
  // NOTE: To make string safes, not URL safe
  public String removeOrEscapeString(String s) {
    //String s2 = s.replace("’", "").replace("|", "").replace(",", "").replace(" ",  "_").replace("\"", "");
    // NOTE: Removes quotes to avoid breaking SPARQL command
    String s2 = s.replace("\"", "");
    return s2;
  }

  private String getDayOfWeek(int dayOfWeekIndex) {
    switch (dayOfWeekIndex) {
      case 1:
        return "Sunday";
      case 2:
        return "Monday";
      case 3:
        return "Tuesday";
      case 4:
        return "Wednesday";
      case 5:
        return "Thursday";
      case 6:
        return "Friday";
      case 7:
        return "Saturday";
      default:
        return null;
    }
  }

  // NOTE: Only retrieving those triples that have dates as xds:date
  // NOTE: Used to detect which triples should try to be enriched with sentiment data
  public ResultSet queryAll() {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX m: <").append(URI).append("> ");
    sb.append("PREFIX time: <").append(OWL_TIME_URI).append("> ");
    sb.append("SELECT * FROM ").append(LOCAL_GRAPH).append(" WHERE { ?s ?p ?o . ");
    sb.append("?p m:rawSubject ?rs ; ");
    sb.append("m:rawPredicate ?rp ; ");
    sb.append("m:rawObject ?ro ; ");
    sb.append("m:tokenList ?tl ; ");
    sb.append("m:hasSource ?hs ; ");
    //sb.append("time:inDateTime ?idt ; ");
    sb.append("m:xsdSourceDate ?xsd . } ");
    //System.out.println(sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results;
  }

  public void updateWithTweetInfo(RDFNode predicate, Tweet tweet) {
    updateTweetSource(predicate, tweet.getURL());
    updateSentimentScore(predicate, tweet.getAvgSentimentScore());
  }

  // NOTE: Tweet source SPARQL ops
  public void updateTweetSource(RDFNode predicate, String tweetSource) {
    if (isTweetSourceAvailableAlready(predicate)) {
      deleteTweetSource(predicate);
    }
    insertTweetSource(predicate, tweetSource);
  }
  
  // NOTE: Tweet source SPARQL ops
  private void deleteTweetSource(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("WITH ").append(LOCAL_GRAPH).append(" DELETE WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("hasTweetSource> ").append("?ts").append(" . } ");
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  private boolean isTweetSourceAvailableAlready(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM ").append(LOCAL_GRAPH).append(" WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("hasTweetSource> ?ts . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertTweetSource(RDFNode predicate, String tweetSource) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH ").append(LOCAL_GRAPH).append(" { ");
    sb.append("<").append(predicate.asResource().toString()).append("> ");
    sb.append("<").append(URI).append("hasTweetSource> \"").append(tweetSource).append("\" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  // NOTE: Sentiment score SPARQL ops
  public void updateSentimentScore(RDFNode predicate, Double sentimentScore) {
    if (isSentimentScoreAvailableAlready(predicate)) {
      deleteSentimentScore(predicate);
    }
    insertSentimentScore(predicate, sentimentScore);
  }

  private void deleteSentimentScore(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("WITH ").append(LOCAL_GRAPH).append(" DELETE WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("sentimentScore> ").append("?ss").append(" . } ");
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }
  
  private boolean isSentimentScoreAvailableAlready(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM ").append(LOCAL_GRAPH).append(" WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("sentimentScore> ?ss . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertSentimentScore(RDFNode predicate, Double sentimentScore) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH ").append(LOCAL_GRAPH).append(" { ");
    sb.append("<").append(predicate.asResource().toString()).append("> ");
    sb.append("<").append(URI).append("sentimentScore> ").append(sentimentScore).append(" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }
  
  public void deleteTweetInfo(RDFNode predicate) {
    if (isTweetSourceAvailableAlready(predicate)) {
      deleteTweetSource(predicate);
    }
    if (isSentimentScoreAvailableAlready(predicate)) {
      deleteSentimentScore(predicate);
    }
  }
  
  /**
   * Inserts or update owl:sameAs triples for the subjects that were successfully
   * interlinked by the interlinker within the pipeline
   * 
   * @param rawSubjectsInterlinked
   */
  public void updateInterlinks(Map<String, String> rawSubjectsInterlinked) {
    for (String rawSubject : rawSubjectsInterlinked.keySet()) {
      String interlinkURI = rawSubjectsInterlinked.get(rawSubject);
      if (interlinkURI != null) {
        String subject = getURIfromRawSubject(rawSubject);
        if (isInterlinkAvailableAlready(subject)) {
          StringBuilder sb = new StringBuilder();
          sb.append("PREFIX owl: <").append(OWL_URI).append("> ");
          sb.append("WITH ").append(LOCAL_GRAPH).append(" DELETE WHERE { ");
          sb.append("<").append(subject).append("> ");
          sb.append("owl:sameAs ?sa . } ");
          VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
          vur.exec();
        }
        insertInterlink(subject, interlinkURI);
      }
    }
  }
  
  private boolean isInterlinkAvailableAlready(String subject) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX owl: <").append(OWL_URI).append("> ");
    sb.append("SELECT * FROM ").append(LOCAL_GRAPH).append(" WHERE { ");
    sb.append("<").append(subject).append("> ");
    sb.append("owl:sameAs ?sa . }");
    //System.out.println(sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }
  
  private String getURIfromRawSubject(String rawSubject) {
    StringBuilder sbTemp = new StringBuilder();
    sbTemp.append(URI).append(toCamelCaseURIReady(rawSubject, true));
    return sbTemp.toString();
  }
  
  private void insertInterlink(String subject, String interlinkURI) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX owl: <").append(OWL_URI).append("> ");
    sb.append("INSERT INTO GRAPH ").append(LOCAL_GRAPH).append(" { ");
    sb.append("<").append(subject).append("> ");
    sb.append("owl:sameAs ").append("<").append(interlinkURI).append("> . }");
    //System.out.println(sb.toString());
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }
}
