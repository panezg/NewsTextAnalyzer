package org.newstextanalyzer;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class InterestingSentencesPipeline {
  private StanfordCoreNLP pipeline;

  public InterestingSentencesPipeline() {
    // set up pipeline properties
    Properties props = new Properties();
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");

    pipeline = new StanfordCoreNLP(props);
  }

  public void run(String text, String personName) {
    CoreDocument document = new CoreDocument(text);
    // annnotate the document
    pipeline.annotate(document);

    for (int i = 0; i < document.sentences().size(); i++) {
      CoreSentence sentence = document.sentences().get(i);

      boolean containsPerson = false;
      boolean containsWhen = false;
      // boolean containsWhere = false;
      boolean containsWhere = true;
      boolean constainsResign = false;

      List<CoreLabel> tokens = sentence.tokens();
      for (CoreLabel token : tokens) {
        if (token.ner().equals("PERSON") && (personName == null || token.word().equals(personName))) {
          containsPerson = true;
        }
        if (token.value().contains("resign")) {
          constainsResign = true;
        }
        if (token.ner().equals("TIME") || token.ner().equals("DATE")) {
          containsWhen = true;
        }
        /*
         * if (token.ner().equals("LOCATION") || token.ner().equals("COUNTRY") ||
         * token.ner().equals("STATE_OR_PROVINCE")) { containsWhere = true; }
         */
      }
      if (!containsPerson || !containsWhen || !containsWhere || !constainsResign) {
        continue;
      } else {
        System.out.println(sentence);
      }
    }
  }
}