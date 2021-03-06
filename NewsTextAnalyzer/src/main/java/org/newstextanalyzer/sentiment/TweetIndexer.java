package org.newstextanalyzer.sentiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.newstextanalyzer.Const;

import com.google.gson.Gson;

public class TweetIndexer {
  // NOTE: Directory path that contains the folders with the tweets
  public final static String TWEETS_DIRECTORY_PATH = "/Users/gpanez/Documents/tweets/GdnPolitics";

  private static TweetIndexer instance = null;
  private TweetLookup tl;

  public static TweetIndexer getInstance() {
    if (instance == null) {
      instance = new TweetIndexer();
    }
    return instance;
  }

  private TweetIndexer() {
    this.tl = TweetLookup.getInstance();
  }

  public void buildIndex() {
    try {
      tl.initWriter();
      File dir = new File(TWEETS_DIRECTORY_PATH);
      if (dir.exists() && dir.isDirectory()) {
        File[] subDirs = dir.listFiles();
        Arrays.sort(subDirs);
        for (File subDir : subDirs) {
          if (subDir.exists() && subDir.isDirectory()) {
            File[] tweetFiles = subDir.listFiles();
            for (File tweetFile : tweetFiles) {
              // NOTE: Avoiding .DS_Store files from getting to the JSON parser
              if (tweetFile.getName().equals(".DS_Store")) {
                continue;
              }
              Gson gson = new Gson();
              Tweet tweet = gson.fromJson(new BufferedReader(new FileReader(tweetFile)), Tweet.class);
              tl.indexTweet(tweet, subDir.getName());
            }
          }
        }
        tl.closeWriter();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}