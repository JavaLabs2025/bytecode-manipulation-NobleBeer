package org.example.visitor;

import org.example.model.ClassMetrics;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CustomMethodVisitor extends MethodVisitor {

    private final ClassMetrics.MethodMetrics metrics;

    public CustomMethodVisitor(ClassMetrics.MethodMetrics metrics) {
        super(Opcodes.ASM9);
        this.metrics = metrics;
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (isStoreInstruction(opcode)) {
            incrementAssignments();
        }
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        incrementBranches();

        if (isConditionalJump(opcode)) {
            incrementConditions();
        }

        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitEnd() {
        calculateAbcMetric();
        super.visitEnd();
    }

    private boolean isStoreInstruction(int opcode) {
        return opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE;
    }

    private boolean isConditionalJump(int opcode) {
        return (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE)
                || opcode == Opcodes.IFNULL
                || opcode == Opcodes.IFNONNULL
                || opcode == Opcodes.GOTO
                || opcode == Opcodes.JSR;
    }

    private void incrementAssignments() {
        var abc = metrics.getAbcMetric();
        abc.setAssignments(abc.getAssignments() + 1);
    }

    private void incrementBranches() {
        var abc = metrics.getAbcMetric();
        abc.setBranches(abc.getBranches() + 1);
    }

    private void incrementConditions() {
        var abc = metrics.getAbcMetric();
        abc.setConditions(abc.getConditions() + 1);
    }

    private void calculateAbcMetric() {
        var abcMetric = metrics.getAbcMetric();
        var assignments = abcMetric.getAssignments();
        var branches = abcMetric.getBranches();
        var conditions = abcMetric.getConditions();

        metrics.setMetricValue(Math.sqrt(
                assignments * assignments +
                branches * branches +
                conditions * conditions)
        );
    }
}
