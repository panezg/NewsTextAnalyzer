package org.newstextanalyzer.virtuoso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class VirtuosoClientOld {
  public static final String URI = "http://myuri/";

  private static VirtuosoClientOld instance;

  private VirtGraph set;
  private Map<String, Integer> predicateRoots;

  public static VirtuosoClientOld getInstance() {
    if (instance == null) {
      instance = new VirtuosoClientOld();
    }
    return instance;
  }

  private VirtuosoClientOld() {
    set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");
    predicateRoots = new HashMap<>();
  }

  public void clean() {
    String str = "CLEAR GRAPH <http://test1>";
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

    StringBuilder sbTemp = new StringBuilder();
    sbTemp.append(URI);
    sbTemp.append(toCamelCaseURIReady(triple.getArgument1().toString(), true));
    String fqSubject = sbTemp.toString();

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
    sbTemp.append(URI);
    sbTemp.append(predicateRoot);
    sbTemp.append("#");
    sbTemp.append(count);
    String fqPredicate = sbTemp.toString();

    String object = "";
    if (tripleWrapper.isObjectMatched()) {
      sbTemp = new StringBuilder();
      sbTemp.append(URI);
      sbTemp.append(toCamelCaseURIReady(triple.getArgument2().toString(), true));
      object = sbTemp.toString();
    } else {
      object = triple.getArgument2().toString();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH <http://test1> { ").append("<").append(fqSubject).append("> ").append("<")
        .append(fqPredicate).append("> ");
    if (tripleWrapper.isObjectMatched()) {
      sb.append("<").append(object).append("> . ");
    } else {
      sb.append("\"").append(object.replace("\"", "")).append("\" . ");
    }
    // Ideification - predicate root
    sb.append("<").append(fqPredicate).append("> ").append("<").append(URI).append("singletonPropertyOf> ").append("<")
        .append(URI).append(predicateRoot).append("> . ")
        // Ideification - predicate source
        .append("<").append(fqPredicate).append("> ").append("<").append(URI).append("hasSource> ").append("\"")
        .append(tripleWrapper.getSource()).append("\" . ")
        // Ideification - predicate date
        .append("<").append(fqPredicate).append("> ");
    try {
      // NOTE: Don't need assignment since I only require to check if it is
      // consistent date
      new SimpleDateFormat("yyyy-MM-dd").parse(tripleWrapper.getExtractedDate());
      sb.append("<").append(URI).append("xsdDate> ");
      sb.append("\"").append(tripleWrapper.getExtractedDate()).append("\"^^xsd:date . ");
    } catch (ParseException pe) {
      sb.append("<").append(URI).append("strDate> ");
      sb.append("\"").append(tripleWrapper.getExtractedDate()).append("\" . ");
    }
    // Ideification - predicate score
    sb.append("<").append(fqPredicate).append("> ").append("<").append(URI).append("confidenceScore> ")
        .append(tripleWrapper.getScoreAsString()).append(" . ")
        // Ideification - predicate raw text
        .append("<").append(fqPredicate).append("> ").append("<").append(URI).append("rawSubject> ").append("\"")
        .append(triple.getArgument1().toString()).append("\" . ")
        // Ideification - predicate raw predicate
        .append("<").append(fqPredicate).append("> ").append("<").append(URI).append("rawPredicate> ").append("\"")
        .append(triple.getRelation().toString()).append("\" . ")
        // Ideification - predicate raw object
        .append("<").append(fqPredicate).append("> ").append("<").append(URI).append("rawObject> ").append("\"")
        .append(triple.getArgument2().toString()).append("\" . ")
        // Ideification - triple token list
        .append("<").append(fqPredicate).append("> ").append("<").append(URI).append("tokenList> ").append("\"")
        .append(triple.getArgument1().toString()).append(" ").append(triple.getRelation().toString()).append(" ")
        .append(triple.getArgument2().toString()).append("\" . }");

    // System.out.println(sb.toString());
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  public String toCamelCaseURIReady(String s, boolean upper) {
    String s2 = s.replace("’", "").replace("|", "").replace(' ', '_');
    return upper ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s2)
        : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s2);
  }

  // NOTE: Only retrieving those triples that have dates as xds:date
  public ResultSet queryAll() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM <http://test1> WHERE { ?s ?p ?o . ").append("?p <").append(URI)
        .append("rawSubject> ?rs . ").append("?p <").append(URI).append("rawPredicate> ?rp . ").append("?p <")
        .append(URI).append("rawObject> ?ro . ").append("?p <").append(URI).append("tokenList> ?tl . ").append("?p <")
        .append(URI).append("xsdDate> ?d . ").append("?p <").append(URI).append("hasSource> ?hs . ")
        .append("FILTER (datatype(?d) = xsd:date) . } ");

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
      sb.append("WITH <http://test1> ").append("DELETE WHERE { ")
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
    sb.append("SELECT * FROM <http://test1> WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("hasTweetSource> ?ts . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertTweetSource(RDFNode predicate, String tweetSource) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH <http://test1> { ").append("<").append(predicate.asResource().toString()).append("> ")
        .append("<").append(URI).append("hasTweetSource> \"").append(tweetSource).append("\" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }

  // NOTE: Sentiment score SPARQL ops
  public void updateSentimentScore(RDFNode predicate, Double sentimentScore) {
    if (isSentimentScoreAvailableAlready(predicate)) {
      StringBuilder sb = new StringBuilder();
      sb.append("WITH <http://test1> ").append("DELETE WHERE { ")
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
    sb.append("SELECT * FROM <http://test1> WHERE { ")
        // NOTE: .asResource() already provides URI
        .append("<").append(predicate.asResource().toString()).append("> ").append("<").append(URI)
        .append("sentimentScore> ?ss . }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    return results.hasNext();
  }

  private void insertSentimentScore(RDFNode predicate, Double sentimentScore) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO GRAPH <http://test1> { ").append("<").append(predicate.asResource().toString()).append("> ")
        .append("<").append(URI).append("sentimentScore> ").append(sentimentScore).append(" . }");

    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
    vur.exec();
  }
}
