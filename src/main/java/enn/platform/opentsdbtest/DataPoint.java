package enn.platform.opentsdbtest;

import java.util.Map;

class DataPoint {
  public String metric;
  public Long timestamp;
  public Double value;
  public Map<String, String> tags;

  public DataPoint(String metric, Long timestamp, Double value, Map<String, String> tags) {
    this.metric = metric;
    this.timestamp = timestamp;
    this.value = value;
    this.tags = tags;
  }
}
