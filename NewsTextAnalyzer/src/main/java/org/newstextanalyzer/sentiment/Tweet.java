package org.newstextanalyzer.sentiment;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Tweet {
  @SerializedName("screen_name")
  String screenName;
  Long id;
  @SerializedName("id_str")
  String idStr;
  @SerializedName("url")
  String URL;
  @SerializedName("full_text")
  String fullText;
  Integer status;
  @SerializedName("created_at")
  String createdAt;
  @SerializedName("reply_count")
  int replyCount;
  List<Reply> replies;
  @SerializedName("avg_sentiment_score")
  double avgSentimentScore;

  public String getScreenName() {
    return screenName;
  }

  public Long getId() {
    return id;
  }

  public String getIdStr() {
    return idStr;
  }

  public String getFullText() {
    return fullText;
  }

  public int getReplyCount() {
    return replyCount;
  }

  public Integer getStatus() {
    return status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getURL() {
    return URL;
  }

  public void setURL(String uRL) {
    URL = uRL;
  }

  public void setScreenName(String screenName) {
    this.screenName = screenName;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setIdStr(String idStr) {
    this.idStr = idStr;
  }

  public void setFullText(String fullText) {
    this.fullText = fullText;
  }

  public void setReplyCount(int replyCount) {
    this.replyCount = replyCount;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public double getAvgSentimentScore() {
    return avgSentimentScore;
  }

  public void setAvgSentimentScore(double avgSentimentScore) {
    this.avgSentimentScore = avgSentimentScore;
  }

  public void setReplies(List<Reply> replies) {
    this.replies = replies;
  }

  public List<Reply> getReplies() {
    return replies;
  }

  @Override
  public String toString() {
    return "Tweet [screenName=" + screenName + ", id=" + id + ", idStr=" + idStr + ", fullText=" + fullText + ", replyCount="
        + replyCount + ", status=" + status + ", createdAt=" + createdAt + ", replies=" + replies
        + ", avgSentimentScore=" + avgSentimentScore + "]";
  }
}

class Reply {
  String reply;
  Double sentimentScore;

  public String getReply() {
    return reply;
  }

  public Double getSentimentScore() {
    return sentimentScore;
  }

  public void setReply(String reply) {
    this.reply = reply;
  }

  public void setSentimentScore(Double sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

  @Override
  public String toString() {
    return "Reply [reply=" + reply + ", sentimentScore=" + sentimentScore + "]";
  }
}
