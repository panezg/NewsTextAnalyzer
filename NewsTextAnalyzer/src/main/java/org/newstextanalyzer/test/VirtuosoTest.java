package org.newstextanalyzer.test;

import org.apache.jena.ext.com.google.common.base.CaseFormat;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

public class VirtuosoTest {
  // some definitions
  static String personURI = "http://somewhere/JohnSmith";
  static String fullName = "John Smith";

  public static void run() {
    // create an empty Model
    Model model = ModelFactory.createDefaultModel();

    // create the resource
    Resource johnSmith = model.createResource(personURI);

    // add the property
    johnSmith.addProperty(VCARD.FN, fullName);

    // Virtuoso adapter
    // --- Example of read

    /* STEP 1 */
    VirtGraph set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");

    /* STEP 2 */

    /* STEP 3 */
    /* Select all data in virtuoso */
    Query sparql = QueryFactory.create("SELECT * WHERE { GRAPH ?graph { ?s ?p ?o } } limit 100");

    /* STEP 4 */
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);

    ResultSet results = vqe.execSelect();
    while (results.hasNext()) {
      QuerySolution result = results.nextSolution();
      RDFNode graph = result.get("graph");
      RDFNode s = result.get("s");
      RDFNode p = result.get("p");
      RDFNode o = result.get("o");
      System.out.println(graph + " { " + s + " " + p + " " + o + " . }");
    }

    // --- Example of write

    /* STEP 1 */
    set = set;

    /* STEP 2 */
    System.out.println("\nexecute: CLEAR GRAPH <http://test1>");
    String str = "CLEAR GRAPH <http://test1>";
    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();

    System.out.println("\nexecute: INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }");
    str = "INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }";
    vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();

    /* STEP 3 */
    /* Select all data in virtuoso */
    System.out.println("\nexecute: SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
    sparql = QueryFactory.create("SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");

    /* STEP 4 */
    vqe = VirtuosoQueryExecutionFactory.create(sparql, set);

    results = vqe.execSelect();
    while (results.hasNext()) {
      QuerySolution rs = results.nextSolution();
      RDFNode s = rs.get("s");
      RDFNode p = rs.get("p");
      RDFNode o = rs.get("o");
      System.out.println(" { " + s + " " + p + " " + o + " . }");
    }

    System.out.println("\nexecute: DELETE FROM GRAPH <http://test1> { <aa> <bb> 'cc' }");
    str = "DELETE FROM GRAPH <http://test1> { <aa> <bb> 'cc' }";
    vur = VirtuosoUpdateFactory.create(str, set);
    vur.exec();

    System.out.println("\nexecute: SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
    vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
    results = vqe.execSelect();
    while (results.hasNext()) {
      QuerySolution rs = results.nextSolution();
      RDFNode s = rs.get("s");
      RDFNode p = rs.get("p");
      RDFNode o = rs.get("o");
      System.out.println(" { " + s + " " + p + " " + o + " . }");
    }
  }
  
  public static void foo() {
    /* STEP 3 */
    /* Select all data in virtuoso */
    
    System.out.println(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, "this_is_an_Example"));
    
    System.out.println("\nexecute: SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
    Query sparql = QueryFactory.create("SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
    VirtuosoQueryExecution vqe;
    
    /* STEP 4 */
    VirtGraph set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");
    vqe = VirtuosoQueryExecutionFactory.create(sparql, set);

    ResultSet results = vqe.execSelect();
    while (results.hasNext()) {
      QuerySolution rs = results.nextSolution();
      RDFNode s = rs.get("s");
      RDFNode p = rs.get("p");
      RDFNode o = rs.get("o");
      System.out.println(" { " + s + " " + p + " " + o + " . }");
    }
  }
  
  public static void main(String[] args) {
    foo();
    System.out.println("done");
  }
}