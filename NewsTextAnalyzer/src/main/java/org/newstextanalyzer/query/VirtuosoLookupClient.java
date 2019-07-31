package org.newstextanalyzer.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

public class VirtuosoLookupClient {
  public static final String URI = "http://myuri/";
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia-live.openlinksw.com/sparql>";
  public static final String DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia.org/sparql>";
  
  private static VirtuosoLookupClient instance;

  public static VirtuosoLookupClient getInstance() {
    if (instance == null) {
      instance = new VirtuosoLookupClient();
    }
    return instance;
  }

  private VirtuosoLookupClient() {
  }
  
  public List<Map<String, String>> lookupPersonByName(String personName) {
    String query = ""
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + "PREFIX dbo: <http://dbpedia.org/ontology/> "
        + "SELECT ?s ?l (COUNT(?inl) as ?c) WHERE { "
        + "?s a <http://dbpedia.org/class/yago/Person100007846> . "
        + "?s rdfs:label ?l . "
        + "?inl ?p ?s . "
        + "FILTER (regex(?l, \"" + personName + "\")) "
        + "FILTER (lang(?l) = \"en\") "
        + "} "
        + "GROUP BY ?s ?l "
        + "ORDER BY DESC(?c) ";
        
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, query);
    QueryExecution qef = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    ResultSet results = qef.execSelect();
    
