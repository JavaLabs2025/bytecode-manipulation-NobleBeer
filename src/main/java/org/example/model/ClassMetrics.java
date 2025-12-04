package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ClassMetrics {

    private String name;
    private String parentClassName;
    private int fieldCount;
    private final List<MethodMetrics> methodMetrics = new ArrayList<>();

    public void addMetrics(MethodMetrics methodMetrics) {
        this.methodMetrics.add(methodMetrics);
    }

    @Getter
    @Setter
    public static class MethodMetrics {
        private String signature;
        private final ABCMetric abcMetric = new ABCMetric();

        public void setMetricValue(double metricValue) {
            this.getAbcMetric().setMetricValue(metricValue);
        }

        @Getter
        @Setter
        public static class ABCMetric {
            private long assignments;
            private long branches;
            private long conditions;
            private double metricValue;
        }
    }
}
