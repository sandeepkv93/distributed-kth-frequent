package com.distributed.coordinator;

import com.distributed.model.DataPartition;
import com.distributed.model.ProcessingResult;
import com.distributed.node.ProcessingNode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Coordinator {
  private final int numNodes;
  private final long memoryThresholdPerNode;
  private final List<ProcessingNode> nodes;

  public Coordinator(int numNodes, long memoryThresholdPerNode) {
    this.numNodes = numNodes;
    this.memoryThresholdPerNode = memoryThresholdPerNode;
    this.nodes =
        IntStream.range(0, numNodes)
            .mapToObj(i -> new ProcessingNode(i, memoryThresholdPerNode))
            .collect(Collectors.toList());
  }

  public int findKthFrequent(List<Integer> data, int k) {
    if (data == null) {
      throw new IllegalArgumentException("Input data cannot be null");
    }
    if (k <= 0) {
      throw new IllegalArgumentException("K must be positive");
    }
    if (data.isEmpty()) {
      return -1;
    }

    long startTime = System.currentTimeMillis();
    log.info("Starting distributed processing for K={} with {} nodes", k, numNodes);
    log.info("Input data: {}", data);

    try {
      // Step 1: Distribute data using round-robin
      List<List<Integer>> partitions = new ArrayList<>();
      for (int i = 0; i < numNodes; i++) {
        partitions.add(new ArrayList<>());
      }

      // Round-robin distribution
      for (int i = 0; i < data.size(); i++) {
        partitions.get(i % numNodes).add(data.get(i));
      }

      // Create data partitions
      List<DataPartition> dataPartitions = new ArrayList<>();
      for (int i = 0; i < numNodes; i++) {
        DataPartition partition = new DataPartition();
        partition.setNodeId(i);
        partition.setData(partitions.get(i));
        dataPartitions.add(partition);
        log.info("Node {} received data: {}", i, partition.getData());
      }

      // Step 2: Process in parallel
      List<CompletableFuture<ProcessingResult>> futures = new ArrayList<>();
      for (int i = 0; i < numNodes; i++) {
        futures.add(nodes.get(i).processData(dataPartitions.get(i)));
      }

      // Step 3: Wait for all results and merge
      Map<Integer, Integer> globalFrequencies = new HashMap<>();
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .thenAccept(
              v -> {
                futures.stream()
                    .map(CompletableFuture::join)
                    .forEach(
                        result -> {
                          log.info(
                              "Node {} processed frequencies: {}",
                              result.getNodeId(),
                              result.getFrequencies());
                          mergeFrequencies(globalFrequencies, result.getFrequencies());
                        });
              })
          .join();

      log.info("Global frequencies: {}", globalFrequencies);

      // Step 4: Find kth frequent using priority queue
      List<Map.Entry<Integer, Integer>> sortedEntries =
          new ArrayList<>(globalFrequencies.entrySet());
      sortedEntries.sort(
          (a, b) -> {
            int freqCompare = Integer.compare(b.getValue(), a.getValue());
            return freqCompare != 0 ? freqCompare : Integer.compare(a.getKey(), b.getKey());
          });

      log.info("Sorted frequencies: {}", sortedEntries);

      if (k > sortedEntries.size()) {
        return -1;
      }

      int result = sortedEntries.get(k - 1).getKey();

      long totalTime = System.currentTimeMillis() - startTime;
      log.info("Processing completed in {}ms, found {}th most frequent: {}", totalTime, k, result);

      return result;

    } catch (Exception e) {
      log.error("Error processing data", e);
      throw new RuntimeException("Failed to process data", e);
    }
  }

  private void mergeFrequencies(Map<Integer, Integer> global, Map<Integer, Integer> local) {
    local.forEach(
        (key, value) -> {
          global.merge(key, value, Integer::sum);
          log.debug("Merged key {} with value {}, new total: {}", key, value, global.get(key));
        });
  }
}
