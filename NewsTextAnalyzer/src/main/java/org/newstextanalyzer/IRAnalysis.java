package org.newstextanalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IRAnalysis {
  public final static String INDEX_DIRECTORY = "/Users/gpanez/Documents/news/the_guardian_preprocessed_index";
  public final static String WN_EVENT_WORDS_FILE = App.WORDNET_DIRECTORY_PATH + "/" + "aggregate_event_hierarchy.csv";
  public final static String WN_EVENT_WORDS_DEDUP_FILE = App.WORDNET_DIRECTORY_PATH + "/" + "aggregate_event_hierarchy_dedup.csv";
  public final static String WN_EVENT_WORDS_ANALYSIS_RESULT_FILE = App.WORDNET_DIRECTORY_PATH + "/" + "aggregate_event_hierarchy_dedup_analysis.csv";
  
  private final static String TIME_FIELD = "TIME";
  private final static String ID_FIELD = "ID";
  private final static String URL_FIELD = "URL";
  private final static String TITLE_FIELD = "TITLE";
  private final static String BODY_FIELD = "BODY";
  
  //private final static int MAX_RESULTS = 10;

  private Analyzer analyzer;
  private Similarity similarity;
  private Directory directory;

  public IRAnalysis() {
    this.analyzer = new EnglishAnalyzer();
    this.similarity = new BM25Similarity();
  }

  public void buildIndex() {
    try {
      directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
      // To store an index in memory
      // Directory directory = new RAMDirectory();
      // To store an index on disk
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      config.setSimilarity(similarity);
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
      IndexWriter iwriter = new IndexWriter(directory, config);
      
      File dir = new File(App.TGN_DIRECTORY_PATH);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {            
            File[] newsArticleFiles = subDir.listFiles();
            for (File newsArticleFile : newsArticleFiles) {
              NewsArticle newsArticle = new TheGuardianNewsArticle(newsArticleFile); 
              
              //Add file content to a document in the index
              Document doc = new Document();
              doc.add(new TextField(TIME_FIELD, newsArticle.getTime(), Field.Store.YES));
              doc.add(new TextField(ID_FIELD, newsArticle.getId(), Field.Store.YES));
              doc.add(new TextField(URL_FIELD, newsArticle.getURL(), Field.Store.YES));
              doc.add(new TextField(TITLE_FIELD, newsArticle.getTitle(), Field.Store.YES));
              doc.add(new TextField(BODY_FIELD, newsArticle.getBody(), Field.Store.YES));
              iwriter.addDocument(doc);
            }
          }
        }
      }
      iwriter.close();
      directory.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deduplicateWordNetWords() {
    try {
      File file = new File(WN_EVENT_WORDS_FILE);
      if (file.exists()) {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line;
        Map<String, String> hm = new HashMap<String, String>();
        while ((line = br.readLine()) != null) {
          String URI = line.split(",")[1];
          String word = line.split(",")[0];
          //using URI as key
//          if (!hm.containsKey(URI)) {
//            hm.put(URI, word);
//          }
          //using word as key without using any policy to determine which synset to keep, in case they are different
          if (!hm.containsKey(word)) {
            hm.put(word, URI);
          }
        }
        br.close();
        file = new File(WN_EVENT_WORDS_DEDUP_FILE);
        Writer writer = new BufferedWriter(new FileWriter(file, false));
        for (String word : hm.keySet()) {
          writer.write(word + "," + hm.get(word) + ", \n");
        }
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void explore() {
    try {
      File file = new File(WN_EVENT_WORDS_DEDUP_FILE);
      if (file.exists()) {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line;
        List<String> words = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
          words.add(line.split(",")[0].replace("\"", ""));
        }
        br.close();
        List<QueryResult> queryResults = this.searchWords(words);
        //sorted in ascending order
        Collections.sort(queryResults);
        File fileOut = new File(WN_EVENT_WORDS_ANALYSIS_RESULT_FILE);
        Writer writer = new BufferedWriter(new FileWriter(fileOut, false));
        //for (int i = queryResults.size() - 1; i >= queryResults.size() - 100; i--) {
        for (int i = queryResults.size() - 1; i >= 0; i--) {
          //System.out.println(queryResults.get(i));
          writer.write(queryResults.get(i) + ", \n");
        }
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected List<QueryResult> searchWords(List<String> words) {
    try {
      directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
      DirectoryReader ireader = DirectoryReader.open(directory);
      IndexSearcher isearcher = new IndexSearcher(ireader);
      isearcher.setSimilarity(similarity);

      List<QueryResult> queryResults = new ArrayList<QueryResult>();

      // Result result = new Result(word, ireader, analyzer);
      for (String word : words) {
        QueryParser parser = new QueryParser(BODY_FIELD, analyzer);
        //System.out.println(word);
        Query query = parser.parse("\"" + word + "\"");
        
        // Get the set of results
        ScoreDoc[] hits = isearcher.search(query, 1000000).scoreDocs;
        int hitsCount = hits.length;
        hits = isearcher.search(query, 10).scoreDocs;

        //new code for highlighting
        //Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter();
        //It scores text fragments by the number of unique query terms found
        //Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);
        //It breaks text up into same-size texts but does not split up spans
        //Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 50);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
        //Fragmenter fragmenter = new SimpleFragmenter(10);
        //set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);
        
        List<DocResult> docResults = new ArrayList<DocResult>();
        for (int i = 0; i < hits.length; i++) {
          Document hitDoc = isearcher.doc(hits[i].doc);
          //Get stored text from found document
          String text = hitDoc.get(BODY_FIELD);
          //Create token stream
          TokenStream stream = TokenSources.getAnyTokenStream(ireader, hits[i].doc, BODY_FIELD, analyzer);
          //Get highlighted text fragments
          String[] frags = highlighter.getBestFragments(stream, text, 10);
//          for (String frag : frags)
//          {
//              System.out.println("=======================");
//              System.out.println(frag);
//          }
          docResults.add(new DocResult(word, 
              hitDoc.get(TIME_FIELD),
              hitDoc.get(ID_FIELD),
              hitDoc.get(URL_FIELD),
              hitDoc.get(TITLE_FIELD),
              hitDoc.get(BODY_FIELD), 
              frags));
        }
        queryResults.add(new QueryResult(word, hitsCount, docResults));
      }
      directory.close();
      return queryResults;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

class QueryResult implements Comparable<QueryResult>{
  private String word;
  private int hitsCount;
  private List<DocResult> docResults;

  public QueryResult(String word, int hitsCount, List<DocResult> docResults) {
    this.word = word;
    this.hitsCount = hitsCount;
    this.docResults = docResults;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("QueryResult for word: " + word + "\n");
    sb.append("Hits count: " + hitsCount + "\n");
    for (DocResult docResult : docResults) {
      sb.append(docResult);
    }
    return sb.toString();
  }
  
  public int getHitsCount() {
    return hitsCount;
  }
  
  @Override
  public int compareTo(QueryResult qr) {
    if (this.getHitsCount() == qr.getHitsCount()) {
      return 0;
    }
    else if (this.getHitsCount() < qr.getHitsCount()) {
      return -1;
    }
    else {
      return 1;
    }
  }
}

class DocResult {
  private String word;
  private String time;
  private String id;
  private String URL;
  private String title;
  private String body;
  private String[] frags;
  
  private List<String> sentences;

  public DocResult(String word, String time, String id, String URL, String title, String body, String[] frags) {
    this.word = word;
    this.time = time;
    this.id = id;
    this.URL = URL;
    this.title = title;
    this.body = body;
    this.frags = frags;
    //getSentences();
  }

  private void getSentences() {
    sentences = new ArrayList<String>();
    BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.UK);
    iterator.setText(body);
    int start = iterator.first();

    for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
      String sentence = body.substring(start, end);
      if (sentence.contains(word)) {
        sentences.add(sentence);
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TITLE: " + this.title + "\n");
    sb.append("{");
//    for (String sentence : sentences) {
//      sb.append("[");
//      sb.append(sentence);
//      sb.append("],\n");
//    }
    for (String frag : frags) {
      sb.append("[");
      sb.append(frag);
      sb.append("],\n");
    }
    sb.append("}");
    return sb.toString();
  }
}