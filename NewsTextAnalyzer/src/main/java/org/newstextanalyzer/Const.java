package org.newstextanalyzer;

public class Const {
  //NOTE: Directory path that contains the folders with the news articles in simple format
  public final static String TGN_DIRECTORY_PATH = "/Users/gpanez/Documents/news/the_guardian_preprocessed";
  
  // NOTE: 
  public static final String LIVE_DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia-live.openlinksw.com/sparql";
  public static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
  
  public static final String LIVE_DBPEDIA_SPARQL_SERVICE_ENDPOINT = "<" + LIVE_DBPEDIA_SPARQL_ENDPOINT + ">";
  public static final String DBPEDIA_SPARQL_SERVICE_ENDPOINT = "<" + DBPEDIA_SPARQL_ENDPOINT + ">";
}
