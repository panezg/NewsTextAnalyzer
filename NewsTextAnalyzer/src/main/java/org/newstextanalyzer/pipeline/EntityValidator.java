package org.newstextanalyzer.pipeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.newstextanalyzer.pipeline.IPipelineStep.StepType;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;

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

  private SimpleDateFormat dateTimeComplexFormatter;
  private SimpleDateFormat dateSimpleFormatter;
  private Parser parser; 
  
  // private PrintWriter pw;

  /*
   * for (int i = 0; i < patternStrings.length; i++) { patterns[i] =
   * Pattern.compile(patternStrings[i]); }
   * 
   */
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
    dateTimeComplexFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateSimpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
    parser = new Parser();
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    List<TripleWrapper> validatedTriplesWrapper = new ArrayList<>();

    for (TripleWrapper tripleWrapper : triplesWrapper) {
      if (tripleWrapper.getTriple().getArgument1().getText().trim().length() == 0) {
        continue;
      }
      try {
        Sentence rawSubject = new Sentence(tripleWrapper.getTriple().getArgument1().getText());
        // TODO: Add kind of subject meta info, so look up can try different queries
        // TODO: How to deal when there are 2 or more entity types in the subject
        // TODO: Senator Elizabeth Warren is recognized as TITLE, PERSON, PERSON, what should the subject be
        // what should the link be?
        // TODO: But, Theresa May's Government will become Theresa May if you follow such rule :/
        // TODO: The possessive after the PERSON is strong signal is not about the person
        // maybe if the sentences include said, discard them because they are usually citations'
        // TODO: using related to entities property might be simpler and faster than the above
        List<String> nerTags = rawSubject.nerTags();
        if (nerTags.contains("PERSON") || nerTags.contains("ORGANIZATION")) {
          String subjectPersonAbout = extractPersonSequence(rawSubject);
          System.out.println(rawSubject);
          System.out.println("---> " + subjectPersonAbout);
          String subject = new String();
          if (subject != null) {
            //TODO: Need another meta info since I cannot change the Chunked object
            //tripleWrapper.getTriple().
            validatedTriplesWrapper.add(tripleWrapper);
          }
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

      String extractedDateExpression = extractTemporalMetaInfo(sent);
      String extractedDate = null;
      
      // NOTE: Getting news article date as reference
      Date referenceDate;
      try {
        referenceDate = dateTimeComplexFormatter.parse(validatedTriplesWrapper.get(0).getNewsArticle().getTime());
      } catch(ParseException pe) {
        pe.printStackTrace();
        throw new RuntimeException();
      }
      
      if (extractedDateExpression != null) {
        // NOTE: From the date expression within the sentence, try to determine an actual date
        // TODO: Need to add code to handle the case of ranges, when Natty detects from/to
        
        List<DateGroup> groups = parser.parse(extractedDateExpression, referenceDate);
        if (groups.size() > 0) {
          extractedDate = dateSimpleFormatter.format(groups.get(0).getDates().get(0));
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
        } else {
          // If there was no temporal expression, then, for the moment, save the news article date
          // simple
          tripleWrapper.setExtractedDate(dateSimpleFormatter.format(referenceDate));
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

  private String extractPersonSequence(Sentence rawSubject) {
    List<CoreLabel> tokens = rawSubject.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    List<SequenceMatchResult<CoreMap>> matches = personMultiMatcher.findNonOverlapping(tokens);
    if (matches.size() > 0) {
      return matches.get(0).group();
    }
    return null;
  }
  
  @Override
  public StepType getStepType() {
    return StepType.VALIDATOR;
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    // pw.close();
  }
}