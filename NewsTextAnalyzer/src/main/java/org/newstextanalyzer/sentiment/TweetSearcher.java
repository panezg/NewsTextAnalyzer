package org.newstextanalyzer.sentiment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TweetSearcher {
  private static final int NUM_DAYS_WINDOW = 2;
  private static final int MIN_TWEET_REPLY_COUNT = 0;

  private static TweetSearcher instance = null;
  private LuceneClient lc;

  public static TweetSearcher getInstance() {
    if (instance == null) {
      instance = new TweetSearcher();
    }
    return instance;
  }

  private TweetSearcher() {
    this.lc = LuceneClient.getInstance();
    lc.initSearcher();
  }

  public List<Tweet> search(String rawSubject, String rawPredicate, String rawObject, String textToSearch,
      Date dateAround) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(dateAround);
    // NOTE: Since replies can only come after the occurrence of the tweet, reduce
    // the search space
    cal.add(Calendar.DAY_OF_MONTH, -1);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    String fromDate = formatter.format(cal.getTime());

    cal.setTime(dateAround);
    cal.add(Calendar.DAY_OF_MONTH, NUM_DAYS_WINDOW);
    String toDate = formatter.format(cal.getTime());
    return lc.searchTweets(rawSubject, rawPredicate, rawObject, textToSearch, fromDate, toDate, MIN_TWEET_REPLY_COUNT);
  }
}
