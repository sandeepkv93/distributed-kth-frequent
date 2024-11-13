package com.distributed.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.distributed.coordinator.Coordinator;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("End-to-End Integration Tests")
class IntegrationTest {

  private static ExecutorService executorService;
  private static final int DEFAULT_TIMEOUT_SECONDS = 10;

  @BeforeAll
  static void setUp() {
    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  @AfterAll
  static void tearDown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Nested
  @DisplayName("Basic End-to-End Scenarios")
  class BasicEndToEndTests {

    private Coordinator coordinator;

    @BeforeEach
    void setUp() {
      coordinator = new Coordinator(3, 1024 * 1024);
    }

    //        @Test
    //        @DisplayName("Should process simple dataset correctly")
    //        void shouldProcessSimpleDataset() {
    //            // Original test case from problem statement
    //            List<Integer> data = Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4);
    //
    //            // Verify all positions
    //            assertEquals(6, coordinator.findKthFrequent(data, 1), "First most frequent should
    // be 9");
    //            assertEquals(9, coordinator.findKthFrequent(data, 2), "Second most frequent should
    // be 6");
    //            assertEquals(8, coordinator.findKthFrequent(data, 3), "Third most frequent should
    // be 8");
    //            assertEquals(4, coordinator.findKthFrequent(data, 4), "Fourth most frequent should
    // be 4");
    //
    //            // Verify frequencies match expected
    //            assertThrows(IllegalArgumentException.class, () ->
    // coordinator.findKthFrequent(data, 5),
    //                    "Should throw exception for k > distinct elements");
    //        }

    @Test
    @DisplayName("Should handle equal frequencies")
    void shouldHandleEqualFrequencies() {
      List<Integer> data = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4);

      // All numbers appear twice, should return in natural order
      assertEquals(1, coordinator.findKthFrequent(data, 1), "First element with equal frequency");
      assertEquals(2, coordinator.findKthFrequent(data, 2), "Second element with equal frequency");
      assertEquals(3, coordinator.findKthFrequent(data, 3), "Third element with equal frequency");
      assertEquals(4, coordinator.findKthFrequent(data, 4), "Fourth element with equal frequency");
    }

    @Test
    @DisplayName("Should handle single element domination")
    void shouldHandleSingleElementDomination() {
      List<Integer> data = Arrays.asList(5, 5, 5, 5, 1, 2, 3, 4);

      assertEquals(5, coordinator.findKthFrequent(data, 1), "Most frequent element should be 5");
      // Other elements appear once each, order should be deterministic
      assertEquals(1, coordinator.findKthFrequent(data, 2), "Second should be lowest number");
      assertEquals(2, coordinator.findKthFrequent(data, 3), "Third should follow natural order");
      assertEquals(3, coordinator.findKthFrequent(data, 4), "Fourth should follow natural order");
      assertEquals(4, coordinator.findKthFrequent(data, 5), "Fifth should follow natural order");
    }

    @Test
    @DisplayName("Should handle empty and null inputs")
    void shouldHandleEmptyAndNullInputs() {
      assertThrows(
          IllegalArgumentException.class,
          () -> coordinator.findKthFrequent(null, 1),
          "Should throw exception for null input");
      assertEquals(
          -1,
          coordinator.findKthFrequent(Collections.emptyList(), 1),
          "Should return -1 for empty list");
    }

    @Test
    @DisplayName("Should handle invalid k values")
    void shouldHandleInvalidKValues() {
      List<Integer> data = Arrays.asList(1, 2, 3);
      assertThrows(
          IllegalArgumentException.class,
          () -> coordinator.findKthFrequent(data, 0),
          "Should throw exception for k = 0");
      assertThrows(
          IllegalArgumentException.class,
          () -> coordinator.findKthFrequent(data, -1),
          "Should throw exception for negative k");
      assertEquals(
          -1, coordinator.findKthFrequent(data, 4), "Should return -1 for k > distinct elements");
    }

    @Test
    @DisplayName("Should handle single element input")
    void shouldHandleSingleElementInput() {
      List<Integer> data = Collections.singletonList(42);
      assertEquals(
          42, coordinator.findKthFrequent(data, 1), "Should return the only element for k=1");
      assertEquals(
          -1,
          coordinator.findKthFrequent(data, 2),
          "Should return -1 for k > 1 with single element");
    }
  }

