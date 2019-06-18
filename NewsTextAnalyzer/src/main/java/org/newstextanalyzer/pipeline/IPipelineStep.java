package org.newstextanalyzer.pipeline;

import java.util.Map;

public interface IPipelineStep {
  public enum StepType { CLASSIFIER, EXTRACTOR, VALIDATOR, LINKER, INTERLINKER, PERSISTOR };
  
  Object execute(String sentence, Object... extra);
 
  void finish(Map<StepType, Object> sink);
  
  StepType getStepType();
}
