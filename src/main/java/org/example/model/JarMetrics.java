package org.example.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JarMetrics {

    private final Map<String, String> classParent = new HashMap<>();
    private final Map<String, List<String>> classMethods = new HashMap<>();
    private final Map<String, Integer> classMaxDepth = new HashMap<>();
    private final Map<String, Integer> classOverriddenMethods = new HashMap<>();
    private final Map<String, ClassMetrics> classMetrics = new HashMap<>();

    public void addParent(String className, String parent) {
        classParent.put(className, parent);
    }

    public void addMethods(String className, List<String> methodSignatures) {
        classMethods.put(className, methodSignatures);
    }

    public void addClassMetrics(ClassMetrics classMetrics) {
        this.classMetrics.put(classMetrics.getName(), classMetrics);
    }

    public void calculateMetrics() {
        classParent.keySet().forEach(this::calculateDepth);
        classMethods.forEach(this::calculateOverriddenMethods);
    }

    public int getMaxDepthOfInheritance() {
        return classMaxDepth.values().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    public double getAverageInheritanceDepth() {
        return classMaxDepth.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    public double getAverageAbcScore() {
        return classMetrics.values().stream()
                .flatMap(c -> c.getMethodMetrics().stream())
                .mapToDouble(mm -> mm.getAbcMetric().getMetricValue())
                .average()
                .orElse(0.0);
    }

    public double getAverageOverriddenMethods() {
        return classOverriddenMethods.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    public double getAverageFieldCount() {
        return classMetrics.values().stream()
                .mapToInt(ClassMetrics::getFieldCount)
                .average()
                .orElse(0.0);
    }

    private void calculateDepth(String className) {
        classMaxDepth.put(className, computeDepth(className));
    }

    private int computeDepth(String className) {
        int depth = 0;
        var current = className;

        while (classParent.containsKey(current)) {
            depth++;
            current = classParent.get(current);
        }

        return depth;
    }

    private void calculateOverriddenMethods(String className, List<String> methods) {
        var inherited = collectInheritedMethods(className);
        var count = methods.stream().filter(inherited::contains).count();
        classOverriddenMethods.put(className, (int) count);
    }

    private Set<String> collectInheritedMethods(String className) {
        var parent = classParent.get(className);
        Set<String> inherited = new HashSet<>();

        while (parent != null) {
            var parentMethods = classMethods.get(parent);
            if (parentMethods != null) {
                inherited.addAll(parentMethods);
            }
            parent = classParent.get(parent);
        }

        return inherited;
    }
}
