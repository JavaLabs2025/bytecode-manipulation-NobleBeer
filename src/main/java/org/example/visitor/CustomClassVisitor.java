package org.example.visitor;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ClassMetrics;
import org.example.model.JarMetrics;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@Slf4j
public class CustomClassVisitor extends ClassVisitor {

    private final ClassMetrics classMetrics = new ClassMetrics();
    private final JarMetrics jarMetrics;

    public CustomClassVisitor(JarMetrics jarMetrics) {
        super(Opcodes.ASM9);
        this.jarMetrics = jarMetrics;
    }

    @Override
    public void visit(int version, int access, String className, String signature, String parentName, String[] interfaces) {
        classMetrics.setName(className);
        classMetrics.setParentClassName(parentName);
        super.visit(version, access, className, signature, parentName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        incrementFieldCount();
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return createMethodMetrics(signature);
    }

    @Override
    public void visitEnd() {
        saveCollectedMetrics();
        super.visitEnd();
    }

    private void incrementFieldCount() {
        classMetrics.setFieldCount(classMetrics.getFieldCount() + 1);
    }

    private org.objectweb.asm.MethodVisitor createMethodMetrics(String signature) {
        var methodMetrics = new ClassMetrics.MethodMetrics();
        methodMetrics.setSignature(signature);
        classMetrics.addMetrics(methodMetrics);
        return new CustomMethodVisitor(methodMetrics);
    }

    private void saveCollectedMetrics() {
        jarMetrics.addParent(classMetrics.getName(), classMetrics.getParentClassName());

        jarMetrics.addMethods(
                classMetrics.getName(),
                classMetrics.getMethodMetrics()
                        .stream()
                        .map(ClassMetrics.MethodMetrics::getSignature)
                        .toList()
        );

        jarMetrics.addClassMetrics(classMetrics);
    }
}
