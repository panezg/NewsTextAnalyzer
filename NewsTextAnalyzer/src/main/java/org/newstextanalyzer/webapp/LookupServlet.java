package org.newstextanalyzer.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.newstextanalyzer.query.VirtuosoLookupClient;

import com.google.gson.Gson;

enum LookupType {
  PERSON, COUNTRY, ROLE, SUBJECT
}

@WebServlet(name = "lookupservlet", urlPatterns = "/Lookup")
public class LookupServlet extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1185797270134950351L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LookupType lookupType = LookupType.valueOf(req.getParameter("lookupType"));

    List<Map<String, String>> records = null;
    if (lookupType == LookupType.PERSON) {
      String name = req.getParameter("name");
      records = VirtuosoLookupClient.getInstance().lookupPersonByName(name);
    } else if (lookupType == LookupType.COUNTRY) {
      String country = req.getParameter("countryName");
      records = VirtuosoLookupClient.getInstance().lookupCountryByName(country);
    } else if (lookupType == LookupType.ROLE) {
      String roleName = req.getParameter("roleName");
      // NOTE: Since lookup occurs over the resourceName
      roleName = roleName.replace(" ", "_");
      records = VirtuosoLookupClient.getInstance().lookupRoleByName(roleName);
    } else if (lookupType == LookupType.SUBJECT) {
      String subjectName = req.getParameter("subjectName");
      // NOTE: Since lookup occurs over the resourceName
      subjectName = subjectName.replace(" ", "_");
      records = VirtuosoLookupClient.getInstance().lookupSubjectByName(subjectName);
    }
    
    Gson gson = new Gson();
    String jsonData = gson.toJson(records);
    PrintWriter out = resp.getWriter();
    try {
      out.println(jsonData);
    } finally {
      out.close();
    }
  }
}
