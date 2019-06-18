package org.newstextanalyzer;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.newstextanalyzer.pipeline.IPipeline;
import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ResignREBoot implements IPipeline {

  private StanfordCoreNLP pipeline;

  private final WordsPair[] seeds = { new WordsPair("Boris Johnson", "Secretary of State"),
      new WordsPair("David Davis", "Secretary of State"), new WordsPair("Al Franken", "Senator"),
      new WordsPair("Jeremy Heywood", "Secretary"), new WordsPair("Tracey Crouch", "Minister") };

  private Pattern[] patterns = new Pattern[seeds.length * 2];

  public ResignREBoot() {
    // set up pipeline properties
    Properties props = new Properties();
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit");

    pipeline = new StanfordCoreNLP(props);
    for (int i = 0; i < seeds.length; i++) {
      patterns[i * 2] = Pattern.compile(".*" + seeds[i].getWord1() + ".*");
      patterns[i * 2 + 1] = Pattern.compile(".*" + seeds[i].getWord2() + ".*");
    }
  }

  public void run(NewsArticle newsArticle) {
    String docText = newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString();
    CoreDocument document = new CoreDocument(docText);
    // annnotate the document
    pipeline.annotate(document);

    for (int i = 0; i < document.sentences().size(); i++) {
      CoreSentence sentence = document.sentences().get(i);

      for (int j = 0; j < seeds.length; j++) {
        Matcher matcher1 = patterns[j * 2].matcher(sentence.text());
        Matcher matcher2 = patterns[j * 2 + 1].matcher(sentence.text());
        if (matcher1.matches() && matcher2.matches()) {
          System.out.println("SENTENCE MATCH: " + sentence.text());
        }
//        while (matcher.find()) {
//          System.out.print("Start index: " + matcher.start());
//          System.out.print("End index: " + matcher.end() + " ");
//          System.out.println("MATCH: " + matcher.group());
//        }
      }
    }
  }
  
  @Override
  public Map<StepType, Object> finish() {
    // TODO Auto-generated method stub
    return null;
  }
}
