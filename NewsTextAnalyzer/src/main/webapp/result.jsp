<%@ page import ="java.util.*" %>
<%@ page import ="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>KB Query Demo Results</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="stylesResults.css">
</head>
<body>
<center>
<h1>
    Results
</h1>
<p>Legend: 
  <span class="subject">subject</span>
  <span class="predicate">predicate</span>
  <span class="object">object</span>
</p>
<table>
  <tr>
    <th>Triple</th>
    <th>Sentence</th>
    <th>Source</th>
    <th>Date</th>
    <th>Sentiment Score</th>
    <th>Tweet Link</th>
  </tr>
<%
List<Map<String, String>> records= (List<Map<String, String>>) request.getAttribute("records");
Iterator it = records.iterator();

while (it.hasNext()) {
  Map<String, String> record = (Map<String, String>) it.next();
  String responseType = (String) request.getAttribute("responseType");
  if (responseType.equals("PERSON") || responseType.equals("ROLE") || responseType.equals("EVENT") || responseType.equals("SUBJECT") || responseType.equals("SENTIMENT")) {
    out.println("<tr>");
    out.println("<td>");
    out.println("<span class=\"subject\">" + record.get("rawSubject") + "</span>");
    out.println("<span class=\"predicate\">" + record.get("rawPredicate") + "</span>");
    out.println("<span class=\"object\">" + record.get("rawObject") + "</span>");
    out.println("</td>");
    out.println("<td><div class=\"tooltip\"><img src=\"quotes.png\" height=\"30px\" width=\"30px\" /><span class=\"tooltiptext\">" + record.get("hasSourceSentence") + "</span></div></td>");
    out.println("<td><a href=\"" + record.get("hasSource") + "\">&#9432</td>");
    out.println("<td>" + record.get("date") + "</td>");
    //out.println("<td>" + record.get("tokenList") + "</td>");
    out.println("<td>" + record.get("sentimentScore") + "</td>");
    if (record.get("hasTweetSource").length() > 0) {
      out.println("<td><a href=\"" + record.get("hasTweetSource") + "\"><img src=\"Twitter_Logo_Blue.png\" height=\"30px\" width=\"30px\" /></td>");
    } else {
      out.println("<td></td>");
    }
    out.println("</tr>");
  }
}
%>
</table>
</body>
</html>