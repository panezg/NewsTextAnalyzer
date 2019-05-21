package org.newstextanalyzer;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ChunkedBinaryExtractionWrapper {
  private ChunkedBinaryExtraction cbe;
  private double conf;
  
  public ChunkedBinaryExtractionWrapper(ChunkedBinaryExtraction cbe, double conf) {
    this.cbe = cbe;
    this.conf = conf;  
  }
  
  
}
