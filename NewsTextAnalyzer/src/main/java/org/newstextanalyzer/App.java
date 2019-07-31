package org.newstextanalyzer;

import java.io.IOException;
import java.util.Date;

import org.newstextanalyzer.lookup.DBpediaScrapper;
import org.newstextanalyzer.pipeline.EntityValidator;
import org.newstextanalyzer.pipeline.IPipelineStep;
import org.newstextanalyzer.pipeline.InterLinker;
import org.newstextanalyzer.pipeline.PipelineManager;
import org.newstextanalyzer.pipeline.ReVerbOIE;
import org.newstextanalyzer.pipeline.Referencer;
import org.newstextanalyzer.pipeline.SimpleClassifier;
import org.newstextanalyzer.pipeline.IntraLinker;
import org.newstextanalyzer.pipeline.VirtuosoPersistor;
import org.newstextanalyzer.sentiment.SentimentEnricher;
import org.newstextanalyzer.sentiment.TweetIndexer;

public class App {

  public static void main(String[] args) throws IOException {
    System.out.println("Start: " + new Date());
    System.out.println("Indexing tweets in Lucene");
    System.out.println("------------");
    TweetIndexer.getInstance().buildIndex();

    // TODO: The duration aspect from - to
    // NOTE: arguments for Run Config
    //"scrap"
    //"process" "enrich"
    if (args != null) {
      if (args.length == 1 && args[0] != null && args[0].equals("scrap")) {
        System.out.println("Scrapping DBpedia and DBpedia Live for people entities");
        System.out.println("------------");
        DBpediaScrapper scrapper = DBpediaScrapper.getInstance();
        scrapper.run();
        scrapper.finalize();
      }
      if (args.length >= 1 && args[0] != null && args[0].equals("process")) {
        System.out.println("Processing news articles through pipeline");
        System.out.println("------------");
        String[] years = new String[] { 
            "2000", "2001", "2002", "2003", "2004", 
            "2005", "2006", "2007", "2008", "2009",
            "2010", "2011", "2012", "2013", "2014", 
            "2015", "2016", "2017", "2018", "2019" };
        
        new NewsCorpusProcessor().processThroughPipeline(
            new PipelineManager(
                new IPipelineStep[] { 
                    new SimpleClassifier(),
                    new Referencer(),
                    new ReVerbOIE(), 
                    new EntityValidator(), 
                    new IntraLinker(), 
                    new InterLinker(),
                    new VirtuosoPersistor() 
                }
            ),
            Const.TGN_DIRECTORY_PATH,
            //new String[] { "2019" }
            years
        );
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
        // new NLPAnalysis().findInterestingSentences(rrre, new String[] {"2018"});

        /*
        new NLPAnalysis().findInterestingSentences(new Pipeline(new IPipelineStep[] { new SimpleClassifier(),
            new ReVerbWrapper(), new EntityValidator(), new SimpleLinker(), new VirtuosoPersistor() }),
            new String[] { "2019" });
        */
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