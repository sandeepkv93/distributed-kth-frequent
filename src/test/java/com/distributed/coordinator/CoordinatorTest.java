package com.distributed.coordinator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Coordinator Tests")
class CoordinatorTest {
  private Coordinator coordinator;
  private static final int DEFAULT_NODES = 3;
  private static final long DEFAULT_MEMORY = 1024 * 1024;

  @BeforeEach
  void setUp() {
    coordinator = new Coordinator(DEFAULT_NODES, DEFAULT_MEMORY);
  }

  @Nested
  @DisplayName("Basic Functionality Tests")
  class BasicFunctionalityTests {

    @Test
    @DisplayName("Should handle null input")
    void shouldHandleNullInput() {
      Exception exception =
          assertThrows(IllegalArgumentException.class, () -> coordinator.findKthFrequent(null, 1));
      assertEquals("Input data cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty input list")
    void shouldHandleEmptyInput() {
      assertEquals(-1, coordinator.findKthFrequent(List.of(), 1));
    }

    @Test
    @DisplayName("Should handle single element input")
    void shouldHandleSingleElement() {
      assertEquals(5, coordinator.findKthFrequent(List.of(5), 1));
    }

    @Test
    @DisplayName("Should handle invalid k values")
    void shouldHandleInvalidK() {
      assertThrows(
          IllegalArgumentException.class,
          () -> coordinator.findKthFrequent(Arrays.asList(1, 2, 3), 0));
      assertThrows(
          IllegalArgumentException.class,
          () -> coordinator.findKthFrequent(Arrays.asList(1, 2, 3), -1));
    }

    @Test
    @DisplayName("Should handle k larger than distinct elements")
    void shouldHandleKLargerThanDistinct() {
      assertEquals(-1, coordinator.findKthFrequent(Arrays.asList(1, 2, 3), 4));
    }
  }

  @Nested
  @DisplayName("Frequency Analysis Tests")
  class FrequencyAnalysisTests {

    @ParameterizedTest
    @MethodSource("kthFrequentTestCases")
    @DisplayName("Should find correct kth frequent element")
    void shouldFindCorrectKthFrequent(List<Integer> input, int k, int expected) {
      assertEquals(expected, coordinator.findKthFrequent(input, k));
    }

    static Stream<Arguments> kthFrequentTestCases() {
      return Stream.of(
          // Original test case
          Arguments.of(Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4), 3, 8),
          // Equal frequencies
          Arguments.of(Arrays.asList(1, 1, 2, 2, 3, 3), 2, 2),
          // Single dominant element
          Arguments.of(Arrays.asList(5, 5, 5, 1, 2, 3), 2, 1),
          // All unique elements
          Arguments.of(Arrays.asList(1, 2, 3, 4, 5), 3, 3));
    }

    @Test
    @DisplayName("Should handle ties correctly")
    void shouldHandleTiesCorrectly() {
      List<Integer> data = Arrays.asList(1, 1, 2, 2, 3, 3);
      assertEquals(1, coordinator.findKthFrequent(data, 1));
      assertEquals(2, coordinator.findKthFrequent(data, 2));
      assertEquals(3, coordinator.findKthFrequent(data, 3));
    }
  }

  @Nested
  @DisplayName("Data Distribution Tests")
  class DataDistributionTests {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 8})
    @DisplayName("Should distribute data correctly across nodes")
    void shouldDistributeDataCorrectly(int numNodes) {
      List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
      Coordinator multiNodeCoordinator = new Coordinator(numNodes, DEFAULT_MEMORY);

      int result = multiNodeCoordinator.findKthFrequent(data, 2);
      assertTrue(result > 0, "Should find valid result regardless of node count");
    }
  }
}
