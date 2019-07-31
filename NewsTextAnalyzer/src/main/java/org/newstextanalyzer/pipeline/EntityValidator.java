package org.newstextanalyzer.pipeline;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;

/**
 * Inspects triples and keeps those that have interesting entity types as subjects
 * by using Stanford Simple NLP lib
 * In addition, if there was a temporal expression within the sentence from
 * which the triples were extracted, it uses Natty to formalize the expression
 * and assigns such date to the subset of validated triples from such sentence
 *    
 * @author gpanez
 *
 */

public class EntityValidator implements IPipelineStep {
  // private static final String TEMP_ANALYSIS =
  // "/Users/gpanez/Documents/news/temp/analysis.txt";
  private MultiPatternMatcher<CoreMap> temporalMultiMatcher;
  private MultiPatternMatcher<CoreMap> personMultiMatcher;
  private MultiPatternMatcher<CoreMap> organizationMultiMatcher;

  private SimpleDateFormat dateSimpleFormatter;
  private Parser parser; 
  
  private Properties propsNoFineGrainNoNumericNoSUTime;
  private Properties propsNoFineGrain;

  private int countNerTagInvokation = 0;
  private int tokensProcessedNER = 0;
  
  private long totalTime = 0;
  
  /*
   * for (int i = 0; i < patternStrings.length; i++) { patterns[i] =
   * Pattern.compile(patternStrings[i]); }
   * 
   */
  // TODO: Important. Optimizing nerTags() invokation is suboptimal if that ends up increasing the number of tokens explored
  // Shifting to only invoking once nerTags but for every sentence than hit the Validator
  // reduced invokations by 10%, but increased total tokens by 500%, which end up in the new method taking twice as much time
  // However, CPU utilization according to NetBeans profiler hovers around 10% with the simple implementation while with the original
  // per subject hovers around 28%. Thus maybe, it is worth to parallelize both and compare again
  
  // TODO: Could have 2 props, one for just no fine grained
  // Another one for no fine grained no numerical SUTIME, because it inspects the subject
  // Remember that nofinegrained prop for the nertagger means no dissersing countries, titles, etc.
  // https://stanfordnlp.github.io/CoreNLP/ner.html
  
  // private PrintWriter pw;

  // TODO: Even when the NNP Pos Tag filtering step is included, doing it Nbeans per piece is faster by 20% even though it invokes more, because process half tokens
  /* Eclipse
   * # trigger sentences: 5450, # sentences: 12425, perc: 44% 
    # # trigger sentences: 5450, # sentences: 12425, perc: 44% 
    # validatedTriple: 1362, # triples: 2517, perc: 54%
    Number of NER invokations: 2071
    Number of tokens processed NER: 67509
    TotalTime: 28990
    AvgTime: 13.998068565910188
    AvgInvokation Token Size: 32.59729599227426
    NBEANS
    # trigger sentences: 5450, # sentences: 12425, perc: 44% 
    # validatedTriple: 1362, # triples: 2517, perc: 54%
    Number of NER invokations: 3745
    Number of tokens processed NER: 46531
    TotalTime: 22490
    AvgTime: 6.005340453938585
    AvgInvokation Token Size: 12.424833110814419
    
    //With improvements
    Number of NER invokations: 3745
    Number of tokens processed NER: 46531
    TotalTime: 6234
    AvgTime: 1.6646194926568758
    AvgInvokation Token Size: 12.424833110814419
   * */
  
