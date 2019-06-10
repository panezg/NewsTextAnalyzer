package org.newstextanalyzer;

public class DateExtractor {
  // NOTE: next week means it hasn't happened yet
  private static final String[] KNOWN_IRRELEVANT_PHRASES = { "the day", "the future", "minutes later", "next week" };
  private static final String[] KNOWN_RELEVANT_PATTERNS = { "" };

  // In order of frequency according to manual analysis via Excel pivot table
  public static String extract(String newsArticleDate, String stringDate) {
    if (stringDate.toLowerCase().equals("last week") || stringDate.toLowerCase().equals("last weekend")) {
      // NOTE: return YYYY-mm-?? but first subtract 7 days from article date
      return null;
    }
    if (stringDate.toLowerCase().equals("last year")) {
      // NOTE: return YYYY-??-?? but first subtract 1 year from article date
      return null;
    }
    if (stringDate.toLowerCase().equals("last month")) {
      // NOTE: return YYYY-mm-?? but first subtract 1 month from article date
      return null;
    }
    if (stringDate.toLowerCase().equals("this year")) {
      // NOTE: return YYYY-??-??
      return null;
    }
    if (stringDate.toLowerCase().equals("this week") || stringDate.toLowerCase().equals("this weekend")) {
      // NOTE: return YYYY-mm-??
      return null;
    }

    if (stringDate.toLowerCase().equals("this weekend")) {
      return null;
    }

    return null;
  }
}
