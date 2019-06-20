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
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LOCAL_SPARQL_ENDPOINT = "<http://test1>";
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia-live.openlinksw.com/sparql>";
  

  private static VirtuosoClient instance;

  private VirtGraph set;
  private Map<String, Integer> predicateRoots;
  private int owlTimeCountInstance;

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
    String str = "CLEAR GRAPH ".concat(LOCAL_SPARQL_ENDPOINT);
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();
  }

  public void saveTriple(TripleWrapper tripleWrapper) {
    ChunkedBinaryExtraction triple = tripleWrapper.getTriple();
    // Node foo1 = NodeFactory.createURI("http://test1/#" + triple.getArgument1());
    // Node bar1 = NodeFactory.createURI("http://test1/#" + triple.getRelation());
    // Node baz1 = NodeFactory.createURI("http://test1/#" + triple.getArgument2());
    // set.add(new Triple(foo1, bar1, baz1));

    // triple.getArgument1().toString().replace("’", "")
    // NOTE: <> is for ids or uris, if spaces throw error
    // String insertData = "INSERT INTO GRAPH <http://test1> { " + "foo fof" +
    // "> vcard2006:hasAddress <" + "hello" + "> . }";
    // String insertData = "INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' .
    // <aa1> <bb1> 123. }";
    // System.out.println(insertData);
    // VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(insertData, set);
    // vur.exec();

    // "INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }";

    // NOTE: Calculating the subject
    StringBuilder sbTemp = new StringBuilder();
    String finalSubject = tripleWrapper.getSubjectReplacement() != null ? tripleWrapper.getSubjectReplacement().group().toString() : triple.getArgument1().toString();
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
      sbTemp.append("m:").append(toCamelCaseURIReady(triple.getArgument2().toString(), true));
      object = sbTemp.toString();
    } else {
      object = triple.getArgument2().toString();
    }

    // NOTE: Calculating insert SPARQL statement
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX m: <").append(URI).append("> ");
    sb.append("PREFIX time: <").append(OWL_TIME_URI).append("> ");
    sb.append("PREFIX tz-w: <").append(WORLD_TIME_ZONE_URI).append("> ");

    sb.append("INSERT INTO GRAPH ").append(LOCAL_SPARQL_ENDPOINT).append(" { ");
    sb.append(subject).append(" ").append(predicate).append(" ");
    if (tripleWrapper.isObjectMatched()) {
      sb.append(object).append(" . ");
    } else {
      sb.append("\"").append(object.replace("\"", "")).append("\" . ");
    }
    // Predicate list for predicate via ideification
    // Ideification - predicate root
    sb.append(predicate).append(" m:singletonPropertyOf ").append("<").append(URI).append(predicateRoot).append("> ; ");
    // Ideification - predicate source
    sb.append("m:hasSource ").append("\"").append(tripleWrapper.getSource()).append("\" ; ");
    // Ideification - predicate date
    boolean consistentDate = true;
    Date instantDate = null;
    try {
      // NOTE: Check if it is a consistent date by trying to parse it
      instantDate = new SimpleDateFormat("yyyy-MM-dd").parse(tripleWrapper.getExtractedDate());
    } catch (ParseException pe) {
      consistentDate = false;
    }
    if (consistentDate) {
      sb.append("m:xsdDate ").append("\"").append(tripleWrapper.getExtractedDate()).append("\"^^xsd:date ; ");
    } else {
      sb.append("m:strDate ").append("\"").append(tripleWrapper.getExtractedDate()).append("\" ; ");
    }
    // Ideification - predicate score
    sb.append("m:confidenceScore ").append(tripleWrapper.getScoreAsString()).append(" ; ");
    // Ideification - predicate raw text
    sb.append("m:rawSubject ").append("\"").append(triple.getArgument1().toString()).append("\" ; ");
    // Ideification - predicate raw predicate
    sb.append("m:rawPredicate ").append("\"").append(triple.getRelation().toString()).append("\" ; ");
    // Ideification - predicate raw object
    sb.append("m:rawObject ").append("\"").append(triple.getArgument2().toString()).append("\" ; ");
    // Ideification - triple token list
    sb.append("m:tokenList ").append("\"").append(triple.getArgument1().toString()).append(" ")
        .append(triple.getRelation().toString()).append(" ").append(triple.getArgument2().toString()).append("\" . ");

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
    sb.append("}");

    //System.out.println(sb.toString());
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  public String toCamelCaseURIReady(String s, boolean upper) {
    String s2 = s.replace("’", "").replace("|", "").replace(' ', '_');
    return upper ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s2)
        : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s2);
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
  public ResultSet queryAll() {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX m: <").append(URI).append("> ");
    sb.append("PREFIX time: <").append(OWL_TIME_URI).append("> ");
    sb.append("SELECT * FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ?s ?p ?o . ");
    sb.append("?p m:rawSubject ?rs ; ");
    sb.append("m:rawPredicate ?rp ; ");
    sb.append("m:rawObject ?ro ; ");
    sb.append("m:tokenList ?tl ; ");
    sb.append("m:hasSource ?hs ; ");
    sb.append("time:inDateTime ?idt ; ");
    sb.append("m:xsdDate ?xd . } ");
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
      StringBuilder sb = new StringBuilder();
      sb.append("WITH ").append(LOCAL_SPARQL_ENDPOINT).append(" DELETE WHERE { ")
          // NOTE: .asResource() already provides URI
          .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
          .append("hasTweetSource> ").append("?ts").append(" . } ");
      VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
      vur.exec();
    }
    insertTweetSource(predicate, tweetSource);
  }

  private boolean isTweetSourceAvailableAlready(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("hasTweetSource> ?ts . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertTweetSource(RDFNode predicate, String tweetSource) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH ").append(LOCAL_SPARQL_ENDPOINT).append(" { ");
    sb.append("<").append(predicate.asResource().toString()).append("> ");
    sb.append("<").append(URI).append("hasTweetSource> \"").append(tweetSource).append("\" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  // NOTE: Sentiment score SPARQL ops
  public void updateSentimentScore(RDFNode predicate, Double sentimentScore) {
    if (isSentimentScoreAvailableAlready(predicate)) {
      StringBuilder sb = new StringBuilder();
      sb.append("WITH ").append(LOCAL_SPARQL_ENDPOINT).append(" DELETE WHERE { ")
          // NOTE: .asResource() already provides URI
          .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
          .append("sentimentScore> ").append("?ss").append(" . } ");
      VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
      vur.exec();
    }
    insertSentimentScore(predicate, sentimentScore);
  }

  private boolean isSentimentScoreAvailableAlready(RDFNode predicate) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("sentimentScore> ?ss . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertSentimentScore(RDFNode predicate, Double sentimentScore) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH ").append(LOCAL_SPARQL_ENDPOINT).append(" { ");
    sb.append("<").append(predicate.asResource().toString()).append("> ");
    sb.append("<").append(URI).append("sentimentScore> ").append(sentimentScore).append(" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
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
          sb.append("WITH ").append(LOCAL_SPARQL_ENDPOINT).append(" DELETE WHERE { ");
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
    sb.append("SELECT * FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
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
    sb.append("INSERT INTO GRAPH ").append(LOCAL_SPARQL_ENDPOINT).append(" { ");
    sb.append("<").append(subject).append("> ");
    sb.append("owl:sameAs ").append("<").append(interlinkURI).append("> . }");
    //System.out.println(sb.toString());
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }
  
  // NOTE: Queries for Questioner
  
  public void q() {}
}
