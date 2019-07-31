package org.newstextanalyzer.lookup;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntitySearcher {

  private PersonResourceLookup prl;

  private static EntitySearcher instance;

  private EntitySearcher() {
    prl = PersonResourceLookup.getInstance();
    prl.initSearcher();
  }

  public static EntitySearcher getInstance() {
    if (instance == null) {
      instance = new EntitySearcher();
    }
    return instance;
  }

  public Record searchPerson(String label) {
    List<Record> records = prl.searchRecords(label, null);
    // NOTE: Might need to not do this sorting anymore, as the Lucene match might be the best indication
    // Sort will make people younger with part of the name being matched first
    // In any case, only do this when more than 1 record has same value, which is unlikely
    //Collections.sort(records, Collections.reverseOrder());
    if (records.size() > 0) {
      return records.get(0);
    }
    return null;
  }
  
  public void run(Set<String> rawSubjectsPendingInterlinkingAttempt, Map<String, String> rawSubjectsInterlinked) {
    int count = 0;
    for (String rawSubject : rawSubjectsPendingInterlinkingAttempt) {
      count++;
      if (count % 100 == 0 ) {
        System.out.println("Count: " + count);
      }
      Record record = searchPerson(rawSubject);
      if (record != null) {
        rawSubjectsInterlinked.put(rawSubject, record.getResourceName());
      }
      else {
        rawSubjectsInterlinked.put(rawSubject, null);
      }
    }
  }
}
