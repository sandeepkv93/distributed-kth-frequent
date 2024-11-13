package com.distributed.node;

import com.distributed.model.DataPartition;
import com.distributed.model.ProcessingResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessingNode {
  private final int nodeId;
  private final long memoryThreshold;

  public ProcessingNode(int nodeId, long memoryThreshold) {
    this.nodeId = nodeId;
    this.memoryThreshold = memoryThreshold;
  }

  public CompletableFuture<ProcessingResult> processData(DataPartition partition) {
    return CompletableFuture.supplyAsync(
        () -> {
          log.info(
              "Node {} starting processing of {} elements: {}",
              nodeId,
              partition.getData().size(),
              partition.getData());

          ProcessingResult result = new ProcessingResult();
          result.setNodeId(nodeId);
          result.setFrequencies(countFrequencies(partition.getData()));

          log.info(
              "Node {} completed processing. Frequencies: {}", nodeId, result.getFrequencies());
          return result;
        });
  }

  private Map<Integer, Integer> countFrequencies(List<Integer> data) {
    Map<Integer, Integer> frequencies = new HashMap<>();
    for (Integer num : data) {
      frequencies.merge(num, 1, Integer::sum);
    }
    return frequencies;
  }
}
