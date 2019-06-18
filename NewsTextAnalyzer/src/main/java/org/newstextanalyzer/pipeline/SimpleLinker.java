package org.newstextanalyzer.pipeline;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

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
  
  public SimpleLinker() {
    this.subjects = new HashSet<>();
  }
  
  @Override
  public Object execute(String sentence, Object... extra) {
    // TODO: Should try to link more, like among subjects with small variations
    // NOTE: Maybe Levenstein distance or additional cleaning could help by exploration of triple list on open data Google tool
    // NOTE: Maybe DBpedia lookup could help
    
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];

    for (TripleWrapper tripleWrapper : triplesWrapper) {
      ChunkedBinaryExtraction triple = tripleWrapper.getTriple();
      subjects.add(triple.getArgument1().toString());
      if (subjects.contains(triple.getArgument2().toString())) {
        tripleWrapper.setObjectMatched(true);
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
