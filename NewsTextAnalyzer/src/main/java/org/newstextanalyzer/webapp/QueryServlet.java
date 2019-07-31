package org.newstextanalyzer.webapp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.newstextanalyzer.query.VirtuosoQueryClient;

enum QueryType {
  PERSON, ROLE, EVENT, SUBJECT, SENTIMENT
}

@WebServlet(name = "queryservlet", urlPatterns = "/Query")
public class QueryServlet extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1478925163444874344L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    QueryType queryType = QueryType.valueOf(req.getParameter("queryType"));
    req.setAttribute("responseType", queryType.toString());
    String strTemporalFilter = req.getParameter("temporalFilter");
    boolean temporalFilter = false;
    if (strTemporalFilter != null && strTemporalFilter.equals("on")) {
      temporalFilter = true;
    }
    String startDate = req.getParameter("startDate");
    String endDate = req.getParameter("endDate");
    if (queryType == QueryType.PERSON) {
      String personResourceName = req.getParameter("personResourceName");
      Logger.getLogger(this.getClass().getName()).log(Level.INFO, "startDate: " + startDate);
      Logger.getLogger(this.getClass().getName()).log(Level.INFO, "endDate: " + endDate);

      List<Map<String, String>> records = VirtuosoQueryClient.getInstance().queryByPerson(personResourceName,
          temporalFilter, startDate, endDate);
      req.setAttribute("records", records);
      RequestDispatcher view = req.getRequestDispatcher("result.jsp");
      view.forward(req, resp);
    } else if (queryType == QueryType.ROLE) {
      String countryResourceName = req.getParameter("countryResourceName");
      String roleResourceName = req.getParameter("roleResourceName");

      List<Map<String, String>> records = VirtuosoQueryClient.getInstance().queryByRoleCountry(roleResourceName,
          countryResourceName, temporalFilter, startDate, endDate);
      req.setAttribute("records", records);
      RequestDispatcher view = req.getRequestDispatcher("result.jsp");
      view.forward(req, resp);
    } else if (queryType == QueryType.EVENT) {
      String event = req.getParameter("event");

      List<Map<String, String>> records = VirtuosoQueryClient.getInstance().queryByEvent(event, temporalFilter,
          startDate, endDate);
      req.setAttribute("records", records);
      RequestDispatcher view = req.getRequestDispatcher("result.jsp");
      view.forward(req, resp);
    } else if (queryType == QueryType.SUBJECT) {
      String subjectResourceName = req.getParameter("subjectResourceName");
      List<Map<String, String>> records = VirtuosoQueryClient.getInstance().queryBySubject(subjectResourceName,
          temporalFilter, startDate, endDate);
      req.setAttribute("records", records);
      RequestDispatcher view = req.getRequestDispatcher("result.jsp");
      view.forward(req, resp);
    } else if (queryType == QueryType.SENTIMENT) {
      String sentimentThreshold = req.getParameter("sentimentTheshold");
      String sentimentOperator = req.getParameter("sentimentOperator");
      String sentimentOrder = req.getParameter("sentimentOrder");

      List<Map<String, String>> records = VirtuosoQueryClient.getInstance().queryBySentiment(sentimentThreshold, sentimentOperator,
          sentimentOrder, temporalFilter, startDate, endDate);
      req.setAttribute("records", records);
      RequestDispatcher view = req.getRequestDispatcher("result.jsp");
      view.forward(req, resp);
    }
  }
}