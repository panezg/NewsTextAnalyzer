package org.newstextanalyzer.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.newstextanalyzer.NewsArticle;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;

/**
 * Process one news article at a time, extracts sentences from the article
 * and feeds each sentence to the underlying pipeline steps
 * 
 * @author gpanez
 *  
 */
public class PipelineManager implements IPipelineManager {
  private IPipelineStep[] pipelineSteps;
  private Map<StepType, Object> sink;
  private StanfordCoreNLP scNLPPipeline;
  private boolean active;
  private int numSentenceSeen;
  private int numSentenceTrigger;
  private int numTriple;
  private int numValidatedTriple;

  public PipelineManager(IPipelineStep[] pipelineSteps) {
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
    boolean firstSentenceFromNewsArticle = true;
    for (CoreSentence sentence : document.sentences()) {
      numSentenceSeen++;
      if (numSentenceSeen % 10000 == 0) {
        System.out.println("numSentenceSeen: " + numSentenceSeen);
      }
      // NOTE: Always parse first paragraph?
      /*
      if (numSentenceSeen < 4760000) {
        continue;
      }
      
      if (numSentenceSeen == 1000) {
        this.active = false;
      }
      */
      String carrySentence = null;
      List<Object> carryExtras = new ArrayList<>();
      pipelineStepLoop:
      for (IPipelineStep pipelineStep : pipelineSteps) {
        switch (pipelineStep.getStepType()) {
          case CLASSIFIER:
            carrySentence = null;
            /*
            if ((Boolean) pipelineStep.execute(sentence.text())) {
              numSentenceTrigger++;
              carrySentence = sentence.text();
            }*/
            // TODO: Even if no filtering is done as the first step, doing filtering 
            // of the triples post entity recognition (after the referencer) might be worth it
            carrySentence = sentence.text();
            break;
          case REFERENCER:
            if (carrySentence == null) {
              break pipelineStepLoop;
            }
            Object[] result = (Object[]) (pipelineStep.execute(carrySentence, firstSentenceFromNewsArticle));
            
            Sentence nlpSentence = (Sentence) result[0];
            @SuppressWarnings("unchecked")
            Set<SequenceMatchResult<CoreMap>> referencedPeopleFromNewsArticle = (Set<SequenceMatchResult<CoreMap>>) result[1];
            // Index=0
            carryExtras.add(nlpSentence);
            // Index=1
            carryExtras.add(referencedPeopleFromNewsArticle);
            firstSentenceFromNewsArticle = false;
            break;
          case EXTRACTOR:
            if (carrySentence == null) {
              break pipelineStepLoop;
            } else {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence, newsArticle));
              numTriple += triplesWrapper.size();
              if (triplesWrapper.size() == 0) {
                break pipelineStepLoop;
              } else {
                // Index=2
                carryExtras.add(triplesWrapper);
              }
            }
            break;
          case VALIDATOR:
            if (carrySentence == null || carryExtras.size() != 3) {
              System.out.println(carrySentence);
              break pipelineStepLoop;
            } else {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> validatedTriplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence, carryExtras.get(2), carryExtras.get(0)));
              numValidatedTriple += validatedTriplesWrapper.size();
              carryExtras.set(2, validatedTriplesWrapper);
            }
            break;
          case LINKER:
          case INTERLINKER:
            if (carrySentence == null || carryExtras.size() != 3) {
              System.out.println(carrySentence);
              break pipelineStepLoop;
            } else {
              // NOTE: Indicating linkers if this is a new article, so they clean their subject references for the previous document
              @SuppressWarnings("unchecked")
              List<TripleWrapper> decoratedTriplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence,
                  carryExtras.get(2), carryExtras.get(1), firstValidLinkedBoundSentenceFromNewsArticle));
              carryExtras.set(2, decoratedTriplesWrapper);
              // NOTE: Required to be here, because the linker needs to create DS
              // before processing the first linked-bound valid sentence
              // Linker might nor run on the first sentence of the article because that one is not valid
              firstValidLinkedBoundSentenceFromNewsArticle = false;
            }
            break;
          case PERSISTOR:
            if (carrySentence != null && carryExtras.size() > 0) {
              //System.out.println("-->" + carrySentence);
              pipelineStep.execute(carrySentence, carryExtras.get(2));
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