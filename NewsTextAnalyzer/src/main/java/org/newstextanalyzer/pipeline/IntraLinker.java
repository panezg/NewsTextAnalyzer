package org.newstextanalyzer.pipeline;

import java.util.HashMap;
import java.util.HashSet;
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

public class IntraLinker implements IPipelineStep {
  private Set<String> subjects;
  private Set<String> subjectsFromNewsArticle;
  private Set<SequenceMatchResult<CoreMap>> referencedPeopleSubjectsFromNewsArticle;
  private Map<String, SequenceMatchResult<CoreMap>> matchedReferencedPeopleSubjectsFromNewsArticle;
  
  public IntraLinker() {
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
    List<TripleWrapper> validatedTriplesWrapper = (List<TripleWrapper>) extra[0];
    @SuppressWarnings("unchecked")
    Set<SequenceMatchResult<CoreMap>> referencedPeopleFromNewsArticle = (Set<SequenceMatchResult<CoreMap>>) extra[1];
    Boolean firstValidLinkedBoundSentenceFromNewsArticle = (Boolean) extra[2];
    if (firstValidLinkedBoundSentenceFromNewsArticle) {
      //NOTE: Queue-like collection that ensure uniqueness of elements, and preserves addition order
      //this.referencedPeopleSubjectsFromNewsArticle = new LinkedHashSet<>();
      this.matchedReferencedPeopleSubjectsFromNewsArticle = new HashMap<>();
    }

    for (TripleWrapper tripleWrapper : validatedTriplesWrapper) {
      ChunkedBinaryExtraction triple = tripleWrapper.getTriple();
      subjects.add(triple.getArgument1().toString());
      
      if (subjects.contains(triple.getArgument2().toString())) {
        tripleWrapper.setObjectMatched(true);
      }
      
      if (tripleWrapper.getSubjectPersonAbout() != null) {
        //referencedPeopleSubjectsFromNewsArticle.add(tripleWrapper.getSubjectPersonAbout());
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
          // TODO: Sometimes articles have many subtitles and people are referenced 
          // in the subtitles by last name, thus searching from beginning of the 
          // list won't find the full name; to increase linking coverage it would be 
          // ok to look forward at the end of the processing
          for (SequenceMatchResult<CoreMap> referencedPerson : referencedPeopleFromNewsArticle) {
            if (referencedPerson.groupNodes().size() > 1) {
              //List<? extends CoreMap> nodes = referencedPerson.groupNodes();
              if (referencedPerson.group().indexOf(triple.getArgument1().toString()) != -1) {
                tripleWrapper.setSubjectReplacement(referencedPerson);
                matchedReferencedPeopleSubjectsFromNewsArticle.put(triple.getArgument1().toString(), referencedPerson);
                // TODO: Maybe rank matches by how precise are, prefer longer matches when tie; 
                // don't break at first match
                break;
              }
            }
          }
        }
      }
    }
    return validatedTriplesWrapper;
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
