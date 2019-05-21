package org.newstextanalyzer;

public interface IPipelineStep {
  public enum Type { CLASSIFIER, EXTRACTOR, VALIDATOR, LINKER, PERSISTOR };
  
  Object execute(String sentence, Object... extra);
 
  void clean();
  
  Type getType();
}