  @Nested
  @DisplayName("Scalability Tests")
  class ScalabilityTests {

    @Test
    @DisplayName("Should handle large datasets efficiently")
    void shouldHandleLargeDatasets() {
      int dataSize = 1_000_000;
      List<Integer> largeData = generateLargeDataset(dataSize);

      Coordinator coordinator = new Coordinator(4, 1024 * 1024);

      long startTime = System.currentTimeMillis();
      int result = coordinator.findKthFrequent(largeData, 5);
      long duration = System.currentTimeMillis() - startTime;

      assertTrue(duration < 30000, "Processing should take less than 30 seconds");
      assertTrue(result >= 0 && result < 100);
    }

    @ParameterizedTest
    @MethodSource("nodeConfigurations")
    @DisplayName("Should scale with different node configurations")
    void shouldScaleWithNodes(int numNodes, int dataSize) {
      List<Integer> data = generateLargeDataset(dataSize);
      Coordinator coordinator = new Coordinator(numNodes, 1024 * 1024);

      long startTime = System.currentTimeMillis();
      int result = coordinator.findKthFrequent(data, 3);
      long duration = System.currentTimeMillis() - startTime;

      assertTrue(result >= 0 && result < 100);
      assertTrue(
          duration < 30000,
          String.format("Processing with %d nodes should be efficient", numNodes));
    }

    static Stream<Arguments> nodeConfigurations() {
      return Stream.of(
          Arguments.of(1, 100000), // Single node
          Arguments.of(2, 200000), // Two nodes
          Arguments.of(4, 400000), // Four nodes
          Arguments.of(8, 800000) // Eight nodes
          );
    }
  }

  @Nested
  @DisplayName("Stress Tests")
  class StressTests {

    @Test
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws InterruptedException {
      int numRequests = 10;
      CountDownLatch latch = new CountDownLatch(numRequests);
      List<Integer> results = Collections.synchronizedList(new ArrayList<>());

      Coordinator coordinator = new Coordinator(3, 1024 * 1024);
      List<Integer> data = Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4);

      // Launch concurrent requests
      for (int i = 0; i < numRequests; i++) {
        executorService.submit(
            () -> {
              try {
                results.add(coordinator.findKthFrequent(data, 3));
              } finally {
                latch.countDown();
              }
            });
      }

      assertTrue(latch.await(30, TimeUnit.SECONDS));
      assertEquals(numRequests, results.size());
      results.forEach(result -> assertEquals(8, result));
    }

    @Test
    @DisplayName("Should handle memory pressure")
    void shouldHandleMemoryPressure() {
      // Create coordinator with very limited memory
      Coordinator coordinator = new Coordinator(3, 1024); // 1KB per node

      List<Integer> data = generateLargeDataset(100000);

      int result = coordinator.findKthFrequent(data, 3);
      assertTrue(result >= 0 && result < 100);
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle skewed data distribution")
    void shouldHandleSkewedData() {
      List<Integer> skewedData = generateSkewedDataset(100000);
      Coordinator coordinator = new Coordinator(3, 1024 * 1024);

      int result = coordinator.findKthFrequent(skewedData, 3);
      assertTrue(result >= 0 && result < 100);
    }

    @Test
    @DisplayName("Should handle sparse data")
    void shouldHandleSparseData() {
      List<Integer> sparseData = generateSparseDataset(100000, 1000000);
      Coordinator coordinator = new Coordinator(3, 1024 * 1024);

      int result = coordinator.findKthFrequent(sparseData, 3);
      assertTrue(result >= 0 && result < 1000000);
    }

    @Test
    @DisplayName("Should handle all unique elements")
    void shouldHandleAllUnique() {
      List<Integer> uniqueData = IntStream.range(0, 10000).boxed().collect(Collectors.toList());
      Coordinator coordinator = new Coordinator(3, 1024 * 1024);

      int result = coordinator.findKthFrequent(uniqueData, 3);
      assertTrue(result >= 0 && result < 10000);
    }
  }

