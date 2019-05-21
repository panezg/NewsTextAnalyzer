package org.newstextanalyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class SimpleLinker implements IPipelineStep{
  private Set<String> subjects;
  
  public SimpleLinker() {
    this.subjects = new HashSet<>();
  }
  
  @Override
  public Object execute(String sentence, Object... extra) {
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
  public void clean() {
    return;
  }

  @Override
  public Type getType() {
    return Type.LINKER;
  }

}
