package org.newstextanalyzer.pipeline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * Tracks all distinct triple subjects observed during processing, and attempts 
 * to indicate that there is match between the triple object and a triple subject
 * observed before
 *  
 * @author gpanez
 *
 */

public class SimpleLinker implements IPipelineStep {
  private Set<String> subjects;
  private Set<String> subjectsFromNewsArticle;
  private Set<SequenceMatchResult<CoreMap>> referencedPeopleSubjectsFromNewsArticle;
  private Map<String, SequenceMatchResult<CoreMap>> matchedReferencedPeopleSubjectsFromNewsArticle;
  
  public SimpleLinker() {
    this.subjects = new HashSet<>();
    // 
    /*
    Iterator<?> i = queue.iterator();
    ...
    Object next = i.next();
    i.remove();
    */
  }
  
  @Override
  public Object execute(String sentence, Object... extra) {
    // TODO: Should try to link more, like among subjects with small variations
    // NOTE: Maybe Levenstein distance or additional cleaning could help by exploration of triple list on open data Google tool
    // NOTE: Maybe DBpedia lookup could help
    
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    Boolean firstValidLinkedBoundSentenceFromNewsArticle = (Boolean) extra[1];
    if (firstValidLinkedBoundSentenceFromNewsArticle) {
      //NOTE: Queue-like collection that ensure uniqueness of elements, and preserves addition order
      this.subjectsFromNewsArticle = new LinkedHashSet<>();
      this.referencedPeopleSubjectsFromNewsArticle = new LinkedHashSet<>();
      this.matchedReferencedPeopleSubjectsFromNewsArticle = new HashMap<>();
    }

    for (TripleWrapper tripleWrapper : triplesWrapper) {
      ChunkedBinaryExtraction triple = tripleWrapper.getTriple();
      subjects.add(triple.getArgument1().toString());
      subjectsFromNewsArticle.add(triple.getArgument1().toString());
      
      if (subjects.contains(triple.getArgument2().toString())) {
        tripleWrapper.setObjectMatched(true);
      }
      
      // TODO: More rules from examining data
      // If subject is just one word, and thus by default a Person, since if went through the validator 

      if (triple.getArgument1().getLength() == 1) {
        // Find a referenced person from the article with only 2 words that contain this subject
        // First check if a match already occurred before, and if so use the referred Person
        if (matchedReferencedPeopleSubjectsFromNewsArticle.containsKey(triple.getArgument1().toString())) {
          tripleWrapper.setSubjectReplacement(matchedReferencedPeopleSubjectsFromNewsArticle.get(triple.getArgument1().toString()));
        }
        else {
          // If not, start searching from the beginning of the queue
          for (SequenceMatchResult<CoreMap> referencedPerson : referencedPeopleSubjectsFromNewsArticle) {
            if (referencedPerson.groupNodes().size() == 2) {
              //List<? extends CoreMap> nodes = referencedPerson.groupNodes();
              if (referencedPerson.group().indexOf(triple.getArgument1().toString()) != -1) {
                tripleWrapper.setSubjectReplacement(referencedPerson);
                matchedReferencedPeopleSubjectsFromNewsArticle.put(triple.getArgument1().toString(), referencedPerson);
              }
            }
          }
        }
      }
      if (tripleWrapper.getSubjectPersonAbout() != null) {
        referencedPeopleSubjectsFromNewsArticle.add(tripleWrapper.getSubjectPersonAbout());
      }
    }
    return triplesWrapper;
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    return;
  }

  @Override
  public StepType getStepType() {
    return StepType.LINKER;
  }

}
