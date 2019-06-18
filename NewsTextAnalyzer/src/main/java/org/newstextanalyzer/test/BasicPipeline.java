package org.newstextanalyzer.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class BasicPipeline {
  private StanfordCoreNLP pipeline;

  public BasicPipeline() {
    // set up pipeline properties
    Properties props = new Properties();
    // set the list of annotators to run
    // Annotators' observations
    // tokenize, ssplit, and pos are relatively cheap in terms of time and memory
    // ner requires lemma, and combined, ner and lemma require over 2 GBs of memory
    // parse takes memory consumption to 3.5 GB
    // props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
    // props.setProperty("annotators",
    // "tokenize,ssplit,pos,lemma,ner,parse,coref,kbp");
    // natlog, and openie to extract triples, but didn't work that well for me
    // props.setProperty("annotators",
    // "tokenize,ssplit,pos,lemma,ner,parse,natlog,openie");
    // coref helps solving the pronoun reference problem
    // kbp is for extracting triples too, but doesn't work that well neither
    // props.setProperty("annotators",
    // "tokenize,ssplit,pos,lemma,ner,parse,coref,kbp,natlog,openie");
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    // To set up the entire pipeline, it could take over 6 GB, and has never worked
    // on this laptop
    // props.setProperty("annotators",
    // "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
    // set a property for an annotator, in this case the coref annotator is being
    // set to use the neural algorithm
    // props.setProperty("coref.algorithm", "neural");
    // build pipeline
    pipeline = new StanfordCoreNLP(props);
    // create a document object
  }

  public void run(String text, String personName) {
    CoreDocument document = new CoreDocument(text);
    // annnotate the document
    pipeline.annotate(document);
    // examples

    for (int i = 0; i < document.sentences().size(); i++) {
      // text of the sentence
      CoreSentence sentence = document.sentences().get(i);

      boolean containsPerson = false;
      boolean containsWhen = false;
      boolean containsWhere = false;
      List<Integer> personIndexes = new ArrayList<Integer>();
      List<Integer> timeDateIndexes = new ArrayList<Integer>();

      // tokens
      List<CoreLabel> tokens = sentence.tokens();
      for (CoreLabel token : tokens) {
        if (token.ner().equals("PERSON") && token.word().equals(personName)) {
          containsPerson = true;
          personIndexes.add(token.index() - 1);

        }
        if (token.ner().equals("TIME") || token.ner().equals("DATE")) {
          containsWhen = true;
          timeDateIndexes.add(token.index() - 1);
        }
        if (token.ner().equals("LOCATION") || token.ner().equals("COUNTRY")
            || token.ner().equals("STATE_OR_PROVINCE")) {
          containsWhere = true;
          timeDateIndexes.add(token.index() - 1);
        }
      }
      if (!containsPerson || !containsWhen || !containsWhere) {
        continue;
      } else {
        System.out.println("Sentence: " + sentence);
      }
    }
  }

  public void run3(String text, String personName) {
    CoreDocument document = new CoreDocument(text);
    // annnotate the document
    pipeline.annotate(document);
    // examples

    for (int i = 0; i < document.sentences().size(); i++) {
      // text of the sentence
      CoreSentence sentence = document.sentences().get(i);

      boolean containsPerson = false;
      boolean containsDateTime = false;
      List<Integer> personIndexes = new ArrayList<Integer>();
      List<Integer> timeDateIndexes = new ArrayList<Integer>();

      // tokens
      List<CoreLabel> tokens = sentence.tokens();
      for (CoreLabel token : tokens) {
        if (token.ner().equals("PERSON") && token.word().equals(personName)) {
          containsPerson = true;
          personIndexes.add(token.index() - 1);

        }
        if (token.ner().equals("TIME") || token.ner().equals("DATE")) {
          containsDateTime = true;
          timeDateIndexes.add(token.index() - 1);
        }
      }
      if (!containsPerson || !containsDateTime) {
        continue;
      }

      for (int index : personIndexes) {
        for (int j = index + 1; j < tokens.size(); j++) {
          if (tokens.get(j).tag().startsWith("VB")) {

          }
        }
      }

      // Doesn't get very good results

      System.out.println("Open IE");
      // Get the OpenIE triples for the sentence
      Collection<RelationTriple> triples = sentence.coreMap()
          .get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
      // Print the triples
      for (RelationTriple triple : triples) {
        System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss()
            + "\t" + triple.objectLemmaGloss());
      }
      String sentenceText = sentence.text();
      System.out.println("Example: sentence");
      System.out.println(sentenceText);

      System.out.println("Tokens");
      System.out.println(tokens);

      // list of the part-of-speech tags for the sentence
      List<String> posTags = sentence.posTags();
      System.out.println("POS tags");
      System.out.println(posTags);

      // list of the ner tags for the sentence
      List<String> nerTags = sentence.nerTags();
      System.out.println("NER tags");
      System.out.println(nerTags);

      // constituency parse
      Tree constituencyParseTree = sentence.constituencyParse();
      Tree root = constituencyParseTree;
      System.out.println("Constituency parse tree");
      // System.out.println(constituencyParseTree);
      constituencyParseTree.indentedListPrint();
      System.out.println("Constituency parse tree grand children");
      System.out.println(constituencyParseTree.children()[0].children());
      for (Tree child : constituencyParseTree.children()[0].children()) {
        System.out.println("Printing each grand children");
        System.out.println(child);
      }
      // From a sentence (S)
      // Find the immediate descending noun phrase (NP), or noun (N)
      // From such NP or N, fing the header word

      // Find May as NP
      // Find parent until you find a parent that is NP and at the same level there is
      // a VP
      // Find the most shallow VB, which is now the predicate

      // From a sentence (S)
      // Find the immediate descending verb phrase (VP), or noun (NP)

      Set<Constituent> treeConstituents = constituencyParseTree.constituents(new LabeledScoredConstituentFactory());
      for (Constituent constituent : treeConstituents) {
        List<Tree> constituentLeaves = constituencyParseTree.getLeaves().subList(constituent.start(),
            constituent.end() + 1);
        System.out.println("constituentLeaves size: " + constituentLeaves.size());
        for (Tree constituentLeaf : constituentLeaves) {
          if (constituent.label() != null && constituent.label().toString().equals("NP")
              && constituentLeaf.toString().contains(personName)) {
            // constituent.toString().equals(personName)) {
            // if (constituent.label() != null &&
            // (constituent.label().toString().equals("VP") ||
            // constituent.label().toString().equals("NP"))) {
            System.out.println("constituent leave: " + constituentLeaf.toString());
            System.out.println("found constituent: " + constituent.toString());
            System.out.println(constituencyParseTree.getLeaves().subList(constituent.start(), constituent.end() + 1));
            // while (true) {
            // Tree parent = constituentLeaf.parent();
            // System.out.println("parent: " + parent.toString());
            // }
          }
        }
      }

      // dependency parse for the sentence
      SemanticGraph dependencyParse = sentence.dependencyParse();
      System.out.println("Example: dependency parse");
      System.out.println(dependencyParse);
      System.out.println();

      // kbp relations found in fifth sentence
      List<RelationTriple> relations = sentence.relations();
      System.out.println("Example: relation");
      System.out.println(relations);
      System.out.println();

      // entity mentions in the second sentence
      List<CoreEntityMention> entityMentions = sentence.entityMentions();
      System.out.println("Example: entity mentions");
      System.out.println(entityMentions);

      // coreference between entity mentions
//      CoreEntityMention originalEntityMention = document.sentences().get(3).entityMentions().get(1);
//      System.out.println("Example: original entity mention");
//      System.out.println(originalEntityMention);
//      System.out.println("Example: canonical entity mention");
//      System.out.println(originalEntityMention.canonicalEntityMention().get());
//      System.out.println();
//  
      // get document wide coref info
      Map<Integer, CorefChain> corefChains = document.corefChains();
      System.out.println("Example: coref chains for document");
      System.out.println(corefChains);

      // get quotes in document
//    List<CoreQuote> quotes = document.quotes();
//    CoreQuote quote = quotes.get(0);
//    System.out.println("Example: quote");
//    System.out.println(quote);
//    System.out.println();

      // original speaker of quote
      // note that quote.speaker() returns an Optional
//    System.out.println("Example: original speaker of quote");
//    System.out.println(quote.speaker().get());
//    System.out.println();
//
//    // canonical speaker of quote
//    System.out.println("Example: canonical speaker of quote");
//    System.out.println(quote.canonicalSpeaker().get());
//    System.out.println();

      System.out.println();
    }
  }
  
  public void runTest(String text) {
    CoreDocument document = new CoreDocument(text);
    // annnotate the document
    pipeline.annotate(document);
    // examples

    for (int i = 0; i < document.sentences().size(); i++) {
      // text of the sentence
      CoreSentence sentence = document.sentences().get(i);

      boolean containsPerson = false;
      boolean containsWhen = false;
      boolean containsWhere = false;
      List<Integer> personIndexes = new ArrayList<Integer>();
      List<Integer> timeDateIndexes = new ArrayList<Integer>();

      // tokens
      List<CoreLabel> tokens = sentence.tokens();
      for (CoreLabel token : tokens) {
        /*
        if (token.ner().equals("PERSON") && token.word().equals(personName)) {
          containsPerson = true;
          personIndexes.add(token.index() - 1);

        }
        if (token.ner().equals("TIME") || token.ner().equals("DATE")) {
          containsWhen = true;
          timeDateIndexes.add(token.index() - 1);
        }
        if (token.ner().equals("LOCATION") || token.ner().equals("COUNTRY")
            || token.ner().equals("STATE_OR_PROVINCE")) {
          containsWhere = true;
          timeDateIndexes.add(token.index() - 1);
        }*/
        System.out.print(token);
        System.out.println(" " + token.ner()); 
      }
      /*if (!containsPerson || !containsWhen || !containsWhere) {
        continue;
      } else {
        System.out.println("Sentence: " + sentence);
      }*/
    }
  }
  
  public void runTest2(String text) {
    Sentence subject = new Sentence(text);
    // TODO: Add kind of subject meta info, so look up can try different queries
    // TODO: How to deal when there are 2 or more entity types in the subject
    List<String> nerTags = subject.nerTags();
    int i = 0;
    boolean sequence = false;
    StringBuilder sb = new StringBuilder();
    
    MultiPatternMatcher<CoreMap> multiMatcher;
      
    List<TokenSequencePattern> tokenSequencePatterns = new ArrayList<>();
    tokenSequencePatterns.add(TokenSequencePattern.compile("([ner: PERSON])+"));
    multiMatcher = TokenSequencePattern.getMultiPatternMatcher(tokenSequencePatterns);
    List<CoreLabel> tokens = subject.asCoreLabels(Sentence::posTags, Sentence::nerTags);
    List<SequenceMatchResult<CoreMap>> matches = multiMatcher.findNonOverlapping(tokens);
    if (matches.size() > 0) {
      System.out.println(matches.get(0).group());
    }
    
    /*
    while (i < nerTags.size()) {
      if (nerTags.get(i).equals("PERSON")) {
        // If first token that has NER tag as PERSON
        if (sb.length() != 0) {
          sequence = true;
          sb.append(" ");
        }
        sb.append(subject.originalText(i));
        int j = i + 1;
        if (j < nerTags.size() && !nerTags.get(j).equals("PERSON")) {
          break;
        }
      }
      i++;
    }*/
    System.out.println(nerTags);
    //System.out.println(sb);
  }
  
  public static void main(String[] args) {
    BasicPipeline bp = new BasicPipeline();
    bp.runTest("Senator Elizabeth Warren");
    System.out.println("---------");
    bp.runTest2("Senator Elizabeth Warren");
  }
}