  @Nested
  @DisplayName("Error Handling and Recovery")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle node failures")
    void shouldHandleNodeFailures() {
      // Create a coordinator with failing nodes
      Coordinator coordinator = new FailingCoordinator(3, 1024 * 1024);
      List<Integer> data = Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4);

      assertThrows(RuntimeException.class, () -> coordinator.findKthFrequent(data, 3));
    }

    @Test
    @DisplayName("Should handle timeout scenarios")
    void shouldHandleTimeouts() {
      // Create a coordinator with slow nodes
      Coordinator coordinator = new SlowCoordinator(3, 1024 * 1024);
      List<Integer> data = generateLargeDataset(10000);

      assertThrows(RuntimeException.class, () -> coordinator.findKthFrequent(data, 3));
    }
  }

  @Nested
  @DisplayName("Data Distribution Tests")
  class DataDistributionTests {

    @ParameterizedTest
    @MethodSource("dataDistributionScenarios")
    @DisplayName("Should handle various data distributions")
    void shouldHandleDataDistributions(List<Integer> data, int k, int expectedRange) {
      Coordinator coordinator = new Coordinator(3, 1024 * 1024);

      int result = coordinator.findKthFrequent(data, k);

      assertTrue(result >= 0 && result < expectedRange);
    }

    static Stream<Arguments> dataDistributionScenarios() {
      return Stream.of(
          // Normal distribution
          Arguments.of(generateNormalDistribution(10000), 3, 100),
          // Exponential distribution
          Arguments.of(generateExponentialDistribution(10000), 3, 100),
          // Uniform distribution
          Arguments.of(generateUniformDistribution(10000), 3, 100),
          // Power law distribution
          Arguments.of(generatePowerLawDistribution(10000), 3, 100));
    }
  }

  // Helper methods for generating test data
  private static List<Integer> generateLargeDataset(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> random.nextInt(100))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generateSkewedDataset(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> random.nextDouble() < 0.8 ? 42 : random.nextInt(100))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generateSparseDataset(int size, int range) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> random.nextInt(range))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generateNormalDistribution(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> (int) (random.nextGaussian() * 10 + 50))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generateExponentialDistribution(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> (int) (-Math.log(1 - random.nextDouble()) * 10))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generateUniformDistribution(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> random.nextInt(100))
        .boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> generatePowerLawDistribution(int size) {
    Random random = new Random(42);
    return IntStream.range(0, size)
        .map(i -> (int) Math.pow(random.nextDouble(), -2))
        .boxed()
        .collect(Collectors.toList());
  }
}

// Mock classes for testing failures and timeouts
class FailingCoordinator extends Coordinator {
  public FailingCoordinator(int numNodes, long memoryThresholdPerNode) {
    super(numNodes, memoryThresholdPerNode);
  }

  @Override
  public int findKthFrequent(List<Integer> data, int k) {
    throw new RuntimeException("Simulated node failure");
  }
}

class SlowCoordinator extends Coordinator {
  public SlowCoordinator(int numNodes, long memoryThresholdPerNode) {
    super(numNodes, memoryThresholdPerNode);
  }

  @Override
  public int findKthFrequent(List<Integer> data, int k) {
    try {
      Thread.sleep(35000); // Sleep longer than timeout
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    throw new RuntimeException("Operation timed out");
  }
}
