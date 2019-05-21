package org.newstextanalyzer;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TheGuardianNewsArticle extends NewsArticle {
  private final static String HEADER_SEPARATOR = "#####";
  private final static int HEADER_ENTRIES = 4;
  
  public TheGuardianNewsArticle(File file) throws Exception{
    super(file);
  }
  
  public void construct(File file) throws Exception {
      List<String> lines = Files.readAllLines(file.toPath());
      String time = "", id = "", URL = "", title = "", body = "";
      int countHeaderSeparator = 1;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).equals(HEADER_SEPARATOR)) {
          switch(countHeaderSeparator) {
            case 1:
              time = sb.toString();
              countHeaderSeparator++;
              break;
            case 2:
              id = sb.toString();
              countHeaderSeparator++;
              break;
            case 3:
              URL = sb.toString();
              countHeaderSeparator++;
              break;
            case 4:
              title = sb.toString();
              countHeaderSeparator++;
              break;
          }
          sb = new StringBuilder();
        }
        else {
          sb.append(lines.get(i) + " ");
        }
      }
      if (countHeaderSeparator != HEADER_ENTRIES + 1) {
        throw new Exception("File doesn't conform to format: [" + file.getName() + "]");
      } 
      body = sb.toString();
      super.populate(time, id, URL, title, body, NewsArticle.NewsPublication.THE_GUARDIAN);
  }
}
