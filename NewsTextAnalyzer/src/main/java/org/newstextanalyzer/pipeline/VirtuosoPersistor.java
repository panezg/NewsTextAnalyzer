package org.newstextanalyzer.pipeline;

import java.util.List;
import java.util.Map;

import org.newstextanalyzer.virtuoso.VirtuosoClient;

public class VirtuosoPersistor implements IPipelineStep {
  private VirtuosoClient vc;
  
  public VirtuosoPersistor() {
    vc = VirtuosoClient.getInstance();
    vc.clean();
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    for (TripleWrapper tripleWrapper : triplesWrapper) {
      vc.saveTriple(tripleWrapper);
    }
    // TODO: Do check of repeated triples (s, p, o)
    // Option 1: Discard repeated ones
    // Option 2: Add it to list of news citations on the source property?
    return null;
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    return;
  }

  @Override
  public StepType getStepType() {
    return StepType.PERSISTOR;
  }
}
