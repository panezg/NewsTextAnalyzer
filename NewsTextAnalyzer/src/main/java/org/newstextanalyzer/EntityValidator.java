package org.newstextanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class EntityValidator implements IPipelineStep {
  private static final String TEMP_ANALYSIS = "/Users/gpanez/Documents/news/temp/analysis.txt";
  private MultiPatternMatcher multiMatcher;
  
  private PrintWriter pw;

  /*
   * for (int i = 0; i < patternStrings.length; i++) { patterns[i] =
   * Pattern.compile(patternStrings[i]); }
   * 
   */
  public EntityValidator() {
    List<TokenSequencePattern> tokenSequencePatterns = new ArrayList<>();
    tokenSequencePatterns.add(TokenSequencePattern.compile("([ner: TIME])+ [ner: DATE]"));
    tokenSequencePatterns.add(TokenSequencePattern.compile("([ner: DATE] [ner: TIME])"));
    tokenSequencePatterns.add(TokenSequencePattern.compile("([ner: DATE]){2,}"));
    multiMatcher = TokenSequencePattern.getMultiPatternMatcher(tokenSequencePatterns);
    
    try {
      pw = new PrintWriter(new File(TEMP_ANALYSIS));
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
      throw new RuntimeException();
    }
  }

  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    List<TripleWrapper> validatedTriplesWrapper = new ArrayList<>();

    for (TripleWrapper tripleWrapper : triplesWrapper) {
      Sentence subject = new Sentence(tripleWrapper.getTriple().getArgument1().getText());
      List<String> nerTags = subject.nerTags();
      if (nerTags.contains("PERSON") || nerTags.contains("ORGANIZATION")) {
        validatedTriplesWrapper.add(tripleWrapper);
        // pw.println("Arg1=" + triple.getArgument1() + "; Rel=" + triple.getRelation()
        // + "; Arg2=" + triple.getArgument2());

      }
    }
    
    if (validatedTriplesWrapper.size() > 0) {
      Sentence sent = new Sentence(sentence);

      String extractedDate = extractTemporalMetaInfo(sent);
      // TODO: Add location meta info
      String extractedLocation = extractLocationMetaInfo(sent);

      for (TripleWrapper tripleWrapper : validatedTriplesWrapper) {
        if (extractedDate != null) { 
          tripleWrapper.setExtractedDate(extractedDate);
        }
        if (extractedLocation != null) {
          tripleWrapper.setExtractedLocation(extractedLocation);
        }
      }

      /*
       * if (token.ner().equals("LOCATION") || token.ner().equals("COUNTRY") ||
       * token.ner().equals("CITY") || token.ner().equals("STATE_OR_PROVINCE")) {
       * containsWhere = true; timeDateIndexes.add(token.index() - 1);
       * newsArticle.getTime(); }
       */
      // pw.println(sent);
      // pw.println(sent.nerTags());

    }
    return validatedTriplesWrapper;
  }

  private String extractTemporalMetaInfo(Sentence sent) {
    List<CoreLabel> tokens = sent.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    // Finds all non-overlapping sequences using specified list of patterns
    // When multiple patterns overlap, matches selected based on priority, length,
    // etc.
    List<SequenceMatchResult<CoreMap>> matches = multiMatcher.findNonOverlapping(tokens);
    if (matches.size() > 0) {
      return matches.get(0).group();
    }
    return null;
  }

  private String extractLocationMetaInfo(Sentence sent) {
    return null;
  }

  @Override
  public Type getType() {
    return Type.VALIDATOR;
  }

  @Override
  public void clean() {
    pw.close();
  }
}
