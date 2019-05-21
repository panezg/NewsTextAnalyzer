package org.newstextanalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.base.CaseFormat;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

public class VirtuosoPersistor implements IPipelineStep {
  private VirtGraph set;
  // some definitions
  static String personURI = "http://somewhere/JohnSmith";
  static String fullName = "John Smith";

  private String URI = "http://myuri/";
  private Map<String, Integer> predicateRoots;

  public VirtuosoPersistor() {
    predicateRoots = new HashMap<>();
    set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");
    String str = "CLEAR GRAPH <http://test1>";
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];

    for (TripleWrapper tripleWrapper : triplesWrapper) {
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
      }
      else {
        object = triple.getArgument2().toString();
      }
      
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO GRAPH <http://test1> { ");
      sb.append("<");
      sb.append(fqSubject);
      sb.append("> <");
      sb.append(fqPredicate);
      sb.append("> \"");
      sb.append(object.replace("’", ""));
      sb.append("\" . <");
      sb.append(fqPredicate);
      sb.append("> <");
      sb.append(URI);
      sb.append("singletonPropertyOf> <");
      sb.append(URI);
      sb.append(predicateRoot);
      sb.append("> . <");
      sb.append(fqPredicate);
      sb.append("> <");
      sb.append(URI);
      sb.append("hasSource> \"");
      sb.append(tripleWrapper.getSource());
      sb.append("\" . <");
      sb.append(fqPredicate);
      sb.append("> <");
      sb.append(URI);
      sb.append("date> \"");
      sb.append(tripleWrapper.getDate());

      sb.append("\" . <");
      sb.append(fqPredicate);
      sb.append("> <");
      sb.append(URI);
      sb.append("score> ");
      sb.append(tripleWrapper.getScore());
      sb.append(" . }");
      // System.out.println(sb.toString());
      VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(sb.toString(), set);
      vur.exec();
    }
    return null;
  }

  public String toCamelCaseURIReady(String s, boolean upper) {
    String s2 = s.replace("’", "").replace("|", "").replace(' ', '_');
    return upper ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s2)
        : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s2);
  }

  public static void foo() {
    System.out.println("\nexecute: SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
    Query sparql = QueryFactory.create("SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");

    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql,
        new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba"));
    ResultSet results = vqe.execSelect();
    while (results.hasNext()) {
      QuerySolution rs = results.nextSolution();
      RDFNode s = rs.get("s");
      RDFNode p = rs.get("p");
      RDFNode o = rs.get("o");
      System.out.println(" { " + s + " " + p + " " + o + " . }");
    }
  }

  @Override
  public void clean() {
    return;
  }

  @Override
  public Type getType() {
    return Type.PERSISTOR;
  }
}
