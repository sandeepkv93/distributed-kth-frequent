package com.distributed.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FrequencyPair implements Comparable<FrequencyPair> {
  private int number;
  private int frequency;

  @Override
  public int compareTo(FrequencyPair other) {
    // First compare by frequency in descending order
    int freqCompare = Integer.compare(other.frequency, this.frequency);
    if (freqCompare != 0) {
      return freqCompare;
    }
    // If frequencies are equal, compare by number in ascending order
    return Integer.compare(this.number, other.number);
  }
}
