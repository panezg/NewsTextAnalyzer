package org.newstextanalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NLPAnalysis {
  protected void analyze(List<String> words) {
    try {
      BasicPipeline bp = new BasicPipeline();
      File dir = new File(App.TGN_DIRECTORY_PATH);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        Arrays.sort(subDirs);
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {            
            File[] newsArticleFiles = subDir.listFiles();
            for (File newsArticleFile : newsArticleFiles) {
              NewsArticle newsArticle = new TheGuardianNewsArticle(newsArticleFile);
              bp.run(newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString(), "May");
              return;
            }
          }
        }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void findInterestingSentences(List<String> words, String[] years) {
    try {
      InterestingSentencesPipeline isp = new InterestingSentencesPipeline();
      File dir = new File(App.TGN_DIRECTORY_PATH);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        Arrays.sort(subDirs);
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {
            for (String year : years) {
              if (!subDir.getName().contains(year)) {
                continue;
              }
              File[] newsArticleFiles = subDir.listFiles();
              for (File newsArticleFile : newsArticleFiles) {
                NewsArticle newsArticle = new TheGuardianNewsArticle(newsArticleFile);
                isp.run(newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString(), null);
              }
            }
          }
        }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void findInterestingSentences(IPipeline pipeline, String[] years) {
    try {
      File dir = new File(App.TGN_DIRECTORY_PATH);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        Arrays.sort(subDirs);
        //Preprocessor p = new Preprocessor();
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {
            for (String year : years) {
              if (year != null && !subDir.getName().contains(year)) {
                continue;
              }
              File[] newsArticleFiles = subDir.listFiles();
              for (File newsArticleFile : newsArticleFiles) {
                NewsArticle newsArticle = new TheGuardianNewsArticle(newsArticleFile);
                //p.run(newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString());
                //pipeline.run(newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString());
                //pipeline.run(newsArticle.getTitle() + ".\n");
                pipeline.run(newsArticle);
              }
            }
          }
        }
        System.out.println(pipeline.toString());
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
