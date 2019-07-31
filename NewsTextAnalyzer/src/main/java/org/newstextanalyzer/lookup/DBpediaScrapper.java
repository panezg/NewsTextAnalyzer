package org.newstextanalyzer.lookup;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.newstextanalyzer.Const;


public class DBpediaScrapper {
  public static final int RESULT_SET_MAX_ROWS = 10000;
  
  private PersonResourceLookup llc;
  
  private static DBpediaScrapper instance;

  private DBpediaScrapper() {
    llc = PersonResourceLookup.getInstance();
    llc.initWriter();
  }
  
  public static DBpediaScrapper getInstance() {
    if (instance == null) {
      instance = new DBpediaScrapper();
    }
    return instance;
  }
  
  public void finalize() {
    llc.closeWriter();
  }

  public Queue<Set<Record>> retrieve(String endpoint) {
    int n = 8;
    int size = this.countValidRecords(endpoint);
    
    System.out.println("Size: " + size );
    Queue<WorkItem> workItems = generateWorkItems(size);
    
    Queue<Set<Record>> clqRecords = new ConcurrentLinkedQueue<>();
    
    DBpediaRecordRetriever[] retrievers = new DBpediaRecordRetriever[n];
    for (int i = 0; i < n; i++) {
      retrievers[i] = new DBpediaRecordRetriever("Retriever" + i, endpoint, RESULT_SET_MAX_ROWS, workItems, clqRecords);
    }
    for (int i = 0; i < n; i++) {
      retrievers[i].start();
    }
    for (int i = 0; i < n; i++) {
      try {
        retrievers[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return clqRecords;
  }
  
  public int countValidRecords(String URLEndpoint) {
    // NOTE: Counts number of people records
    // From live DBpedia, without birthDate = 1,000,209; with birthDate = 310,711
    // From DBpedia, without birthDate = 3,078,975, with birthDate = 2,181,427
    //3,128,539
    //3,377,776
    // Above irrelevant
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("SELECT (COUNT(*) as ?c) WHERE { ");
    sb.append("SELECT ?s ?l ?bd (COUNT(?t) as ?ct) WHERE { ");
    sb.append("?s a ?t . ");
    sb.append("OPTIONAL { ?s dbo:birthDate ?bd . } ");
    sb.append("?s rdfs:label ?l . ");
    sb.append("?s a <http://dbpedia.org/class/yago/Person100007846> . ");
    sb.append("FILTER (lang(?l) = \"en\") ");
    sb.append("FILTER (?t IN (<http://dbpedia.org/class/yago/Politician110450303>, <http://umbel.org/umbel/rc/Politician>, <http://dbpedia.org/ontology/Politician>, <http://dbpedia.org/class/yago/Person100007846>)) ");    
    sb.append("} ");
    sb.append("GROUP BY ?s ?l ?bd ");
    sb.append("} ");
    //System.out.println(sb);
    
    try {

      Query query = QueryFactory.create(sb.toString());
      QueryExecution qexec = QueryExecutionFactory.sparqlService(URLEndpoint, query);

      ResultSet results = qexec.execSelect();
      if (results.hasNext()) {
        QuerySolution qs = results.next();
        return Integer.valueOf(qs.get("c").asNode().getLiteralValue().toString());
      }

      // ResultSetFormatter.out(System.out, results, query);
      qexec.close();
    } catch (QueryException qe) {
      qe.printStackTrace();
      System.out.println("Current Query: " + sb.toString());
      throw qe;
    }
    return -1;
  }
  
  public Queue<WorkItem> generateWorkItems(int size) {
    ConcurrentLinkedQueue<WorkItem> workItems = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < size; i+= RESULT_SET_MAX_ROWS) {
      workItems.add(new WorkItem(i));
    }
    return workItems;
  }
  
  public void run() {
    Queue<Set<Record>> clqRecordsLive = retrieve(Const.LIVE_DBPEDIA_SPARQL_ENDPOINT);
    Queue<Set<Record>> clqRecords = retrieve(Const.DBPEDIA_SPARQL_ENDPOINT);
    
    Map<String, Record> records = new HashMap<>();
    int count1 = 0;
    for (Set<Record> set : clqRecordsLive) {
      for (Record record : set) {
        records.put(record.getResourceName(), record);
        count1++;
      }
    }
    System.out.println("Size of person records retrieved from DBpedia Live: " + count1);
    
    int count2 = 0;
    for (Set<Record> set : clqRecords) {
      for (Record record : set) {
        // NOTE: if record exists in DBpedia, but not in DBpedia Live
        if (!records.containsKey(record.getResourceName())) {
          records.put(record.getResourceName(), record);  
        } else {
          // NOTE: if record exists both in DBpedia and DBpedia Live, then merge them
          Record r = records.get(record.getResourceName());
          records.put(record.getResourceName(), record.merge(r));
        }
        count2++;
      }
    }
    System.out.println("Size of person records retrieved from DBpedia: " + count2);
    
    for (Record record : records.values())  {
      llc.indexRecord(record);
    }
    System.out.println("Size of person records merged: " + records.size());
  }
  
  public static void main(String[] args) {
    DBpediaScrapper.getInstance().run();
  }
}

class WorkItem {
  private int offset;
  
  public WorkItem(int offset) {
    this.offset = offset;
  }
  
  public int getOffset() {
    return offset;
  }
  
  public String toString() {
    return "WorkItem with offset: " + offset;
  }
}