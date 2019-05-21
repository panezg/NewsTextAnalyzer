package org.newstextanalyzer;

public interface IPipelineStep {
  public enum Type { CLASSIFIER, EXTRACTOR, VALIDATOR, PERSISTOR };
  
  Object execute(String text, Object... extra);
 
  void clean();
  
  Type getType();
}
