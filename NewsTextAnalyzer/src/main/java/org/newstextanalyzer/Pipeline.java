package org.newstextanalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class Pipeline implements IPipeline {
  private IPipelineStep[] pipelineSteps;
  private StanfordCoreNLP scNLPPipeline;
  private boolean active;
  private int numSentenceSeen;
  private int numSentenceTrigger;
  private int numTriple;
  private int numValidatedTriple;

  public Pipeline(IPipelineStep[] pipelineSteps) {
    this.pipelineSteps = pipelineSteps;
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit");
    this.scNLPPipeline = new StanfordCoreNLP(props);
    this.active = true;
  }

  public void run(NewsArticle newsArticle) {
    if (!active) {
      return;
    }
    String docText = newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString(); 
    CoreDocument document = new CoreDocument(docText);
    scNLPPipeline.annotate(document);

    for (CoreSentence sentence : document.sentences()) {
      numSentenceSeen++;
      if (numSentenceSeen % 10000 == 0) {
        System.out.println("numSentenceSeen: " + numSentenceSeen);
      }
      if (numSentenceSeen == 10000) {
        for (IPipelineStep pipelineStep : pipelineSteps) {
          pipelineStep.clean();
        }
        this.active = false;
      }

      String carrySentence = null;
      List<Object> carryExtras = new ArrayList<>();
      for (IPipelineStep pipelineStep : pipelineSteps) {
        switch (pipelineStep.getType()) {
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
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep
                  .execute(carrySentence, newsArticle));
              numTriple += triplesWrapper.size();
              if (triplesWrapper.size() != 0) {
                carryExtras.add(triplesWrapper);
              }
            }
            break;
          case VALIDATOR:
            if (carrySentence != null && carryExtras.size() > 0) {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence, carryExtras.get(0)));
              numValidatedTriple += triplesWrapper.size();
              carryExtras.set(0, triplesWrapper);
            }
            break;
          case LINKER:
            if (carrySentence != null && carryExtras.size() > 0) {
              @SuppressWarnings("unchecked")
              List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) (pipelineStep.execute(carrySentence, carryExtras.get(0)));
              carryExtras.set(0, triplesWrapper);
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

  public String toString() {
    String perc1 = String.valueOf(Math.round(((double) numSentenceTrigger / numSentenceSeen) * 100));
    String perc2 = String.valueOf(Math.round(((double) numValidatedTriple / numTriple) * 100));
    return "# trigger sentences: " + numSentenceTrigger + ", # sentences: " + numSentenceSeen + ", perc: " + perc1
        + "% \n# validatedTriple: " + numValidatedTriple + ", # triples: " + numTriple + ", perc: " + perc2 + "%";
  }
}