package org.newstextanalyzer.sentiment;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TweetLookup {
  // Limit the number of search results we get
  private static final int MAX_RESULTS = 10;
  private static final String TWEET_INDEX_DIRECTORY_PATH = "/Users/gpanez/Documents/tweets/index";

  private static final String SCREEN_NAME = "SCREEN_NAME";
  private static final String ID = "ID";
  private static final String ID_STR = "ID_STR";
  private static final String URL = "URL";
  private static final String FULL_TEXT = "FULL_TEXT";
  private static final String CREATED_AT = "CREATED_AT";
  private static final String REPLY_COUNT = "REPLY_COUNT";
  private static final String AVG_SENTIMENT_SCORE = "AVG_SENTIMENT_SCORE";

  private static TweetLookup instance = null;

  private Analyzer analyzer;
  private Similarity similarity;
  private Directory directory;
  private IndexWriter iwriter;
  private IndexSearcher isearcher;
  
  // NOTE: Utility String to hold latest Lucene query
  private String latestLuceneQuery;

  public static TweetLookup getInstance() {
    if (instance == null) {
      instance = new TweetLookup();
    }
    return instance;
  }

  private TweetLookup() {
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
      directory = FSDirectory.open(Paths.get(TWEET_INDEX_DIRECTORY_PATH));
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

  public void indexTweet(Tweet tweet, String dateStr) {
    try {
      Document doc = new Document();
      doc.add(new StoredField(SCREEN_NAME, tweet.getScreenName()));
      doc.add(new StoredField(ID, tweet.getId()));
      doc.add(new StoredField(ID_STR, tweet.getIdStr()));
      doc.add(new StoredField(URL, tweet.getURL()));
      doc.add(new TextField(FULL_TEXT, tweet.getFullText(), Field.Store.YES));
      doc.add(new TextField(CREATED_AT, dateStr, Field.Store.YES));
      doc.add(new IntPoint(REPLY_COUNT, tweet.getReplyCount()));
      doc.add(new StoredField(AVG_SENTIMENT_SCORE, tweet.getAvgSentimentScore()));
      // NOTE: Required for point fields to be returned
      doc.add(new StoredField(REPLY_COUNT, tweet.getReplyCount()));

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

  
  /* NOTE: Trying to find tweets that are similar to the Triple. Most tweets will be headlines from 
   * news articles
   */
  public List<Tweet> searchTweets(String rawSubject, String rawPredicate, String rawObject, String textToSearch,
      String fromDate, String toDate, int minReplyCount) {
    try {
      QueryParser parser = new QueryParser(FULL_TEXT, analyzer);
      // Query query = parser.parse(textToSearch.replace("?","\\?"));

      //parser.setDefaultOperator(QueryParser.Operator.AND);

      // Query textQuery = parser.parse(QueryParser.escape(textToSearch.trim()));
      Query rawSubjectQuery = parser.parse("\"" + QueryParser.escape(rawSubject.trim()) + "\"");
      Query rawPredicateQuery = parser.parse("\"" + QueryParser.escape(rawPredicate.trim()) + "\"");
      //parser.setDefaultOperator(QueryParser.Operator.OR);
      boolean objectNotEmpty = true;
      Query rawObjectQuery = null;
      if (rawObject.trim().length() != 0)  {
        rawObjectQuery = parser.parse(QueryParser.escape(rawObject.trim()));
      } else {
        objectNotEmpty = false;
      }

      TermRangeQuery dateQuery = TermRangeQuery.newStringRange(CREATED_AT, fromDate, toDate, true, true);

      Query replyQuery = IntPoint.newRangeQuery(REPLY_COUNT, minReplyCount, Integer.MAX_VALUE);

      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      // builder.add(textQuery, BooleanClause.Occur.MUST);
      builder.add(rawSubjectQuery, BooleanClause.Occur.MUST);
      builder.add(rawPredicateQuery, BooleanClause.Occur.MUST);
      if (objectNotEmpty) {
        builder.add(rawObjectQuery, BooleanClause.Occur.SHOULD);
      }
      builder.add(dateQuery, BooleanClause.Occur.MUST);
      builder.add(replyQuery, BooleanClause.Occur.FILTER);
      BooleanQuery bq = builder.build();
      
      // Get the set of results
      ScoreDoc[] hits = isearcher.search(bq, MAX_RESULTS).scoreDocs;

      List<Tweet> tweets = new ArrayList<>();

      for (int i = 0; i < hits.length; i++) {
        Document hitDoc = isearcher.doc(hits[i].doc);
        // System.out.println("Documents: " + hits[i]);
        Tweet tweet = new Tweet();
        tweet.setScreenName(hitDoc.get(SCREEN_NAME));
        tweet.setId(Long.valueOf(hitDoc.get(ID)));
        tweet.setIdStr(hitDoc.get(ID_STR));
        tweet.setURL(hitDoc.get(URL));
        tweet.setFullText(hitDoc.get(FULL_TEXT));
        tweet.setCreatedAt(hitDoc.get(CREATED_AT));
        tweet.setReplyCount(Integer.valueOf(hitDoc.get(REPLY_COUNT)));
        tweet.setAvgSentimentScore(Double.valueOf(hitDoc.get(AVG_SENTIMENT_SCORE)));
        tweets.add(tweet);
      }
      return tweets;
    } catch (ParseException e) {
      System.out.println("rs: " + rawSubject);
      System.out.println("rp: " + rawPredicate);
      System.out.println("ro: " + rawObject);
      System.out.println("latestLuceneQuery: " + latestLuceneQuery);
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}
