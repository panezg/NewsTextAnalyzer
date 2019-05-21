package org.newstextanalyzer;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ResignRERegEx implements IPipeline {

  private StanfordCoreNLP pipeline;

  //forms of verb reduction
  private final String[] patternStrings = { ".*PERSON, who resigned as TITLE.*",
      ".*resignation of TITLE PERSON.*",
      ".*resignation of TITLE, PERSON.*", 
      ".*PERSON resigned as TITLE.*", 
      ".*PERSON, resigned as TITLE.*",      
      ".*TITLE PERSON resigns.*",
      ".*TITLE, PERSON w? resigned.*",
      ".*PERSON 's resignation as TITLE.*",
      ".*resignations, including .* TITLE, PERSON.*",
      ".*PERSON, resigned from ORGANIZATION.*"};
  
  private Pattern[] patterns = new Pattern[patternStrings.length];

  public ResignRERegEx() {
    // set up pipeline properties
    Properties props = new Properties();
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");

    pipeline = new StanfordCoreNLP(props);
    for (int i = 0; i < patternStrings.length; i++) {
      patterns[i] = Pattern.compile(patternStrings[i]);
    }
  }

  public void run(NewsArticle newsArticle) {
    String docText = newsArticle.getTitle() + ".\n" + newsArticle.getBody().toString();
    CoreDocument document = new CoreDocument(docText);
    // annnotate the document
    pipeline.annotate(document);

    for (int i = 0; i < document.sentences().size(); i++) {
      CoreSentence sentence = document.sentences().get(i);

      List<CoreLabel> tokens = sentence.tokens();
      //System.out.println(sentence);
      //System.out.println(tokens);
      //System.out.println(sentence.posTags());
      //System.out.println(sentence.nerTags());
      StringBuilder sb = new StringBuilder();
      for (CoreLabel token : tokens) {
        if (token.ner().equals("O")) {
          if (token.tag().length() > 1) {
            sb.append(" ");
          }
          sb.append(token.value());
        } else {
          sb.append(" " + token.ner());
        }
      }
      //System.out.println(sb.toString());
      for (Pattern pattern : patterns) {
        Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.matches()) {
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
}
