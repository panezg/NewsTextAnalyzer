package org.newstextanalyzer.pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryException;
import org.newstextanalyzer.lookup.EntityLookupClient;
import org.newstextanalyzer.lookup.EntityLookupClient2;
import org.newstextanalyzer.lookup.EntityLookupClient3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class InterLinker implements IPipelineStep {
  private Map<String, String> rawSubjectsInterlinked;
  private EntityLookupClient elc;
  private EntityLookupClient2 elc2;
  private EntityLookupClient3 elc3;
  private Gson gson;
  
  public InterLinker() {
    this.gson = new GsonBuilder().serializeNulls().create();
    loadInterlinkingMapping();
    this.elc = new EntityLookupClient();
    this.elc2 = new EntityLookupClient2();
    this.elc3 = new EntityLookupClient3();
  }
  
  @Override
  public Object execute(String sentence, Object... extra) {
    @SuppressWarnings("unchecked")
    List<TripleWrapper> triplesWrapper = (List<TripleWrapper>) extra[0];
    // NOTE: Interlinking is a batch process that occurs post pipeline processing
    // but that requires inspection of each triple. See finish() method
    for (TripleWrapper tripleWrapper : triplesWrapper) {
      // TODO: Find better location
      if (tripleWrapper.getScore() < ReVerbWrapper.CONFIDENCE_THRESHOLD) {
        continue;
      }      
      
      String rawSubject = tripleWrapper.getTriple().getArgument1().toString();
      if (!rawSubjectsInterlinked.containsKey(rawSubject)) {
        //String crossDBURI = elc.lookup(rawSubject);
        try {
          String crossDBURI = elc2.lookup(rawSubject);
          // NOTE: Adding even looks ups that resulted in no match to prevent repetitions
          // of those subjects from issuing more requests
          rawSubjectsInterlinked.put(rawSubject, crossDBURI);
        }
        catch (QueryException qe) {
          // NOTE: shouldn't reach this  code
          saveInterlinkingMapping();
          throw new RuntimeException();
        }
      }
    }
    return triplesWrapper;
  }
  
  private void loadInterlinkingMapping() {
    try {
      File file = new File("data/cross_entity_linking.txt");
      if (file.exists() && !file.isDirectory()) {
        BufferedReader bsr = new BufferedReader(new FileReader(file, Charset.forName("UTF-8")));
        Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();
        rawSubjectsInterlinked = gson.fromJson(bsr, typeOfT);
      } else {
        rawSubjectsInterlinked = new HashMap<>();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
  
  private void saveInterlinkingMapping() {
    try {
      Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/cross_entity_linking.txt"), Charset.forName("UTF-8")));
      Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();
      gson.toJson(rawSubjectsInterlinked, typeOfT, writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  @Override
  public void finish(Map<StepType, Object> sink) {
    System.out.println("Valid interlinked raw subjects in map: " + rawSubjectsInterlinked.size());
    elc3.run(rawSubjectsInterlinked);
    saveInterlinkingMapping();
    sink.put(this.getStepType(), rawSubjectsInterlinked);
  }
  
  public void test() {
    elc3.run(rawSubjectsInterlinked);
    saveInterlinkingMapping();
//    elc3.run(null);
  }

  @Override
  public StepType getStepType() {
    return StepType.INTERLINKER;
  }
  
  public static void main(String[] args) {
    System.out.println(new Date());
    InterLinker il = new InterLinker();
    il.test();
    System.out.println(new Date());
  }
}
