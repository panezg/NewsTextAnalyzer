package org.newstextanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.newstextanalyzer.pipeline.IPipeline;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;
import org.newstextanalyzer.virtuoso.VirtuosoClient;


public class NewsCorpusProcessor {
  protected Map<StepType, Object> processThroughPipeline(IPipeline pipeline, String corpusPath, String[] years) {
    try {
      File dir = new File(corpusPath);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        Arrays.sort(subDirs);
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {
            for (String year : years) {
              if (year != null && !subDir.getName().contains(year)) {
                continue;
              }
              File[] newsArticleFiles = subDir.listFiles();
              for (File newsArticleFile : newsArticleFiles) {
                NewsArticle newsArticle = new TheGuardianNewsArticle(newsArticleFile);
                // p.run(newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString());
                // pipeline.run(newsArticle.getTitle() + ".\n" +
                // newsArticle.getBody().toString());
                // pipeline.run(newsArticle.getTitle() + ".\n");
                pipeline.run(newsArticle);
              }
            }
          }
        }
        //System.out.println(pipeline.toString());
      }
      Map<StepType, Object> sink = pipeline.finish();
      if (sink.containsKey(StepType.INTERLINKER)) {
        VirtuosoClient vc = VirtuosoClient.getInstance();
        //vc.updateInterlinks((Map<String, String>) sink.get(StepType.INTERLINKER));        
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
    return null;
  }
}
