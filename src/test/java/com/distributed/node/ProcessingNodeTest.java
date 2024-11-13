package com.distributed.node;

import static org.junit.jupiter.api.Assertions.*;

import com.distributed.model.DataPartition;
import com.distributed.model.ProcessingResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Processing Node Tests")
class ProcessingNodeTest {
  private ProcessingNode node;
  private static final long DEFAULT_MEMORY_THRESHOLD = 1024 * 1024; // 1MB

  @BeforeEach
  void setUp() {
    node = new ProcessingNode(1, DEFAULT_MEMORY_THRESHOLD);
  }

  @Nested
  @DisplayName("Basic Processing Tests")
  class BasicProcessingTests {
    @Test
    @DisplayName("Should process empty data partition")
    void shouldProcessEmptyPartition() throws ExecutionException, InterruptedException {
      DataPartition emptyPartition = new DataPartition();
      emptyPartition.setNodeId(1);
      emptyPartition.setData(Arrays.asList());

      CompletableFuture<ProcessingResult> futureResult = node.processData(emptyPartition);
      ProcessingResult result = futureResult.get();

      assertNotNull(result);
      assertEquals(1, result.getNodeId());
      assertTrue(result.getFrequencies().isEmpty());
      assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    @DisplayName("Should process single element partition")
    void shouldProcessSingleElementPartition() throws ExecutionException, InterruptedException {
      DataPartition partition = new DataPartition();
      partition.setNodeId(1);
      partition.setData(Arrays.asList(5));

      ProcessingResult result = node.processData(partition).get();

      assertEquals(1, result.getFrequencies().size());
      assertEquals(1, result.getFrequencies().get(5));
    }
  }

  @Nested
  @DisplayName("Frequency Counting Tests")
  class FrequencyCountingTests {
    @ParameterizedTest
    @MethodSource("frequencyTestCases")
    @DisplayName("Should correctly count frequencies")
    void shouldCorrectlyCountFrequencies(List<Integer> input, Map<Integer, Integer> expected)
        throws ExecutionException, InterruptedException {

      DataPartition partition = new DataPartition();
      partition.setNodeId(1);
      partition.setData(input);

      ProcessingResult result = node.processData(partition).get();

      assertEquals(expected, result.getFrequencies());
    }

    static Stream<Arguments> frequencyTestCases() {
      return Stream.of(
          Arguments.of(
              Arrays.asList(1, 1, 2, 2, 3),
              createFrequencyMap(new int[] {1, 2, 3}, new int[] {2, 2, 1})),
          Arguments.of(Arrays.asList(5, 5, 5, 5), createFrequencyMap(new int[] {5}, new int[] {4})),
          Arguments.of(
              Arrays.asList(1, 2, 3, 4, 5),
              createFrequencyMap(new int[] {1, 2, 3, 4, 5}, new int[] {1, 1, 1, 1, 1})));
    }

    private static Map<Integer, Integer> createFrequencyMap(int[] numbers, int[] frequencies) {
      Map<Integer, Integer> map = new HashMap<>();
      for (int i = 0; i < numbers.length; i++) {
        map.put(numbers[i], frequencies[i]);
      }
      return map;
    }
  }

  @Nested
  @DisplayName("Memory Management Tests")
  class MemoryManagementTests {
    @Test
    @DisplayName("Should handle large data sets with memory constraints")
    void shouldHandleLargeDataSets() throws ExecutionException, InterruptedException {
      // Create a node with very low memory threshold
      ProcessingNode lowMemoryNode = new ProcessingNode(1, 100);

      // Generate large dataset
      List<Integer> largeData = generateLargeDataset(10000);

      DataPartition partition = new DataPartition();
      partition.setNodeId(1);
      partition.setData(largeData);

      ProcessingResult result = lowMemoryNode.processData(partition).get();

      assertNotNull(result);
      assertFalse(result.getFrequencies().isEmpty());
      // Should have pruned low frequencies
      result.getFrequencies().values().forEach(freq -> assertTrue(freq > 1));
    }

    private List<Integer> generateLargeDataset(int size) {
      return Stream.generate(() -> (int) (Math.random() * 100))
          .limit(size)
          .collect(java.util.stream.Collectors.toList());
    }
  }

  @Nested
  @DisplayName("Performance Tests")
  class PerformanceTests {
    @Test
    @DisplayName("Should process data within acceptable time")
    void shouldProcessDataWithinTime() throws ExecutionException, InterruptedException {
      List<Integer> data =
          Stream.generate(() -> (int) (Math.random() * 100))
              .limit(1000)
              .collect(java.util.stream.Collectors.toList());

      DataPartition partition = new DataPartition();
      partition.setNodeId(1);
      partition.setData(data);

      long startTime = System.currentTimeMillis();
      ProcessingResult result = node.processData(partition).get();
      long duration = System.currentTimeMillis() - startTime;

      assertTrue(duration < 1000, "Processing should take less than 1 second");
      assertEquals(result.getProcessingTimeMs(), duration, 100);
    }
  }
}
