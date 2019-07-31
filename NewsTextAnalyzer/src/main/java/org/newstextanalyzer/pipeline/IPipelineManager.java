package org.newstextanalyzer.pipeline;

import java.util.Map;

import org.newstextanalyzer.NewsArticle;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

public interface IPipelineManager {
  void run(NewsArticle newsArticle);
  
  Map<StepType, Object> finish(); 
}
