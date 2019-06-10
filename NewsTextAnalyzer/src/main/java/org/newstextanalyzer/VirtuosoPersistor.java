package org.newstextanalyzer;

import java.util.List;

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
    return null;
  }

  @Override
  public void clean() {
    return;
  }

  @Override
  public Type getType() {
    return Type.PERSISTOR;
  }
}
