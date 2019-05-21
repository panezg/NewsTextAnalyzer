package org.newstextanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ReVerbWrapper implements IPipelineStep {
  public static double CONFIDENCE_THRESHOLD = 0.75;
  
  private OpenNlpSentenceChunker chunker;
  private ReVerbExtractor reverb;
  private ConfidenceFunction confFunc;

  public ReVerbWrapper() {
    // NOTE: OpenNLP looks on the classpath for the default model files related
    // to chunker
    try {
      this.chunker = new OpenNlpSentenceChunker();
      this.reverb = new ReVerbExtractor();
      this.confFunc = new ReVerbOpenNlpConfFunction();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException();
    }
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    NewsArticle newsArticle = (NewsArticle) extra[0];
    ChunkedSentence sent = chunker.chunkSentence(sentence);

    // System.out.println(text);
    // for (int i = 0; i < sent.getLength(); i++) {
    // String token = sent.getToken(i);
    // String posTag = sent.getPosTag(i);
    // String chunkTag = sent.getChunkTag(i);
    // System.out.println(token + " " + posTag + " " + chunkTag);
    // }
    
    //List<ChunkedBinaryExtraction> triples = new ArrayList<>();
    List<TripleWrapper> triplesWrapper = new ArrayList<>();
    for (ChunkedBinaryExtraction triple : reverb.extract(sent)) {
      double conf = confFunc.getConf(triple);
      if (conf >= CONFIDENCE_THRESHOLD) {
        // Prints out extractions from the sentence.
        // System.out.println("Conf=" + conf + "; Arg1=" + triple.getArgument1() + ";
        // Rel=" + triple.getRelation() + "; Arg2=" + triple.getArgument2());
        triplesWrapper.add(new TripleWrapper(triple, newsArticle, conf));
      }
    }
    return triplesWrapper;
  }

  @Override
  public Type getType() {
    return Type.EXTRACTOR;
  }

  @Override
  public void clean() {
    return;
  }
}
