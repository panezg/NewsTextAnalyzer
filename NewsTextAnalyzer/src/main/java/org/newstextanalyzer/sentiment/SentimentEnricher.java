package org.newstextanalyzer.sentiment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.newstextanalyzer.virtuoso.VirtuosoClient;

public class SentimentEnricher {
  private static SentimentEnricher instance;
  
  private VirtuosoClient vc;
  // private TweetExplorer te;
  private TweetSearcher ts;

  private SentimentEnricher() {
    vc = VirtuosoClient.getInstance();
    // te = new TweetExplorer();
    ts = TweetSearcher.getInstance();
  }
  
  public static SentimentEnricher getInstance() {
    if (instance == null) {
      instance = new SentimentEnricher();
    }
    return instance;
  }

  public void enrich() {
    ResultSet results = vc.queryAll();

    int count = 0;
    // int matchesToTriple = 0;
    while (results.hasNext()) {
      count++;
      if (count % 1000 == 0) {
        System.out.println("count:" + count);
      }

      QuerySolution qs = results.nextSolution();
      // RDFNode s = qs.get("s");
      RDFNode p = qs.get("p");
      // RDFNode o = qs.get("o");
      RDFNode rs = qs.get("rs");
      RDFNode rp = qs.get("rp");
      RDFNode ro = qs.get("ro");
      RDFNode tl = qs.get("tl");
      // NOTE: hs = hasSource, or TGN URL
      // RDFNode hs = qs.get("hs");
      RDFNode xsd = qs.get("xsd");
      // System.out.println(hs.toString());

      try {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(xsd.toString().split("^^")[0]);
        List<Tweet> tweets = ts.search(rs.toString(), rp.toString(), ro.toString(), tl.toString(), date);
        if (tweets != null && tweets.size() > 0) {
          Tweet tweet = tweets.get(0);
          // System.out.println(tweet.getId());
          // matchesToTriple++;
          vc.updateWithTweetInfo(p, tweet);
        } else {
          vc.deleteTweetInfo(p);
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
      // System.out.println("matchesToTriple: " + matchesToTriple);

      // System.out.println(" { " + s + " " + p + " " + o + " . }");
      // System.out.println(" { " + rs + " " + rp + " " + ro + "; " + d + " " + hs + "
      // . }");
    }
  }
}