  public EntityValidator() {
    List<TokenSequencePattern> temporalTokenSequencePatterns = new ArrayList<>();
    temporalTokenSequencePatterns.add(TokenSequencePattern.compile("([ner: TIME])+ [ner: DATE]"));
    temporalTokenSequencePatterns.add(TokenSequencePattern.compile("([ner: DATE] [ner: TIME])"));
    temporalTokenSequencePatterns.add(TokenSequencePattern.compile("([ner: DATE]){2,}"));
    temporalMultiMatcher = TokenSequencePattern.getMultiPatternMatcher(temporalTokenSequencePatterns);
    
    List<TokenSequencePattern> personSequencePatterns = new ArrayList<>();
    personSequencePatterns.add(TokenSequencePattern.compile("([ner: PERSON])+"));
    personMultiMatcher = TokenSequencePattern.getMultiPatternMatcher(personSequencePatterns);
    
    /*
     * try { pw = new PrintWriter(new File(TEMP_ANALYSIS)); } catch
     * (FileNotFoundException fnfe) { fnfe.printStackTrace(); throw new
     * RuntimeException(); }
     */
    dateSimpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
    parser = new Parser();
    
    //
    propsNoFineGrain = new Properties();
    propsNoFineGrain.setProperty("ner.applyFineGrained", "false");
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    Sentence nlpSentence = (Sentence) extra[1];
    List<TripleWrapper> validatedTriplesWrapper = new ArrayList<>();

    //Sentence NLPSentence = new Sentence(sentence);
    //Instant start = Instant.now();
    //List<String> NERTags = NLPSentence.nerTags(props);
    //Instant finish = Instant.now();
    //long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
    //totalTime += timeElapsed;
    //countNerTagInvokation++;
    //tokensProcessedNER += NERTags.size();
    
    //System.out.println("sent -> " + sentence);
    for (TripleWrapper tripleWrapper : triplesWrapper) {
      if (tripleWrapper.getTriple().getArgument1().getText().trim().length() == 0) {
        continue;
      }
      
      try {
        //Sentence rawSubject = new Sentence(tripleWrapper.getTriple().getArgument1().getText());
        // NOTE: Important. Sometimes the way Apache OpenNLP and Stanford CoreNLP tokenize
        // gives different list of tokens, usually by a margin of 1, because of a comma or
        // quote is included as part of another token by Apache OpenNLP, often in long sentences
        // Often, the tokenization performed by Stanford CoreNLP looks better;
        // However, there doesn't seem to be a way to make ReVerb work with Stanford CoreNLP
        // and no enough time to check
        // Notice that when URLs get tokenized the results could be completely different
        // with Apache OpenNLP having tens of more tokens, most of which looks incorrect.
        // So, the component will tolerate difference of one token, but no more,
        // even that could lead to weird off triples
        if (Math.abs(tripleWrapper.getTriple().getSentence().getTokens().size() - nlpSentence.tokens().size()) > 1) {
          continue;
        }
        ChunkedArgumentExtraction caeSubject = tripleWrapper.getTriple().getArgument1();

        int startIndex = caeSubject.getStart();
        int endIndex = caeSubject.getStart() + caeSubject.getLength();
        /*
        System.out.println(startIndex + ", " + endIndex);
        System.out.println(tripleWrapper.getTriple().toString());
        System.out.print("[");
        int i = 0;
        for (String token : tripleWrapper.getTriple().getSentence().getTokens()) {
          System.out.print(i + "->" + token + ", ");
          i++;
        }
        System.out.println("] ");
        int j = 0;
        System.out.print("[");
        for (Token token : nlpSentence.tokens()) {
          System.out.print(j + "->" + token.originalText() + ", ");
          j++;
        }
        System.out.println("] ");
        System.out.println(nlpSentence.nerTags());
        */
        List<String> subjectNERTags = nlpSentence.nerTags().subList(startIndex, endIndex);
        //List<String> subjectNERTags = rawSubject.nerTags(propsNoFineGrainNoNumericNoSUTime);
        
        // TODO: Add kind of subject meta info, so look up can try different queries
        // TODO: How to deal when there are 2 or more entity types in the subject
        // TODO: Senator Elizabeth Warren is recognized as TITLE, PERSON, PERSON, what should the subject be
        // what should the link be?
        // TODO: But, Theresa May's Government will become Theresa May if you follow such rule :/
        // TODO: The possessive after the PERSON is strong signal is not about the person
        // maybe if the sentences include said, discard them because they are usually citations'
        // TODO: using related to entities property might be simpler and faster than the above
        
        //List<String> nerTags = rawSubject.nerTags();        
        
        if (subjectNERTags.contains("PERSON") && subjectNERTags.contains("ORGANIZATION")) {
          //System.out.println("Both: " + nlpSentence.substring(startIndex, endIndex));
          //nlpSentence.
        }
        
        if (subjectNERTags.contains("PERSON") || subjectNERTags.contains("ORGANIZATION")) {
          SequenceMatchResult<CoreMap> subjectPersonAbout = extractPersonSequence(nlpSentence, startIndex, endIndex);
          if (subjectPersonAbout != null) {
            tripleWrapper.setSubjectPersonAbout(subjectPersonAbout);
            if (subjectPersonAbout.group().trim().equals(caeSubject.getText().trim())) {
              tripleWrapper.setSubjectOnlyPerson(true);
            }
          }
          validatedTriplesWrapper.add(tripleWrapper);
        }
        // NOTE: For example, allow triples about countries
        // NOTE: Consider adding additional validation rules or modifying the ones above
        // to only let triples pass with location info
        /*
         * if (token.ner().equals("LOCATION") || token.ner().equals("COUNTRY") ||
         * token.ner().equals("CITY") || token.ner().equals("STATE_OR_PROVINCE")) {
         * containsWhere = true; timeDateIndexes.add(token.index() - 1);
         * newsArticle.getTime(); }
         */
        // pw.println(sent);
        // pw.println(sent.nerTags());

      } catch (IllegalStateException ise) {
        System.out.println("Invalid subject according to NER {" + tripleWrapper.getTriple().getArgument1().getText() + "}");
        continue;
      }
    }
    
    if (validatedTriplesWrapper.size() > 0) {
      Sentence sent = new Sentence(sentence);
      Instant start = Instant.now();
      List<String> sentNERTags = sent.nerTags(propsNoFineGrain);
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
      totalTime += timeElapsed;
      countNerTagInvokation++;
      tokensProcessedNER += sentNERTags.size();

      String extractedDateExpression = extractTemporalMetaInfo(sent);
      String extractedDate = null;
      
      // NOTE: Getting news article date as reference
      Date referenceDate = triplesWrapper.get(0).getSourceDate();
      
      if (extractedDateExpression != null) {
        // NOTE: From the date expression within the sentence, try to determine an actual date
        // TODO: Need to add code to handle the case of ranges, when Natty detects from/to
        try {
          List<DateGroup> groups = parser.parse(extractedDateExpression, referenceDate);
          if (groups.size() > 0) {
            extractedDate = dateSimpleFormatter.format(groups.get(0).getDates().get(0));
          }
        } catch (NullPointerException npe) {
          System.out.println("npe for -> " + extractedDateExpression + " ; " + referenceDate);
          extractedDate = null;
        } catch (IllegalArgumentException iae) {
          System.out.println("iae for -> " + extractedDateExpression + " ; " + referenceDate);
          System.out.println("probably about bug within the Natty library, INAUGURATION DAY");
        }
        
        // TODO: Research if using more capabilities of Natty would be useful
        /*
        for (DateGroup group : groups) {
          List<Date> dates = group.getDates();
          int line = group.getLine();
          int column = group.getPosition();
          String matchingValue = group.getText();
          String syntaxTree = group.getSyntaxTree().toStringTree();
          Map<String, List<ParseLocation>> parseMap = group.getParseLocations();
          boolean isRecurring = group.isRecurring();
          Date recursUntil = group.getRecursUntil();
        }*/
      }

      //tripleWrapper.setExtractedDate(extractedDate);
      // TODO: Add location meta info
      String extractedLocationExpression = extractLocationMetaInfo(sent);

      for (TripleWrapper tripleWrapper : validatedTriplesWrapper) {
        // If there is a temporal expression then either save the date converted from it
        // after processing it through Natty; or the raw temporal expression if the expression was not
        // to transform
        if (extractedDateExpression != null) {
          if (extractedDate != null) {
            tripleWrapper.setExtractedDate(extractedDate);  
          } else {
            tripleWrapper.setExtractedDate(extractedDateExpression);
          }
        }
        if (extractedLocationExpression != null) {
          tripleWrapper.setExtractedLocation(extractedLocationExpression);
        }
      }
    }
    return validatedTriplesWrapper;
  }

