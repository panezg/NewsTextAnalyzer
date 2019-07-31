package org.newstextanalyzer.pipeline;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;

public class Referencer implements IPipelineStep {
  // NOTE: Queue-like collection that ensure uniqueness of elements, and preserves addition order
  private Set<SequenceMatchResult<CoreMap>> referencedPeopleFromNewsArticle;
  
  private MultiPatternMatcher<CoreMap> personMultiMatcher;
  private Properties propsNoFineGrainNoNumericNoSUTime;
  
  public Referencer() {
    propsNoFineGrainNoNumericNoSUTime = new Properties();
    propsNoFineGrainNoNumericNoSUTime.setProperty("ner.applyFineGrained", "false");
    propsNoFineGrainNoNumericNoSUTime.setProperty("ner.applyNumericClassifiers", "false");
    propsNoFineGrainNoNumericNoSUTime.setProperty("ner.useSUTime", "false");

    List<TokenSequencePattern> personSequencePatterns = new ArrayList<>();
    personSequencePatterns.add(TokenSequencePattern.compile("([ner: PERSON])+"));
    personMultiMatcher = TokenSequencePattern.getMultiPatternMatcher(personSequencePatterns);
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    Boolean firstSentenceFromNewsArticle = (Boolean) extra[0];
    if (firstSentenceFromNewsArticle) {
      this.referencedPeopleFromNewsArticle = new LinkedHashSet<>();
    }
    Sentence sent = new Sentence(sentence);
    sent.nerTags(propsNoFineGrainNoNumericNoSUTime);
    extractPeopleReferences(sent);
    return new Object[] { sent, referencedPeopleFromNewsArticle };
  }
  
  private void extractPeopleReferences(Sentence sent) {
    //List<CoreLabel> tokens = rawSubject.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    List<CoreLabel> tokens = sent.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    //tokens = tokens.subList(startIndexSubject, endIndexSubject);
    List<SequenceMatchResult<CoreMap>> matches = personMultiMatcher.findNonOverlapping(tokens);
    for (SequenceMatchResult<CoreMap> match : matches) {
      referencedPeopleFromNewsArticle.add(match);  
    }
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
  }

  @Override
  public StepType getStepType() {
    return StepType.REFERENCER;
  }
}