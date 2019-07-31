package org.newstextanalyzer.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class VirtuosoQueryClient {
  public static final String URI = "http://myuri/";
  public static final String OWL_TIME_URI = "http://www.w3.org/2006/time#";
  public static final String WORLD_TIME_ZONE_URI = "http://www.w3.org/2006/timezone-world";
  public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";
  
  public static final String LOCAL_SPARQL_ENDPOINT = "<http://test1>";
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia-live.openlinksw.com/sparql>";
  public static final String DBPEDIA_SPARQL_ENDPOINT = "<http://dbpedia.org/sparql>";
  
  private static VirtuosoQueryClient instance;

  private VirtGraph set;

  public static VirtuosoQueryClient getInstance() {
    if (instance == null) {
      instance = new VirtuosoQueryClient();
    }
    return instance;
  }

  private VirtuosoQueryClient() {
    set = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba");
  }
  
  public List<Map<String, String>> queryByPerson(String personResourceName, boolean temporalFilter, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX dbr: <http://dbpedia.org/resource/> ");
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
    sb.append("SELECT DISTINCT ?rs ?rp ?ro ?tl ?xd ?sc ?hs ?hss ?hts ");
    //sb.append("SELECT DISTINCT ?mp ?mo ?rs ?rp ?ro ?tl ?xd ?sc ");
    boolean temporalQuery = false;
    if (temporalFilter || (startDate != null && startDate.length() > 0) || (endDate != null && endDate.length() > 0)) {
      // NOTE: only retrieving those triples with temporal information
      temporalQuery  = true;
    }
    sb.append("FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    sb.append("?ms ?mp ?mo . ");
    sb.append("?ms owl:sameAs ").append("<").append(personResourceName).append("> . ");
    sb.append("?mp <http://myuri/rawSubject> ?rs . ");
    sb.append("?mp <http://myuri/rawPredicate> ?rp . ");
    sb.append("?mp <http://myuri/rawObject> ?ro . ");
    sb.append("?mp <http://myuri/tokenList> ?tl . ");
    sb.append("?mp <http://myuri/hasSource> ?hs . ");
    sb.append("?mp <http://myuri/hasSourceSentence> ?hss . ");
    sb.append("OPTIONAL {?mp <http://myuri/sentimentScore> ?sc . ?mp <http://myuri/hasTweetSource> ?hts . } ");
    if (temporalQuery) {
      sb.append("?mp <http://myuri/xsdDate> ?xd . ");
    } else {
      sb.append("OPTIONAL {?mp <http://myuri/xsdDate> ?xd .  } ");
    }
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    sb.append("} ");
    if (temporalQuery) {
      sb.append("ORDER BY DESC(?xd)");
    }
    
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    int count = 0;
    List<Map<String, String>> records = new ArrayList<>();
    while (results.hasNext()) {
      count++;
      QuerySolution qs = results.nextSolution();
      Map<String, String> record = new HashMap<>();
      //RDFNode mp = qs.get("mp");
      //RDFNode mo = qs.get("mo");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      RDFNode xd = qs.get("xd");
      RDFNode sc = qs.get("sc");
      RDFNode hs = qs.get("hs");
      RDFNode hss = qs.get("hss");
      RDFNode hts = qs.get("hts");
      //record.put("predicate", mp.toString());
      //record.put("object", mo.toString());
      record.put("rawSubject", rs.toString());
      record.put("rawPredicate", rp.toString());
      record.put("rawObject", ro.toString());
      record.put("tokenList", tl.toString());
      record.put("hasSource", hs.toString());
      record.put("hasSourceSentence", hss.toString());
      record.put("sentimentScore", sc == null ? "" : sc.asLiteral().getValue().toString());
      record.put("hasTweetSource", hts == null ? "" : hts.toString());
      record.put("date", xd == null ? "" : xd.asLiteral().getValue().toString());
      records.add(record);
    }
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "count: " + count);
    return records;
  }
  
  public List<Map<String, String>> queryByRoleCountry(String roleResourceName, String countryResourceName, boolean temporalFilter, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
    //sb.append("SELECT ?mp ?mo ?tl ?xd FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    //sb.append("SELECT ?ms ?mp ?mo ?tl ?xd FROM <http://test1> WHERE { ");
    //sb.append("SELECT DISTINCT ?ms ?mp ?mo ?rs ?rp ?ro ?tl ?xd ?sc ");
    sb.append("SELECT DISTINCT ?rs ?rp ?ro ?tl ?xd ?sc ?hs ?hss ?hts ");
    boolean temporalQuery = false;
    if (temporalFilter || (startDate != null && startDate.length() > 0) || (endDate != null && endDate.length() > 0)) {
      // NOTE: only retrieving those triples with temporal information
      temporalQuery  = true;
    }
    sb.append("FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    sb.append("?ms ?mp ?mo . ");
    sb.append("?ms owl:sameAs ?s . ");
    sb.append("?mp <http://myuri/rawSubject> ?rs . ");
    sb.append("?mp <http://myuri/rawPredicate> ?rp . ");
    sb.append("?mp <http://myuri/rawObject> ?ro . ");
    sb.append("?mp <http://myuri/tokenList> ?tl . ");
    sb.append("?mp <http://myuri/hasSource> ?hs . ");
    sb.append("?mp <http://myuri/hasSourceSentence> ?hss . ");
    sb.append("OPTIONAL {?mp <http://myuri/sentimentScore> ?sc . ?mp <http://myuri/hasTweetSource> ?hts . } ");
    if (temporalQuery) {
      sb.append("?mp <http://myuri/xsdDate> ?xd . ");
    } else {
      sb.append("OPTIONAL {?mp <http://myuri/xsdDate> ?xd .  } ");
    }
    sb.append("{SELECT DISTINCT ?s WHERE { ");
    sb.append("{SERVICE " + LIVE_DBPEDIA_SPARQL_ENDPOINT + " { ");
    sb.append("?s a <" + roleResourceName + "> . ");
    // NOTE: Required to get most of the country reference combinations
    sb.append("{ ");
    sb.append("?s dbo:birthPlace ?bp . ");
    sb.append("?bp dbo:country <" + countryResourceName + "> . ");
    sb.append("} ");
    sb.append("UNION ");
    sb.append("{ ");
    sb.append("?s dbo:birthPlace <" + countryResourceName + "> . ");
    sb.append("} ");
    sb.append("UNION ");
    sb.append("{ ");
    sb.append("?s dbo:birthPlace ?bp . ");
    sb.append("?bp dbo:isPartOf* ?po . ");
    sb.append("?po dbo:country <" + countryResourceName + "> . ");
    sb.append("} ");
    sb.append("}} ");
    sb.append(" UNION ");
    sb.append("{SERVICE " + DBPEDIA_SPARQL_ENDPOINT + " { ");
    sb.append("?s a <" + roleResourceName + "> . ");
    // NOTE: Required to get most of the country reference combinations
    sb.append("{ ");
    sb.append("?s dbo:birthPlace ?bp . ");
    sb.append("?bp dbo:country <" + countryResourceName + "> . ");
    sb.append("} ");
    sb.append("UNION ");
    sb.append("{ ");
    sb.append("?s dbo:birthPlace <" + countryResourceName + "> . ");
    sb.append("} ");
    sb.append("UNION ");
    sb.append("{ ");
    sb.append("?s dbo:birthPlace ?bp . ");
    sb.append("?bp dbo:isPartOf* ?po . ");
    sb.append("?po dbo:country <" + countryResourceName + "> . ");
    sb.append("?s dbo:birthPlace ?bp . ");
    sb.append("} ");    
    sb.append("}} ");    
    sb.append("}} ");
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    sb.append("} ");
    if (temporalQuery) {
      sb.append("ORDER BY DESC(?xd)");
    }
    
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    int count = 0;
    List<Map<String, String>> records = new ArrayList<>();
    while (results.hasNext()) {
      count++;
      QuerySolution qs = results.nextSolution();
      Map<String, String> record = new HashMap<>();
      //RDFNode ms = qs.get("ms");
      //RDFNode mp = qs.get("mp");
      //RDFNode mo = qs.get("mo");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      RDFNode xd = qs.get("xd");
      RDFNode sc = qs.get("sc");
      RDFNode hs = qs.get("hs");
      RDFNode hss = qs.get("hss");
      RDFNode hts = qs.get("hts");
      //record.put("subject", ms.toString());
      //record.put("predicate", mp.toString());
      //record.put("object", mo.toString());
      record.put("rawSubject", rs.toString());
      record.put("rawPredicate", rp.toString());
      record.put("rawObject", ro.toString());
      record.put("tokenList", tl.toString());
      record.put("hasSource", hs.toString());
      record.put("hasSourceSentence", hss.toString());
      record.put("sentimentScore", sc == null ? "" : sc.asLiteral().getValue().toString());
      record.put("hasTweetSource", hts == null ? "" : hts.toString());
      record.put("date", xd == null ? "" : xd.asLiteral().getValue().toString());
      records.add(record);
    }
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "count: " + count);
    return records;
  }
  
  public List<Map<String, String>> queryByEvent(String event, boolean temporalFilter, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX dbr: <http://dbpedia.org/resource/> ");
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
    //sb.append("SELECT DISTINCT ?ms ?mp ?mo ?rs ?rp ?ro ?tl ?xd ?sc ");
    sb.append("SELECT DISTINCT ?rs ?rp ?ro ?tl ?xd ?sc ?hs ?hss ?hts ");
    boolean temporalQuery = false;
    if (temporalFilter || (startDate != null && startDate.length() > 0) || (endDate != null && endDate.length() > 0)) {
      // NOTE: only retrieving those triples with temporal information
      temporalQuery  = true;
    }
    sb.append("FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    sb.append("?ms ?mp ?mo . ");
    sb.append("?ms owl:sameAs ?s . ");
    sb.append("?mp <http://myuri/rawSubject> ?rs . ");
    sb.append("?mp <http://myuri/rawPredicate> ?rp . ");
    sb.append("?mp <http://myuri/rawObject> ?ro . ");
    sb.append("?mp <http://myuri/tokenList> ?tl . ");
    sb.append("?mp <http://myuri/hasSource> ?hs . ");
    sb.append("?mp <http://myuri/hasSourceSentence> ?hss . ");
    sb.append("OPTIONAL {?mp <http://myuri/sentimentScore> ?sc . ?mp <http://myuri/hasTweetSource> ?hts . } ");
    if (temporalQuery) {
      sb.append("?mp <http://myuri/xsdDate> ?xd . ");
    } else {
      sb.append("OPTIONAL {?mp <http://myuri/xsdDate> ?xd .  } ");
    }
    sb.append("FILTER (regex(?rp, \"").append(event).append("\")) ");
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    sb.append("} ");
    if (temporalQuery) {
      sb.append("ORDER BY DESC(?xd)");
    }
    
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    int count = 0;
    List<Map<String, String>> records = new ArrayList<>();
    while (results.hasNext()) {
      count++;
      QuerySolution qs = results.nextSolution();
      Map<String, String> record = new HashMap<>();
      //RDFNode ms = qs.get("ms");
      //RDFNode mp = qs.get("mp");
      //RDFNode mo = qs.get("mo");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      RDFNode xd = qs.get("xd");
      RDFNode sc = qs.get("sc");
      RDFNode hs = qs.get("hs");
      RDFNode hss = qs.get("hss");
      RDFNode hts = qs.get("hts");
      //record.put("subject", ms.toString());
      //record.put("predicate", mp.toString());
      //record.put("object", mo.toString());
      record.put("rawSubject", rs.toString());
      record.put("rawPredicate", rp.toString());
      record.put("rawObject", ro.toString());
      record.put("tokenList", tl.toString());
      record.put("hasSource", hs.toString());
      record.put("hasSourceSentence", hss.toString());
      record.put("sentimentScore", sc == null ? "" : sc.asLiteral().getValue().toString());
      record.put("hasTweetSource", hts == null ? "" : hts.toString());
      record.put("date", xd == null ? "" : xd.asLiteral().getValue().toString());
      records.add(record);
    }
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "count: " + count);
    return records;
  }
  
  public static void main(String[] args) {
    VirtuosoQueryClient.getInstance().queryByRoleCountry("http://dbpedia.org/class/yago/Senator110578471", "http://dbpedia.org/resource/United_States", true, null, null);
  }
  
  public List<Map<String, String>> queryBySubject(String subjectResourceName, boolean temporalFilter, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
    //sb.append("SELECT ?mp ?mo ?tl ?xd FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    //sb.append("SELECT ?ms ?mp ?mo ?tl ?xd FROM <http://test1> WHERE { ");
    //sb.append("SELECT DISTINCT ?ms ?mp ?mo ?rs ?rp ?ro ?tl ?xd ?sc ");
    sb.append("SELECT DISTINCT ?rs ?rp ?ro ?tl ?xd ?sc ?hs ?hss ?hts ");
    boolean temporalQuery = false;
    if (temporalFilter || (startDate != null && startDate.length() > 0) || (endDate != null && endDate.length() > 0)) {
      // NOTE: only retrieving those triples with temporal information
      temporalQuery  = true;
    }
    sb.append("FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    sb.append("?ms ?mp ?mo . ");
    sb.append("?ms owl:sameAs ?s . ");
    sb.append("?mp <http://myuri/rawSubject> ?rs . ");
    sb.append("?mp <http://myuri/rawPredicate> ?rp . ");
    sb.append("?mp <http://myuri/rawObject> ?ro . ");
    sb.append("?mp <http://myuri/tokenList> ?tl . ");
    sb.append("?mp <http://myuri/hasSource> ?hs . ");
    sb.append("?mp <http://myuri/hasSourceSentence> ?hss . ");
    sb.append("OPTIONAL {?mp <http://myuri/sentimentScore> ?sc . ?mp <http://myuri/hasTweetSource> ?hts . } ");
    if (temporalQuery) {
      sb.append("?mp <http://myuri/xsdDate> ?xd . ");
    } else {
      sb.append("OPTIONAL {?mp <http://myuri/xsdDate> ?xd .  } ");
    }
    sb.append("{SELECT DISTINCT ?s WHERE { ");
    sb.append("{SERVICE " + LIVE_DBPEDIA_SPARQL_ENDPOINT + " { ");
    sb.append("?s <http://purl.org/dc/terms/subject> <" + subjectResourceName + "> . ");
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    sb.append("}} ");
    sb.append(" UNION ");
    sb.append("{SERVICE " + DBPEDIA_SPARQL_ENDPOINT + " { ");
    sb.append("?s <http://purl.org/dc/terms/subject> <" + subjectResourceName + "> . ");
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    sb.append("}} ");    
    sb.append("}} ");
    sb.append("} ");
    if (temporalQuery) {
      sb.append("ORDER BY DESC(?xd)");
    }
    
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    List<Map<String, String>> records = new ArrayList<>();
    while (results.hasNext()) {
      QuerySolution qs = results.nextSolution();
      Map<String, String> record = new HashMap<>();
      //RDFNode ms = qs.get("ms");
      //RDFNode mp = qs.get("mp");
      //RDFNode mo = qs.get("mo");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      RDFNode xd = qs.get("xd");
      RDFNode sc = qs.get("sc");
      RDFNode hs = qs.get("hs");
      RDFNode hss = qs.get("hss");
      RDFNode hts = qs.get("hts");
      //record.put("subject", ms.toString());
      //record.put("predicate", mp.toString());
      //record.put("object", mo.toString());
      record.put("rawSubject", rs.toString());
      record.put("rawPredicate", rp.toString());
      record.put("rawObject", ro.toString());
      record.put("tokenList", tl.toString());
      record.put("hasSource", hs.toString());
      record.put("hasSourceSentence", hss.toString());
      record.put("sentimentScore", sc == null ? "" : sc.asLiteral().getValue().toString());
      record.put("hasTweetSource", hts == null ? "" : hts.toString());
      record.put("date", xd == null ? "" : xd.asLiteral().getValue().toString());
      records.add(record);
    }
    return records;
  }
  
  public List<Map<String, String>> queryBySentiment(String sentimentThreshold, String sentimentOperator, String sentimentOrder, boolean temporalFilter, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder();
    sb.append("PREFIX dbr: <http://dbpedia.org/resource/> ");
    sb.append("PREFIX dbo: <http://dbpedia.org/ontology/> ");
    sb.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
    sb.append("SELECT DISTINCT ?rs ?rp ?ro ?tl ?xd ?sc ?hs ?hss ?hts ");
    //sb.append("SELECT DISTINCT ?mp ?mo ?rs ?rp ?ro ?tl ?xd ?sc ");
    boolean temporalQuery = false;
    if (temporalFilter || (startDate != null && startDate.length() > 0) || (endDate != null && endDate.length() > 0)) {
      // NOTE: only retrieving those triples with temporal information
      temporalQuery  = true;
    }
    sb.append("FROM ").append(LOCAL_SPARQL_ENDPOINT).append(" WHERE { ");
    sb.append("?ms ?mp ?mo . ");
    sb.append("?mp <http://myuri/rawSubject> ?rs . ");
    sb.append("?mp <http://myuri/rawPredicate> ?rp . ");
    sb.append("?mp <http://myuri/rawObject> ?ro . ");
    sb.append("?mp <http://myuri/tokenList> ?tl . ");
    sb.append("?mp <http://myuri/hasSource> ?hs . ");
    sb.append("?mp <http://myuri/hasSourceSentence> ?hss . ");
    sb.append("?mp <http://myuri/sentimentScore> ?sc . ");
    sb.append("?mp <http://myuri/hasTweetSource> ?hts . ");
    if (temporalQuery) {
      sb.append("?mp <http://myuri/xsdDate> ?xd . ");
    } else {
      sb.append("OPTIONAL {?mp <http://myuri/xsdDate> ?xd .  } ");
    }
    if (startDate != null && startDate.length() > 0) {
      sb.append("FILTER (?xd >= \"").append(startDate).append("\"^^xsd:date) ");
    }
    if (endDate != null && endDate.length() > 0) {
      sb.append("FILTER (?xd <= \"").append(endDate).append("\"^^xsd:date) ");
    }
    if (sentimentOperator.equals("GREATEREQUALS")) {
      sb.append("FILTER (?sc >= ").append(sentimentThreshold).append(") ");
    } else if (sentimentOperator.equals("LESSEQUALS")) {
      sb.append("FILTER (?sc <= ").append(sentimentThreshold).append(") ");      
    }
    sb.append("} ");
    if (temporalQuery) {
      sb.append("ORDER BY DESC(?xd)");
    }
    
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, sb.toString());
    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sb.toString(), set);
    ResultSet results = vqe.execSelect();
    
    List<Map<String, String>> records = new ArrayList<>();
    while (results.hasNext()) {
      QuerySolution qs = results.nextSolution();
      Map<String, String> record = new HashMap<>();
      //RDFNode mp = qs.get("mp");
      //RDFNode mo = qs.get("mo");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      RDFNode xd = qs.get("xd");
      RDFNode sc = qs.get("sc");
      RDFNode hs = qs.get("hs");
      RDFNode hss = qs.get("hss");
      RDFNode hts = qs.get("hts");
      //record.put("predicate", mp.toString());
      //record.put("object", mo.toString());
      record.put("rawSubject", rs.toString());
      record.put("rawPredicate", rp.toString());
      record.put("rawObject", ro.toString());
      record.put("tokenList", tl.toString());
      record.put("hasSource", hs.toString());
      record.put("hasSourceSentence", hss.toString());
      record.put("sentimentScore", sc == null ? "" : sc.asLiteral().getValue().toString());
      record.put("hasTweetSource", hts == null ? "" : hts.toString());
      record.put("date", xd == null ? "" : xd.asLiteral().getValue().toString());
      records.add(record);
    }
    return records;
  }
}