  private String extractTemporalMetaInfo(Sentence sent) {
    List<CoreLabel> tokens = sent.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    // Finds all non-overlapping sequences using specified list of patterns
    // When multiple patterns overlap, matches selected based on priority, length,
    // etc.
    List<SequenceMatchResult<CoreMap>> matches = temporalMultiMatcher.findNonOverlapping(tokens);
    if (matches.size() > 0) {
      return matches.get(0).group();
    }
    return null;
  }

  private String extractLocationMetaInfo(Sentence sent) {
    return null;
  }

  private SequenceMatchResult<CoreMap> extractPersonSequence(Sentence nlpSentence, int startIndexSubject, int endIndexSubject) {
//  private SequenceMatchResult<CoreMap> extractPersonSequence(Sentence rawSubject) {
    //List<CoreLabel> tokens = rawSubject.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    List<CoreLabel> tokens = nlpSentence.asCoreLabels();
    tokens = tokens.subList(startIndexSubject, endIndexSubject);
    List<SequenceMatchResult<CoreMap>> matches = personMultiMatcher.findNonOverlapping(tokens);
    if (matches.size() > 0) {
      return matches.get(0);
      //return matches.get(0).group();
    }
    return null;
  }
  
  @Override
  public StepType getStepType() {
    return StepType.VALIDATOR;
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    System.out.println("Number of NER invokations: " + countNerTagInvokation);
    System.out.println("Number of tokens processed NER: " + tokensProcessedNER);
    System.out.println("TotalTime: " + totalTime);
    System.out.println("AvgTime: " + (totalTime * 1.0) / countNerTagInvokation);
    System.out.println("AvgInvokation Token Size: " + (tokensProcessedNER * 1.0) / countNerTagInvokation);
    // pw.close();
  }
}
