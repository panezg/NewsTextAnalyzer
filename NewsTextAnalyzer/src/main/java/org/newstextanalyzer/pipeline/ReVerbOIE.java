package org.newstextanalyzer.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.newstextanalyzer.NewsArticle;

import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.util.DefaultObjects;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.tokenize.Tokenizer;

/**
 * Leverages ReVerb and OpenNLP libs to extract triples from a sentence Triples
 * with confidence levels less than threshold are discarded
 * 
 * @author gpanez
 *
 */
public class ReVerbOIE implements IPipelineStep {
  public static final double CONFIDENCE_THRESHOLD = 0.75;

  private OpenNlpSentenceChunker enhancedChunker;
  private Tokenizer tokenizer;
  private POSTagger posTagger;
  private Chunker chunker;
  private ReVerbExtractor reverb;
  private ConfidenceFunction confFunc;

  public ReVerbOIE() {
    // NOTE: OpenNLP looks on the classpath for the default model files related
    // to chunker
    try {
      this.tokenizer = DefaultObjects.getDefaultTokenizer();
      this.posTagger = DefaultObjects.getDefaultPosTagger();
      this.chunker = DefaultObjects.getDefaultChunker();
      this.enhancedChunker = new OpenNlpSentenceChunker(tokenizer, posTagger, chunker);
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
    //NewsArticle newsArticle = null;
    
    ChunkedSentence sent = enhancedChunker.chunkSentence(sentence);

    // System.out.println(text);
    // for (int i = 0; i < sent.getLength(); i++) {
    // String token = sent.getToken(i);
    // String posTag = sent.getPosTag(i);
    // String chunkTag = sent.getChunkTag(i);
    // System.out.println(token + " " + posTag + " " + chunkTag);
    // }

    // NOTE: ChunkedBinaryExtraction object includes getSentence() method
    // List<ChunkedBinaryExtraction> triples = new ArrayList<>();
    List<TripleWrapper> triplesWrapper = new ArrayList<>();
    for (ChunkedBinaryExtraction triple : reverb.extract(sent)) {
      double conf = confFunc.getConf(triple);
      //System.out.println(sent.getChunkTagsAsString());
      //System.out.println(sent.getPosTagsAsString());
      //System.out.println(triple);
      if (conf >= CONFIDENCE_THRESHOLD) {
        // Prints out extractions from the sentence.
        // Rel=" + triple.getRelation() + "; Arg2=" + triple.getArgument2());
        List<String> subjectPOSTags = triple.getArgument1().getPosTags();
        List<String> subjectChunkTags = triple.getArgument1().getChunkTags();
        // https://www.tutorialkart.com/opennlp/chunker-example-in-apache-opennlp/
        if (subjectChunkTags.contains("B-NP") && subjectPOSTags.contains("NNP")) {
          //System.out.println("adding Triple");
          //System.out.println("Conf=" + conf + "; Arg1=" + triple.getArgument1());
          triplesWrapper.add(new TripleWrapper(triple, newsArticle, conf));
        }
      }
      // System.out.println(triple);
      // System.out.println(triple.getPosTags());
      // System.out.println(triple.getChunkTags().toString());
    }
    return triplesWrapper;
  }

  @Override
  public StepType getStepType() {
    return StepType.EXTRACTOR;
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    return;
  }
  /*
  public static void main(String[] args) {
    String sentence = "Robert Mueller did not exonerate Donald Trump.";
    ReVerbOIE r = new ReVerbOIE();
    r.execute(sentence, null);
  }*/
}
