package org.newstextanalyzer.lookup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Utility class that helps retrieve person resources from DBpedia and DBpedia 
 * Live
 * 
 * @author gpanez
 *
 */
class DBpediaRecordRetriever extends Thread implements Runnable {
  private String name;
  private String URLEndpoint;
  private int maxRows;
  private Queue<WorkItem> workItems;
  private Queue<Set<Record>> clqRecords;
  
  public DBpediaRecordRetriever(String name, String URLEndpoint, int maxRows, Queue<WorkItem> workItems, Queue<Set<Record>> clqRecords) {
    this.name = name;
    this.URLEndpoint = URLEndpoint;
    this.maxRows = maxRows;
    this.workItems = workItems;
    this.clqRecords = clqRecords;
  }
  
  public void run() {
    while (true) {
      WorkItem workItem = workItems.poll();
      System.out.println("Thread " + name + " polling: " + workItem);
      if (workItem == null) {
        break;
      } else {
        Set<Record> records = getRecords(URLEndpoint, workItem.getOffset(), maxRows);
        clqRecords.add(records);
      }
    }
  }
  
  public Set<Record> getRecords(String URLEndpoint, int offset, int maxRows) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("SELECT * WHERE {{ ");
    sb.append("SELECT * WHERE {{ ");
    sb.append("SELECT ?s ?l ?bd (COUNT(?t) as ?ct) WHERE { ");
    sb.append("?s a ?t . ");
    sb.append("OPTIONAL { ?s dbo:birthDate ?bd . } ");
    sb.append("?s rdfs:label ?l . ");
    sb.append("?s a <http://dbpedia.org/class/yago/Person100007846> . ");
    sb.append("FILTER (lang(?l) = \"en\") ");
    sb.append("FILTER (?t IN (<http://dbpedia.org/class/yago/Politician110450303>, <http://umbel.org/umbel/rc/Politician>, <http://dbpedia.org/ontology/Politician>, <http://dbpedia.org/class/yago/Person100007846>)) ");
    sb.append("} ");
    sb.append("GROUP BY ?s ?l ?bd ");
    sb.append("}} ");
    sb.append("ORDER BY ?s ?l ?bd ");
    sb.append("}} ");
    sb.append("OFFSET " + offset + " ");
    sb.append("LIMIT " + maxRows);
    System.out.println(sb);
    
    try {
      Query query = QueryFactory.create(sb.toString());
      QueryExecution qexec = QueryExecutionFactory.sparqlService(URLEndpoint, query);
      ResultSet results = null;
      // NOTE: Intended to use tries but realized it required too many to estimate
      // while (tries > 0) {
      while (true) {
        try {
          results = qexec.execSelect();
          break;
        } 
        catch(QueryExceptionHTTP qeh) {
          System.out.println(sb);
          try {
            Random rand = new Random();
            int s = 90 + rand.nextInt(60);
            System.out.println("Thread " + name + " got HttpException: " + qeh.getResponseCode() + ". Retrying ... for: " + offset + ", in " + s + " seconds");
            Thread.sleep(s * 1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      /*
      if (tries == 0) {
        throw new RuntimeException("Thread " + name + " reached maximum tries for: " + offset);
      }*/
      Set<Record> set = new HashSet<>();
      while (results.hasNext()) {
        QuerySolution qs = results.next();
        String s = qs.get("s").toString();
        String l = qs.get("l").asLiteral().getValue().toString();
        Object bd = qs.get("bd");
        if (bd != null) {
          Object bdt = ((RDFNode) bd).asLiteral().getDatatypeURI();
          if (bdt.equals("http://www.w3.org/2001/XMLSchema#date")) {
            try {
              bd = ((RDFNode) bd).asLiteral().getValue().toString();
            }
            catch (DatatypeFormatException dfe) {
              bd = ((RDFNode) bd).toString();
              int endIndex = ((String) bd).indexOf("^");
              if (endIndex != -1) {
                bd = ((String) bd).substring(0, endIndex);
                String strbd = (String) bd;
                // NOTE: Sometimes DBpedia dates are missing 0 to correctly format the month
                if (strbd.length() == 9 && strbd.charAt(4) == '-' && strbd.charAt(6) == '-') {
                  bd = strbd.substring(0, 5) + "0" + strbd.substring(5, 9);
                }
              }
            }
          } else if (((String)bdt).indexOf("^") >= 0) {
            bd = null;
          } else {
            bd = ((RDFNode) bd).toString();
            String strbd = (String) bd;
            // NOTE: Sometimes DBpedia dates are missing 0 to correctly format the month
            if (strbd.length() == 9 && strbd.charAt(4) == '-' && strbd.charAt(6) == '-') {
              bd = strbd.substring(0, 5) + "0" + strbd.substring(5, 9);
            }          }
          //System.out.println(bd);
        }
        int ct = qs.get("ct").asLiteral().getInt();
        Record record = new Record(s, l, (String) bd, ct);
        set.add(record);
      }
      
      qexec.close();
      return set;
    } catch (QueryException qe) {
      qe.printStackTrace();
      System.out.println("Current Query: " + sb.toString());
      throw qe;
    }
  }
}

/**
 * Utility class to recover person records as result of specific query
 * Should only be used within the context of this package
 * @author gpanez
 *
 */
class Record implements Comparable<Record> {
  private String resourceName;
  private String label;
  private String birthDate;
  private int typeCount;

  public Record(String resourceName, String label, String birthDate, int typeCount) {
    super();
    this.resourceName = resourceName;
    this.label = label;
    this.birthDate = birthDate;
    this.typeCount = typeCount;
  }

  public String getResourceName() {
    return resourceName;
  }

  public String getLabel() {
    return label;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public int getTypeCount() {
    return typeCount;
  }

  /**
   * 
   * @param r, the record from DBpedia Live
   * @return
   */
  public Record merge(Record r) {
    String bd = r.getBirthDate();
    if (bd == null && this.birthDate != null) {
      bd = this.birthDate;
    }
    
    // NOTE: Bug, both could have counts of 1, but from different types. So, just sum; 
    int tc = r.getTypeCount() + this.typeCount;
    return new Record(resourceName, label, bd, tc);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    
    if (!(o instanceof Record)) {
      return false;
    }

    Record record = (Record) o;

    return record.getResourceName().equals(resourceName);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + resourceName.hashCode();
    return result;
  }
  
  @Override
  public String toString() {
    return "Record [resourceName=" + resourceName + ", label=" + label + ", birthDate=" + birthDate + ", typeCount="
        + typeCount + "]";
  }
  
  @Override
  public int compareTo(Record r) {
    if (this.birthDate.equals("null") && r.getBirthDate().equals("null")) {
      return this.typeCount - r.getTypeCount();
    } else if (this.birthDate.equals("null")) {
      return -1;
    } else if (r.getBirthDate().equals("null")) {
      return 1;
    }

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    try {
      LocalDate ld1 = LocalDate.fromDateFields(format.parse(this.birthDate));
      LocalDate ld2 = LocalDate.fromDateFields(format.parse(r.getBirthDate()));

      // Note: need to invert order to make it work, given that difference here is calculated as end - start
      int days = Days.daysBetween(ld2, ld1).getDays();
      if (days == 0) {
        return this.typeCount - r.getTypeCount();
      } else {
        return days;
      }
    } catch (ParseException e) {
      e.printStackTrace();
      System.out.println(this.birthDate);
      System.out.println(r.getBirthDate());
      throw new RuntimeException();
    }
  }
}