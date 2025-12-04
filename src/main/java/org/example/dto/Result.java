package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Result {
    private int maxDepthOfInheritance;
    private double averageInheritanceDepth;
    private double averageOverriddenMethods;
    private double averageFieldCount;
    private double averageAbcScore;
}
