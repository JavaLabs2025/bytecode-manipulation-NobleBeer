package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.Result;
import org.example.model.JarMetrics;
import org.example.visitor.CustomClassVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class Example {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            log.error("Ошибка чтения аргументов");
            return;
        }

        var jarPath = args[0];
        var outputFileName = args[1];

        var result = processJarFile(jarPath);
        writeResultAsJson(result, outputFileName);
    }

    private static Result processJarFile(String jarFilePath) throws IOException {
        var jarMetrics = new JarMetrics();

        try (var jarFile = new JarFile(jarFilePath)) {
            jarFile.stream()
                    .filter(e -> e.getName().endsWith(".class"))
                    .forEach(entry -> processClass(entry, jarFile, jarMetrics));
        }

        jarMetrics.calculateMetrics();

        var stringBuilder = new StringBuilder()
                .append("Максимальная глубина наследования: ").append(jarMetrics.getMaxDepthOfInheritance()).append('\n')
                .append("Средняя глубина наследования: ").append(jarMetrics.getAverageInheritanceDepth()).append('\n')
                .append("Метрика ABC: ").append(jarMetrics.getAverageAbcScore()).append('\n')
                .append("Сколичество переопределенных методов: ").append(jarMetrics.getAverageOverriddenMethods()).append('\n')
                .append("Среднее количество полей в классе: ").append(jarMetrics.getAverageFieldCount());

        log.info(stringBuilder.toString());

        return buildFinalResult(jarMetrics);
    }

    private static void processClass(JarEntry entry, JarFile jarFile, JarMetrics metrics) {
        try {
            var reader = new ClassReader(jarFile.getInputStream(entry));
            reader.accept(new CustomClassVisitor(metrics), 0);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла. Причина: " + entry.getName(), e);
        }
    }

    private static Result buildFinalResult(JarMetrics metrics) {
        return Result.builder()
                .maxDepthOfInheritance(metrics.getMaxDepthOfInheritance())
                .averageInheritanceDepth(metrics.getAverageInheritanceDepth())
                .averageOverriddenMethods(metrics.getAverageOverriddenMethods())
                .averageFieldCount(metrics.getAverageFieldCount())
                .averageAbcScore(metrics.getAverageAbcScore())
                .build();
    }

    private static void writeResultAsJson(Object result, String outputFileName) throws IOException {
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        var file = Paths.get(outputFileName).toFile();

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        mapper.writeValue(file, result);

        log.info("Метрики сохранены в файл: {}", file.getAbsolutePath());
    }
}
