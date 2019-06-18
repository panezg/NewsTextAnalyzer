package org.newstextanalyzer.lookup;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class EntityLookupClient {
  // TODO: A class to send lookups to a service (local or remote), and obtain a
  // resource
  // TODO: Might need to include some kind of verification or similarity in the
  // resource
  // like Levenstein distance. It might be worth looking at Silk, and their UI, as
  // inspiration
  // to other simple transformation that can take place
  private static final String DBPEDIA_LOOKUP_URL = "http://lookup.dbpedia.org/api/search/KeywordSearch";
  private static final String QCLASS_PARAM = "QueryClass";
  private static final String QSTRING_PARAM = "QueryString";
  
  private static final String RESULTS_FIELD = "results";
  private static final String URI_FIELD = "uri";
  private static final String REFCOUNT_FIELD = "refCount";
  private static final String LABEL_FIELD = "label";
  private static final String DESCRIPTION_FIELD = "description";
  
  public EntityLookupClient() {

  }

  // TODO: Could do look ups for organisation, subject
  // TODO: Could do look ups for place, subject
  
  public String lookup(String rawSubject) {
    HttpClient client = HttpClients.custom().build();

    // TODO: Is there better way to do this?
    String rawSubjectEscaped = rawSubject.replace(' ', '_');
    
    HttpUriRequest request = RequestBuilder.get()
        .setUri(DBPEDIA_LOOKUP_URL)
        .addParameter(QCLASS_PARAM, "person")
        .addParameter(QSTRING_PARAM, rawSubjectEscaped)
        .setHeader(HttpHeaders.ACCEPT, "application/json")
        .build();
    try {
      ResponseHandler<JSONObject> responseHandler = new ResponseHandler<JSONObject>() {
        @Override
        public JSONObject handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
          int status = response.getStatusLine().getStatusCode();
          if (status == 200) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
              JSONObject json = new JSONObject(EntityUtils.toString(entity));
              return json;
            } else {
              return null;
            }
          } else {
            return null;
          }
        }
      };
      JSONObject json = client.execute(request, responseHandler);
      
      JSONArray results = json.getJSONArray(RESULTS_FIELD);
      //System.out.println("-----------look up -------"  + rawSubjectEscaped + "----------------------");
      if (results != null && results.length() > 0) {
        /*
        for (int i = 0; i < results.length(); i++) {
          //System.out.println("match #" + (i + 1));
          JSONObject match = results.getJSONObject(i);
          //System.out.println(match.getString(URI_FIELD));
          //System.out.println(match.getInt(REFCOUNT_FIELD));
          //System.out.println(match.getString(LABEL_FIELD));
          //System.out.println(match.getString(DESCRIPTION_FIELD));
        }*/
        //return results.getJSONObject(0).getString(URI_FIELD);
        
        String tokens[] = rawSubject.split(" ");
        for (int i = 0; i < results.length(); i++) {
          //System.out.println("match #" + (i + 1));
          JSONObject match = results.getJSONObject(i); 
          
          
          String label = match.getString(LABEL_FIELD);
          for (String token : tokens) {
            // NOTE: This would only work for exact match with the DBpedia lookup
            // The response is too poor to be used, I believe
            //if (rawSubjectEscaped.contains(label)) {
            if (label.contains(token)) {
              return match.getString(URI_FIELD);
            }
          }
          //System.out.println(match.getString(DESCRIPTION_FIELD));
        }
        return null;
      }
      else {
        return null;
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}
