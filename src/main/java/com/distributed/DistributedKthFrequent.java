package com.distributed;

import com.distributed.coordinator.Coordinator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedKthFrequent {
  public static void main(String[] args) {
    // Configuration
    int numNodes = 3;
    long memoryThresholdPerNode = 1024 * 1024; // 1MB
    int k = 3;

    // Generate sample data
    List<Integer> data = generateData(1000);

    // Create and run coordinator
    Coordinator coordinator = new Coordinator(numNodes, memoryThresholdPerNode);
    int result = coordinator.findKthFrequent(data, k);

    log.info("Final Result: {}", result);

    // Run test cases
    runTestCases();
  }

  private static List<Integer> generateData(int size) {
    Random random = new Random(42); // Fixed seed for reproducibility
    return IntStream.range(0, size)
        .map(i -> random.nextInt(100))
        .boxed()
        .collect(Collectors.toList());
  }

  private static void runTestCases() {
    Coordinator coordinator = new Coordinator(3, 1024 * 1024);

    // Test Case 1: Original example
    List<Integer> test1 = Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4);
    log.info("Test 1 (K=3): {}", coordinator.findKthFrequent(test1, 3));

    // Test Case 2: Equal frequencies
    List<Integer> test2 = Arrays.asList(1, 1, 2, 2, 3, 3, 4);
    log.info("Test 2 (K=3): {}", coordinator.findKthFrequent(test2, 3));

    // Test Case 3: Single element dominance
    List<Integer> test3 = Arrays.asList(5, 5, 5, 1, 2, 3, 4);
    log.info("Test 3 (K=2): {}", coordinator.findKthFrequent(test3, 2));
  }
}
