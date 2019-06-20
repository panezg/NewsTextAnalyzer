package org.newstextanalyzer.lookup;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class EntityLookupClient2 {
  public static final String URI = "http://myuri/";
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LOCAL_SPARQL_ENDPOINT = "<http://test1>";
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia-live.openlinksw.com/sparql>";
  private int count = 0;
  private String previousQuery = "";
  
  private static EntityLookupClient2 instance;

  public static EntityLookupClient2 getInstance() {
    if (instance == null) {
      instance = new EntityLookupClient2();
    }
    return instance;
  }

  public String lookup(String rawSubject) {
    //String s = "SELECT * WHERE { ?s ?p ?o . } LIMIT 100";
    count++;
    if (count % 100 == 0) {
      System.out.println(count);
    }
    StringBuilder sb = new StringBuilder();
    rawSubject = rawSubject.replaceAll("[\\[\\]()\\']", "");
    return null;
    /*
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("SELECT * WHERE { ");
    sb.append("?s ?p ?o . ");
    sb.append("?s rdfs:label ?l . ");
    sb.append("?s a <http://dbpedia.org/class/yago/Person100007846> . ");
    //sb.append("?s a <http://dbpedia.org/class/yago/Politician110450303> . ");
    sb.append("FILTER (regex(str(?l), \"" + rawSubject + "\")) ");
    sb.append("}");
    */
    
    /*
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("SELECT ?s (count(?s) as ?c) WHERE { ");
    sb.append("?s a ?t . ");
    sb.append("?s rdfs:label ?l . ");
    sb.append("?s a <http://dbpedia.org/class/yago/Person100007846> . ");
    sb.append("FILTER (regex(str(?l), \"" + rawSubject + "\")) ");
    sb.append("FILTER (?t IN (<http://dbpedia.org/class/yago/Politician110450303>, <http://umbel.org/umbel/rc/Politician>, <http://dbpedia.org/ontology/Politician>, <http://dbpedia.org/class/yago/Person100007846>)) ");
    sb.append("} ");
    sb.append("GROUP BY ?s ");
    sb.append("ORDER BY DESC(?c)");
    */
    //System.out.println(sb.toString());
    
    /*
    try {
      
      Query query = QueryFactory.create(sb.toString());
      
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);
  
      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution qs = results.next();
        //System.out.println(qs.get("o"));
        return qs.get("s").toString();
      }
      
      //ResultSetFormatter.out(System.out, results, query);
      qexec.close();
      previousQuery = sb.toString();
    } catch (QueryException qe) {
      qe.printStackTrace();
      System.out.println("Previous Query: " + previousQuery);
      System.out.println("Current Query: " + sb.toString());
      throw qe;
    }
    return null;*/
  }
  
  /*
  public static void main(String[] args) {
    EntityLookupClient2 elc2 = new EntityLookupClient2();
    elc2.lookup("Corbyn");
  }
  */
}
