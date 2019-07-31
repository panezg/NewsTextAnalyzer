package org.newstextanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.newstextanalyzer.pipeline.IPipelineManager;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;
import org.newstextanalyzer.virtuoso.VirtuosoClient;


public class NewsCorpusProcessor {
  
  protected Map<StepType, Object> processThroughPipeline(IPipelineManager pipeline, String corpusPath, String[] years) {
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
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) sink.get(StepType.INTERLINKER);
        vc.updateInterlinks(map);
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
