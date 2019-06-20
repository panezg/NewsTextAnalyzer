package org.newstextanalyzer.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.newstextanalyzer.NewsArticle;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Process one news article at a time, extracts sentences from the article
 * and feeds each sentence to the underlying pipeline steps
 * 
 * @author gpanez
 *  
 */
public class Pipeline implements IPipeline {
  private IPipelineStep[] pipelineSteps;
  private Map<StepType, Object> sink;
  private StanfordCoreNLP scNLPPipeline;
  private boolean active;
  private int numSentenceSeen;
  private int numSentenceTrigger;
  private int numTriple;
  private int numValidatedTriple;

  public Pipeline(IPipelineStep[] pipelineSteps) {
    this.pipelineSteps = pipelineSteps;
    this.sink = new HashMap<>();
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit");
    this.scNLPPipeline = new StanfordCoreNLP(props);
    this.active = true;
  }

  public void run(NewsArticle newsArticle) {
    if (!active) {
      return;
    }
    String docText = newsArticle.getBody().toString() + ".\n" + newsArticle.getTitle();
    CoreDocument document = new CoreDocument(docText);
    scNLPPipeline.annotate(document);

    boolean firstValidLinkedBoundSentenceFromNewsArticle = true;
    for (CoreSentence sentence : document.sentences()) {
      numSentenceSeen++;
      if (numSentenceSeen % 10000 == 0) {
        System.out.println("numSentenceSeen: " + numSentenceSeen);
      }
      // NOTE: Always parse first paragraph?
      /*
      if (numSentenceSeen < 100000) {
        continue;
      }
      
      if (numSentenceSeen == 1000) {
        this.active = false;
      }
      */
      String carrySentence = null;
      List<Object> carryExtras = new ArrayList<>();
      for (IPipelineStep pipelineStep : pipelineSteps) {
        switch (pipelineStep.getStepType()) {
          case CLASSIFIER:
            carrySentence = null;
            if ((Boolean) pipelineStep.execute(sentence.text())) {
              numSentenceTrigger++;
              carrySentence = sentence.text();
            }
            break;
          case EXTRACTOR:
            if (carrySentence != null) {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence,
                  newsArticle));
              numTriple += triplesWrapper.size();
              if (triplesWrapper.size() != 0) {
                carryExtras.add(triplesWrapper);
              }
            }
            break;
          case VALIDATOR:
            if (carrySentence != null && carryExtras.size() > 0) {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence,
                  carryExtras.get(0)));
              numValidatedTriple += triplesWrapper.size();
              carryExtras.set(0, triplesWrapper);
            }
            break;
          case LINKER:
          case INTERLINKER:
            if (carrySentence != null && carryExtras.size() > 0) {
              // NOTE: Indicating linkers if this is a new article, so they clean their subject references for the previous document
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence,
                  carryExtras.get(0), firstValidLinkedBoundSentenceFromNewsArticle));
              carryExtras.set(0, triplesWrapper);
              // NOTE: Required to be here, because the linker needs to create DS
              // before processing the first linked-bound valid sentence
              // Linker might nor run on the first sentence of the article because that one is not valid
              firstValidLinkedBoundSentenceFromNewsArticle = false;
            }
            break;
          case PERSISTOR:
            if (carrySentence != null && carryExtras.size() > 0) {
              pipelineStep.execute(carrySentence, carryExtras.get(0));
            }
            break;
        }
      }
    }
  }
  
  public Map<StepType, Object> finish() {
    System.out.println(this);
    for (IPipelineStep pipelineStep : pipelineSteps) {
      pipelineStep.finish(sink);
    }
    return sink;
  }

  public String toString() {
    String perc1 = String.valueOf(Math.round(((double) numSentenceTrigger / numSentenceSeen) * 100));
    String perc2 = String.valueOf(Math.round(((double) numValidatedTriple / numTriple) * 100));
    return "# trigger sentences: " + numSentenceTrigger + ", # sentences: " + numSentenceSeen + ", perc: " + perc1
        + "% \n# validatedTriple: " + numValidatedTriple + ", # triples: " + numTriple + ", perc: " + perc2 + "%";
  }
}