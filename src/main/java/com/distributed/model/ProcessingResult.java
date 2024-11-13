package com.distributed.model;

import java.util.Map;
import lombok.Data;

@Data
public class ProcessingResult {
  private int nodeId;
  private Map<Integer, Integer> frequencies;
  private long processingTimeMs;
}
