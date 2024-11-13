package com.distributed.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("FrequencyPair Tests")
class FrequencyPairTest {

  @Test
  @DisplayName("Should create FrequencyPair with correct values")
  void shouldCreateWithCorrectValues() {
    FrequencyPair pair = new FrequencyPair(5, 3);
    assertEquals(5, pair.getNumber());
    assertEquals(3, pair.getFrequency());
  }

  @ParameterizedTest
  @CsvSource({
    // Format: num1,freq1,num2,freq2,expected
    // Higher frequency should come first (return negative)
    "5,4,5,3,-1", // Higher frequency wins
    "5,3,5,4,1", // Lower frequency loses
    "5,3,5,3,0", // Equal frequencies and numbers
    "5,3,6,3,-1", // Equal frequencies, lower number wins
    "6,3,5,3,1" // Equal frequencies, higher number loses
  })
  @DisplayName("Should compare correctly")
  void shouldCompareCorrectly(int num1, int freq1, int num2, int freq2, int expected) {
    FrequencyPair pair1 = new FrequencyPair(num1, freq1);
    FrequencyPair pair2 = new FrequencyPair(num2, freq2);
    assertEquals(
        expected,
        pair1.compareTo(pair2),
        String.format(
            "Comparing (%d,%d) with (%d,%d) should return %d", num1, freq1, num2, freq2, expected));
  }

  @Test
  @DisplayName("Should implement equals correctly")
  void shouldImplementEqualsCorrectly() {
    FrequencyPair pair1 = new FrequencyPair(5, 3);
    FrequencyPair pair2 = new FrequencyPair(5, 3);
    FrequencyPair pair3 = new FrequencyPair(5, 4);
    FrequencyPair pair4 = new FrequencyPair(6, 3);

    assertTrue(pair1.equals(pair2), "Same values should be equal");
    assertFalse(pair1.equals(pair3), "Different frequencies should not be equal");
    assertFalse(pair1.equals(pair4), "Different numbers should not be equal");
    assertFalse(pair1.equals(null), "Comparison with null should return false");
    assertFalse(pair1.equals("not a pair"), "Comparison with different type should return false");
  }

  @Test
  @DisplayName("Should implement hashCode correctly")
  void shouldImplementHashCodeCorrectly() {
    FrequencyPair pair1 = new FrequencyPair(5, 3);
    FrequencyPair pair2 = new FrequencyPair(5, 3);
    FrequencyPair pair3 = new FrequencyPair(5, 4);

    assertEquals(pair1.hashCode(), pair2.hashCode(), "Equal objects should have equal hash codes");
    assertNotEquals(
        pair1.hashCode(), pair3.hashCode(), "Different objects should have different hash codes");
  }

  @Test
  @DisplayName("Should provide correct toString representation")
  void shouldProvideCorrectToString() {
    FrequencyPair pair = new FrequencyPair(5, 3);
    String toString = pair.toString();

    assertTrue(toString.contains("number=5"), "toString should contain number");
    assertTrue(toString.contains("frequency=3"), "toString should contain frequency");
  }

  @Test
  @DisplayName("Should maintain proper ordering in sorted collections")
  void shouldMaintainProperOrdering() {
    // Create pairs with different frequencies and numbers
    FrequencyPair[] pairs = {
      new FrequencyPair(5, 3),
      new FrequencyPair(2, 5),
      new FrequencyPair(3, 5),
      new FrequencyPair(4, 2)
    };

    // Sort the array
    java.util.Arrays.sort(pairs);

    // Verify ordering:
    // 1. Higher frequencies should come first
    // 2. For equal frequencies, lower numbers should come first
    assertEquals(2, pairs[0].getNumber(), "First element should be 2 (freq=5)");
    assertEquals(3, pairs[1].getNumber(), "Second element should be 3 (freq=5)");
    assertEquals(5, pairs[2].getNumber(), "Third element should be 5 (freq=3)");
    assertEquals(4, pairs[3].getNumber(), "Fourth element should be 4 (freq=2)");
  }
}
