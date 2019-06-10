package org.newstextanalyzer;

import java.io.IOException;
import java.util.Date;

import org.newstextanalyzer.sentiment.SentimentEnricher;
import org.newstextanalyzer.sentiment.TweetIndexer;

public class App {
  // NOTE: Directory path that contains the folders with the news articles in
  // simple format
  public final static String TGN_DIRECTORY_PATH = "/Users/gpanez/Documents/news/the_guardian_preprocessed";

  public static void main(String[] args) throws IOException {
    System.out.println("Start: " + new Date());
    System.out.println("Indexing tweets in Lucene");
    System.out.println("------------");
    TweetIndexer.getInstance().buildIndex();

    // TODO: The duration aspect from - to
    // TODO: OWL time
    
    if (args != null) {
      if (args.length >= 1 && args[0] != null && args[0].equals("process")) {
        System.out.println("Processing news articles through pipeline");
        System.out.println("------------");
        new NLPAnalysis().findInterestingSentences(
            new Pipeline(
                new IPipelineStep[] { 
                    new SimpleClassifier(),
                    new ReVerbWrapper(), 
                    new EntityValidator(), 
                    new SimpleLinker(), 
                    new VirtuosoPersistor() }),
                new String[] { "2019" });
      }
      if (args.length >= 2 && args[1] != null && args[1].equals("enrich")) {
        System.out.println("Enrich triples with sentiment meta info from tweets");
        System.out.println("------------");
        SentimentEnricher.getInstance().enrich();
      }
      if (args.length >= 3 && args[2] != null && args[2].equals("old")) {
        IRAnalysis ira = new IRAnalysis();
        // ira.buildIndex();
        // ira.deduplicateWordNetWords();
        // ira.explore();

        // new NLPAnalysis().analyze(null);
        // new NLPAnalysis().findInterestingSentences(null, "2018");

        // ResignRERegEx rrre = new ResignRERegEx();
        String[] years = new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009",
            "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019" };
        // new NLPAnalysis().findInterestingSentences(rrre, new String[] {"2018"});

        new NLPAnalysis().findInterestingSentences(new Pipeline(new IPipelineStep[] { new SimpleClassifier(),
            new ReVerbWrapper(), new EntityValidator(), new SimpleLinker(), new VirtuosoPersistor() }),
            new String[] { "2019" });
        // rre.run(text3);
//    rre.run(text4);
        // srre.run(text5);

        // String text5 = "Jeremy Heywood resigned as UK Cabinet Secretary due of ill
        // health following a three-month leave of absence. (October 24)";
//    ResignREBoot rrb = new ResignREBoot();
        // rrb.run(text5);
//    new NLPAnalysis().findInterestingSentences(rrb, null);

      }
    }

    System.out.println("End: " + new Date());
    System.out.println("done");
  }
}