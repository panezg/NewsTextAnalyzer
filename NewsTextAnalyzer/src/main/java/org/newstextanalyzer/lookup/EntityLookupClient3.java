package org.newstextanalyzer.lookup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class EntityLookupClient3 {
  public static final String URI = "http://myuri/";
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LOCAL_SPARQL_ENDPOINT = "<http://test1>";
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia-live.openlinksw.com/sparql>";
  private int count = 0;
  private String previousQuery = "";
  
  private static EntityLookupClient3 instance;

  public static EntityLookupClient3 getInstance() {
    if (instance == null) {
      instance = new EntityLookupClient3();
    }
    return instance;
  }

  public void run(Map<String, String> rawSubjectsInterlinked) {
    int n = 8;
    
    //Map<String, String> rawSubjectsInterlinked = new HashMap<>();
    /*
    rawSubjectsInterlinked = new HashMap<>();
    rawSubjectsInterlinked.put("Senator Elizabeth Warren", null);
    rawSubjectsInterlinked.put("Warren", null);
    rawSubjectsInterlinked.put("Ryanair", null);
    rawSubjectsInterlinked.put("Sanders", null);
    rawSubjectsInterlinked.put("Barack Obama’s press secretary", null);
    rawSubjectsInterlinked.put("George W Bush’s press secretary", null);
    rawSubjectsInterlinked.put("The White House Correspondents Association", null);
    rawSubjectsInterlinked.put("Trump’s freewheeling question-and-answer sessions , interviews and tweets", null);
    rawSubjectsInterlinked.put("Mike McCurry", null);
    rawSubjectsInterlinked.put("The former CBI chief", null);
    rawSubjectsInterlinked.put("Fowler", null);
    rawSubjectsInterlinked.put("Cummings", null);
    rawSubjectsInterlinked.put("Congress", null);
    rawSubjectsInterlinked.put("Lewis", null);
    rawSubjectsInterlinked.put("The Treasury", null);
    rawSubjectsInterlinked.put("Unilever , AA and Cineworld", null);
    */
    
    int partition = rawSubjectsInterlinked.size() / n;
    
    Retriever[] retrievers = new Retriever[n];
    List<String> keys = new ArrayList<>();
    for (String key : rawSubjectsInterlinked.keySet()) {
      keys.add(key);
    }
    
    int start = 0;
    int end = 0;
    for (int i = 0; i < n; i++) {
      if (i == n - 1) {
        end = rawSubjectsInterlinked.size();
      }
      else {
        end = start + partition;
      }
      retrievers[i] = new Retriever("Retriever" + i, keys, rawSubjectsInterlinked, start, end);
      start += partition;
    }
    for (Retriever retriever : retrievers) {
      retriever.start();
    }
    for (Retriever retriever : retrievers) {
      try {
        retriever.join();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
    }
    /*
    for (String key : rawSubjectsInterlinked.keySet()) {
      System.out.println(key + " -> " + rawSubjectsInterlinked.get(key));
    }*/
  }
}

class Retriever extends Thread implements Runnable {
  private List<String> keys;
  private Map<String, String> map;
  private int startIndex;
  private int endIndex;
  private String name;
  
  public Retriever(String name, List<String> keys, Map<String, String> map, int startIndex, int endIndex) {
    this.name = name;
    this.keys = keys;
    this.map = map;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }
  
  public void run() {
    int count = 0;
    for (int i = startIndex; i < endIndex; i++) {
      count++;
      if (count % 100 == 0 ) {
        System.out.println("Thread " + name + " count: " + count);
      }
      String resource = lookup(keys.get(i));
      if (resource != null) {
        map.put(keys.get(i), resource);
      }
    }
  }
  
  public String lookup(String rawSubject) {
    // String s = "SELECT * WHERE { ?s ?p ?o . } LIMIT 100";
    /*
     * count++; if (count % 100 == 0) { System.out.println(count); }
     */
    StringBuilder sb = new StringBuilder();
    rawSubject = rawSubject.replaceAll("[\\[\\]()\\']", "");
    // return null;
    /*
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("SELECT * WHERE { ");
    sb.append("?s ?p ?o . ");
    sb.append("?s rdfs:label ?l . ");
    sb.append("?s a <http://dbpedia.org/class/yago/Person100007846> . ");
    // sb.append("?s a <http://dbpedia.org/class/yago/Politician110450303> . ");
    sb.append("FILTER (regex(str(?l), \"" + rawSubject + "\")) ");
    sb.append("}");
    */
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

    // System.out.println(sb.toString());

    try {

      Query query = QueryFactory.create(sb.toString());

      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution qs = results.next();
        // System.out.println(qs.get("o"));
        return qs.get("s").toString();
      }

      // ResultSetFormatter.out(System.out, results, query);
      qexec.close();
    } catch (QueryException qe) {
      qe.printStackTrace();
      
      //System.out.println("Current Query: " + sb.toString());
      throw qe;
    }
    return null;
  }
}