    List<RecordUtil<String, String>> records1 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RecordUtil<String, String> record = new RecordUtil<>();
      RDFNode s = qs.get("s");
      RDFNode l = qs.get("l");
      RDFNode c = qs.get("c");
      record.setId(s.toString());
      record.put("resourceName", s.toString());
      record.put("label", l.asLiteral().getValue().toString());
      record.put("count", c.asLiteral().getValue().toString());
      records1.add(record);
    }
    
    qef = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    results = qef.execSelect();
    
    List<RecordUtil<String, String>> records2 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RDFNode s = qs.get("s");
      RDFNode l = qs.get("l");
      RDFNode c = qs.get("c");
      RecordUtil<String, String> record = new RecordUtil<>();
      record.setId(s.toString());
      record.put("resourceName", s.toString());
      record.put("label", l.asLiteral().getValue().toString());
      record.put("count", c.asLiteral().getValue().toString());
      records2.add(record);
    }
    
    // NOTE: Merging counts from both sources
    List<Map<String, String>> records = new ArrayList<>();
    for (RecordUtil<String, String> record : records1) {
      if (!records2.contains(record)) {
        records.add(record);
      } else {
        int i = records2.indexOf(record);
        RecordUtil<String, String> recordComp = records2.get(i);
        int count1 = Integer.valueOf(record.get("count"));
        int count2 = Integer.valueOf(recordComp.get("count"));
        record.put("count", String.valueOf(count1 + count2));
        records.add(record);
        records2.remove(i);
      }
    }
    for (RecordUtil<String, String> record : records2) {
      records.add(record);
    }
    Collections.sort(records, Collections.reverseOrder());
    return records;
  }
    
  public List<Map<String, String>> lookupCountryByName(String countryName) {
    String query = ""
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + "PREFIX dbo: <http://dbpedia.org/ontology/> "
        + "SELECT ?s ?l (COUNT(?inl) as ?c) WHERE { "
        + "?s a dbo:Country . "
        + "?s rdfs:label ?l . "
        + "?inl ?p ?s . "
        + "FILTER (regex(?l, \"" + countryName + "\")) "
        + "FILTER (lang(?l) = \"en\") "
        + "} "
        + "GROUP BY ?s ?l "
        + "ORDER BY DESC(?c) ";
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, query);
    QueryExecution qef = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    ResultSet results = qef.execSelect();
    
    List<RecordUtil<String, String>> records1 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RecordUtil<String, String> record = new RecordUtil<>();
      RDFNode s = qs.get("s");
      RDFNode l = qs.get("l");
      RDFNode c = qs.get("c");
      record.setId(s.toString());
      record.put("resourceName", s.toString());
      record.put("label", l.asLiteral().getValue().toString());
      record.put("count", c.asLiteral().getValue().toString());
      records1.add(record);
    }
    
    qef = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    results = qef.execSelect();
    
    List<RecordUtil<String, String>> records2 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RDFNode s = qs.get("s");
      RDFNode l = qs.get("l");
      RDFNode c = qs.get("c");
      RecordUtil<String, String> record = new RecordUtil<>();
      record.setId(s.toString());
      record.put("resourceName", s.toString());
      record.put("label", l.asLiteral().getValue().toString());
      record.put("count", c.asLiteral().getValue().toString());
      records2.add(record);
    }
    
    // NOTE: Merging counts from both sources
    List<Map<String, String>> records = new ArrayList<>();
    for (RecordUtil<String, String> record : records1) {
      if (!records2.contains(record)) {
        records.add(record);
      } else {
        int i = records2.indexOf(record);
        RecordUtil<String, String> recordComp = records2.get(i);
        int count1 = Integer.valueOf(record.get("count"));
        int count2 = Integer.valueOf(recordComp.get("count"));
        record.put("count", String.valueOf(count1 + count2));
        records.add(record);
        records2.remove(i);
      }
    }
    for (RecordUtil<String, String> record : records2) {
      records.add(record);
    }
    Collections.sort(records, Collections.reverseOrder());
    return records;
  }
  
  // NOTE: Couldn't make it work via SERVICE from Jena, so had to split into 2 calls
  public List<Map<String, String>> lookupRoleByName(String roleName) {
    String query = ""
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + "PREFIX dbyago: <http://dbpedia.org/class/yago/> "
        + "SELECT ?t (COUNT(?inl) as ?c) WHERE { "
        + "?inl a ?t . "
        + "{SELECT DISTINCT ?t WHERE { "
        + "?t rdfs:subClassOf* dbyago:Person100007846 . "
        + "FILTER (regex(?t, \"" + roleName + "\")) "
        + "}} "
        + "} "
        + "GROUP BY ?t "
        + "ORDER BY DESC(?c) ";
    QueryExecution qef = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    ResultSet results = qef.execSelect();
    
    List<RecordUtil<String, String>> records1 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RDFNode t = qs.get("t");
      RDFNode c = qs.get("c");
      RecordUtil<String, String> record = new RecordUtil<>();
      record.setId(t.toString());
      record.put("resourceName", t.toString());
      record.put("count", c.asLiteral().getValue().toString());
      records1.add(record);
    }
    
    qef = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
    //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
    results = qef.execSelect();
    
    List<RecordUtil<String, String>> records2 = new ArrayList<>();
    while (results.hasNext()) { 
      QuerySolution qs = results.nextSolution();
      RDFNode t = qs.get("t");
      RDFNode c = qs.get("c");
      RecordUtil<String, String> record = new RecordUtil<>();
      record.setId(t.toString());
      record.put("resourceName", t.toString());
      record.put("count", c.asLiteral().getValue().toString());
      records2.add(record);
    }
    
    // NOTE: Merging counts from both sources
    List<Map<String, String>> records = new ArrayList<>();
    for (RecordUtil<String, String> record : records1) {
      if (!records2.contains(record)) {
        records.add(record);
      } else {
        int i = records2.indexOf(record);
        RecordUtil<String, String> recordComp = records2.get(i);
        int count1 = Integer.valueOf(record.get("count"));
        int count2 = Integer.valueOf(recordComp.get("count"));
        record.put("count", String.valueOf(count1 + count2));
        records.add(record);
        records2.remove(i);
      }
    }
    for (RecordUtil<String, String> record : records2) {
      records.add(record);
    }
    Collections.sort(records, Collections.reverseOrder());
    return records;
  }

 public List<Map<String, String>> lookupSubjectByName(String subjectName) {
   String query = ""
       + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
       + "PREFIX dbyago: <http://dbpedia.org/class/yago/> "
       + "SELECT ?o (COUNT(?s) as ?c) WHERE { "
       + "?s <http://purl.org/dc/terms/subject> ?o . "
       + "?s a dbyago:Person100007846 . "
       + "FILTER (regex(?o, \"" + subjectName + "\")) "
       + "} "
       + "GROUP BY ?o "
       + "ORDER BY DESC(?c) ";
   QueryExecution qef = QueryExecutionFactory.sparqlService("http://dbpedia-live.openlinksw.com/sparql", query);
   //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
   ResultSet results = qef.execSelect();
   
   Logger.getLogger(this.getClass().getName()).log(Level.INFO, query);
   List<RecordUtil<String, String>> records1 = new ArrayList<>();
   while (results.hasNext()) { 
     QuerySolution qs = results.nextSolution();
     RDFNode o = qs.get("o");
     RDFNode c = qs.get("c");
     RecordUtil<String, String> record = new RecordUtil<>();
     record.setId(o.toString());
     record.put("resourceName", o.toString());
     record.put("count", c.asLiteral().getValue().toString());
     records1.add(record);
   }
   
   qef = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
   //VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
   results = qef.execSelect();
   
   List<RecordUtil<String, String>> records2 = new ArrayList<>();
   while (results.hasNext()) { 
     QuerySolution qs = results.nextSolution();
     RDFNode o = qs.get("o");
     RDFNode c = qs.get("c");
     RecordUtil<String, String> record = new RecordUtil<>();
     record.setId(o.toString());
     record.put("resourceName", o.toString());
     record.put("count", c.asLiteral().getValue().toString());
     records2.add(record);
   }
   
   // NOTE: Merging counts from both sources
   List<Map<String, String>> records = new ArrayList<>();
   for (RecordUtil<String, String> record : records1) {
     if (!records2.contains(record)) {
       records.add(record);
     } else {
       int i = records2.indexOf(record);
       RecordUtil<String, String> recordComp = records2.get(i);
       int count1 = Integer.valueOf(record.get("count"));
       int count2 = Integer.valueOf(recordComp.get("count"));
       record.put("count", String.valueOf(count1 + count2));
       records.add(record);
       records2.remove(i);
     }
   }
   for (RecordUtil<String, String> record : records2) {
     records.add(record);
   }
   Collections.sort(records, Collections.reverseOrder());
   return records;
 }
  
  public static void main(String[] args) {
    //VirtuosoLookupClient.getInstance().lookupRoleByName("Senator");
    //List<Map<String, String>> list = VirtuosoLookupClient.getInstance().lookupCountryByName2("United States");
    List<Map<String, String>> list = VirtuosoLookupClient.getInstance().lookupCountryByName("United States");
    for (Map<String, String> record : list) {
      System.out.println(record.get("resourceName") + " -> " + record.get("label") + " -> " + record.get("sumCount"));
    }
  }
  
  class RecordUtil<T1, T2> extends HashMap<T1, T2> implements Comparable<RecordUtil> {
    private String id; 

    public void setId(String id) {
      this.id = id;
    }
    
    public String getId() {
      return this.id;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      
      if (!(o instanceof RecordUtil)) {
        return false;
      }

      RecordUtil<T1, T2> recordUtil = (RecordUtil<T1, T2>) o;

      return recordUtil.getId().equals(this.id);
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 31 * result + id.hashCode();
      return result;
    }
    
    @Override
    public int compareTo(RecordUtil r) {
      int count1 = Integer.valueOf(this.get("count").toString());
      int count2 = Integer.valueOf(r.get("count").toString());

      return count1 - count2;
    }
  }  
}
