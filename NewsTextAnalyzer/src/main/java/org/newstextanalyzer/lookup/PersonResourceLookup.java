package org.newstextanalyzer.lookup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class PersonResourceLookup {
  // Limit the number of search results we get
  private static final int MAX_RESULTS = 10;
  private static final String PERSON_DBPEDIA_INDEX_DIRECTORY_PATH = "/Users/gpanez/Documents/lookup/index";

  private static final String RESOURCE_NAME = "RESOURCE_NAME";
  private static final String LABEL = "LABEL";
  private static final String BIRTH_DATE = "BIRTH_DATE";
  private static final String TYPE_COUNT = "TYPE_COUNT";

  private static PersonResourceLookup instance = null;

  private Analyzer analyzer;
  private Similarity similarity;
  private Directory directory;
  private IndexWriter iwriter;
  private IndexSearcher isearcher;

  public static PersonResourceLookup getInstance() {
    if (instance == null) {
      instance = new PersonResourceLookup();
    }
    return instance;
  }

  private PersonResourceLookup() {
    try {
      int indexAnalyzer = 0, indexSimilarity = 0;

      Analyzer[] analyzers = new Analyzer[] { new StandardAnalyzer(), new EnglishAnalyzer(), new ClassicAnalyzer() };
      // new CustomAnalyzer(),
      // new CustomAnalyzer(new CharArraySet(Arrays.asList(
      // "with", "were", "also", "some", "when", "over", "other", "both",
      // "into"), false))};
      Similarity[] similarities = new Similarity[] { new BM25Similarity(), new ClassicSimilarity() };
      // ,new MultiSimilarity(new Similarity[] {new ClassicSimilarity(), new
      // BM25Similarity()})};

      analyzer = analyzers[indexAnalyzer];
      similarity = similarities[indexSimilarity];
      // analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
      // analyzer = new GPVAnalyzer(EnglishAnalyzer.getDefaultStopSet());
      // analyzer = new EnglishAnalyzer();
      // analyzer = new StandardAnalyzer();
      // analyzer = new ClassicAnalyzer(EnglishAnalyzer.getDefaultStopSet());
      // analyzer = new ClassicAnalyzer();

      // similarity = new ClassicSimilarity();
      // similarity = new BM25Similarity();
      // similarity = new MultiSimilarity(new Similarity[] {new ClassicSimilarity(),
      // new BM25Similarity()});

      // To store an index in memory
      // Directory directory = new RAMDirectory();
      directory = FSDirectory.open(Paths.get(PERSON_DBPEDIA_INDEX_DIRECTORY_PATH));
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException();
    }
  }

  public void initWriter() {
    try {
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      if (similarity != null) {
        config.setSimilarity(similarity);
      }
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

      iwriter = new IndexWriter(directory, config);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException();
    }
  }

  public void indexRecord(Record record) {
    try {
      Document doc = new Document();
      doc.add(new StoredField(RESOURCE_NAME, record.getResourceName()));
      doc.add(new TextField(LABEL, record.getLabel(), Field.Store.YES));      
      if (record.getBirthDate() != null) {
        doc.add(new StoredField(BIRTH_DATE, record.getBirthDate()));
      }
      else {
        doc.add(new StoredField(BIRTH_DATE, "null"));
      }
      doc.add(new IntPoint(TYPE_COUNT, record.getTypeCount()));
      //doc.add(new TextField(FULL_TEXT, tweet.getFullText(), Field.Store.YES));
      //doc.add(new TextField(CREATED_AT, dateStr, Field.Store.YES));
      //doc.add(new IntPoint(REPLY_COUNT, tweet.getReplyCount()));
      //doc.add(new StoredField(AVG_SENTIMENT_SCORE, tweet.getAvgSentimentScore()));
      // NOTE: Required for point fields to be returned
      doc.add(new StoredField(TYPE_COUNT, record.getTypeCount()));

      // date: [2010-10-4T00:00:00 TO 2010-10-4T23:59:59]
      // created_date:["2015-08-16 07:38:00" TO "2015-08-27 07:38:02"]
      iwriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  public void closeWriter() {
    try {
      iwriter.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException();
    }
  }

  public void initSearcher() {
    try {
      isearcher = new IndexSearcher(DirectoryReader.open(directory));

      if (similarity != null) {
        isearcher.setSimilarity(similarity);
        // isearcher.setSimilarity(new ClassicSimilarity());
        // isearcher.setSimilarity(new BM25Similarity());
        // isearcher.setSimilarity(new MultiSimilarity(new Similarity[] {new
        // ClassicSimilarity(), new BM25Similarity()}));
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  public List<Record> searchRecords(String label, Date referenceDate) {
    try {
      // BooleanQuery.Builder builder = new BooleanQuery.Builder();
      // builder.add(new TermQuery(new Term("contents", "java")), true, false);
      // builder.add(new TermQuery(new Term("contents", "net")), true, false);
      // builder.add(new TermQuery(new Term("contents", "dot")), false, true);
      // Query qpQuery = QueryParser.parse("java AND net NOT dot", "contents", new
      // StandardAnalyzer());
      // Query and subclasses behave as expected with .equals
      // assertEquals(qpQuery, apiQuery);

      QueryParser parser = new QueryParser(LABEL, analyzer);
      // Query query = parser.parse(textToSearch.replace("?","\\?"));

      // Query textQuery = parser.parse(QueryParser.escape(textToSearch.trim()));

      // NOTE: Required to match all words of the label to search, when not doing phrase query search
      parser.setDefaultOperator(QueryParser.Operator.AND);
      
      // NOTE: Tried many ways to create the PhraseQuery. Using the API, e.g., PhraseQuery.Builder
      // is a problem because each word needs to be added independently
      // If the entire phrase is added, no preprocessing is done (lowercase, etc), and nothing gets matched
      // If word by word is added, it works, given that you add the correct word, but not sure
      // which would be the best way to get the words from the search string
      // Using the query parser achieves both. It tokenizes and preprocesses, so you can just pass the search tring
      // and provides a phrase query. Achieved by adding the quotes
      
      Query labelQuery = null;
      try {
        labelQuery = parser.parse("\"" + QueryParser.escape(label.trim()) + "\"");
      } catch (ParseException pe) {
        pe.printStackTrace();
      }
      //String toDate = referenceDate;
      //TermRangeQuery dateQuery = TermRangeQuery.newStringRange(BIRTH_DATE, fromDate, toDate, true, true);

      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      //builder.add(labelQuery, BooleanClause.Occur.SHOULD);
      builder.add(labelQuery, BooleanClause.Occur.SHOULD);
      //builder.add(rawPredicateQuery, BooleanClause.Occur.MUST);
      //builder.add(rawObjectQuery, BooleanClause.Occur.SHOULD);
      //builder.add(dateQuery, BooleanClause.Occur.MUST);
      //builder.add(replyQuery, BooleanClause.Occur.FILTER);
      BooleanQuery bq = builder.build();
      //System.out.println(bq.toString());

      // Get the set of results
      ScoreDoc[] hits = isearcher.search(bq, MAX_RESULTS).scoreDocs;

      List<Record> records = new ArrayList<>();
      
      for (int i = 0; i < hits.length; i++) {
        Document hitDoc = isearcher.doc(hits[i].doc);
        // System.out.println("Documents: " + hits[i]);
        if (hitDoc.get(BIRTH_DATE).contains("^")) {
          continue;
        }
        Record record = new Record(hitDoc.get(RESOURCE_NAME), 
            hitDoc.get(LABEL), 
            hitDoc.get(BIRTH_DATE),
            Integer.valueOf(hitDoc.get(TYPE_COUNT)));
        records.add(record);
      }
      return records;
    //} catch (ParseException e) {
    //  e.printStackTrace();
    //  throw new RuntimeException();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}
