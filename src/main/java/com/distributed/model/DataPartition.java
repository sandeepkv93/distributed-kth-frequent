package com.distributed.model;

import java.util.List;
import lombok.Data;

@Data
public class DataPartition {
  private int nodeId;
  private List<Integer> data;
}
