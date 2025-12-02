package com.example.notification.service;

import com.example.notification.domain.NotificationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 동적 코드 컴파일 및 실행 엔진
 * 
 * Claude AI가 생성한 Java 코드를 런타임에 컴파일하고,
 * NotificationRule 인스턴스를 생성하여 반환합니다.
 */
@Service
@Slf4j
public class DynamicCodeEngine {
    
    private static final String TEMP_DIR_PREFIX = "notification-rules";
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("public\\s+class\\s+(\\w+)");
    
    /**
     * Java 소스 코드를 컴파일하고 NotificationRule 인스턴스를 생성합니다
     * 
     * @param sourceCode Java 소스 코드
     * @return 컴파일되고 인스턴스화된 NotificationRule
     * @throws Exception 컴파일 또는 인스턴스화 실패 시
     */
    public NotificationRule compileAndLoad(String sourceCode) throws Exception {
        log.info("동적 코드 컴파일 시작");
        
        // 클래스 이름 추출
        String className = extractClassName(sourceCode);
        if (className == null) {
            throw new IllegalArgumentException("유효한 클래스 이름을 찾을 수 없습니다");
        }
        
        log.debug("추출된 클래스 이름: {}", className);
        
        // 임시 디렉토리 생성
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        Path sourceDir = tempDir.resolve("src");
        Path outputDir = tempDir.resolve("classes");
        
        Files.createDirectories(sourceDir);
        Files.createDirectories(outputDir);
        
        try {
            // 소스 파일 작성
            Path sourceFile = sourceDir.resolve(className + ".java");
            Files.writeString(sourceFile, sourceCode);
            
            log.debug("소스 파일 생성: {}", sourceFile);
            
            // Java 컴파일러 가져오기
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException("Java 컴파일러를 찾을 수 없습니다. JDK가 필요합니다.");
            }
            
            // 컴파일 옵션 설정
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            
            Iterable<? extends JavaFileObject> compilationUnits = 
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile.toFile()));
            
            List<String> options = Arrays.asList(
                    "-d", outputDir.toString(),
                    "-classpath", System.getProperty("java.class.path")
            );
            
            // 컴파일 실행
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    compilationUnits
            );
            
            boolean success = task.call();
            
            if (!success) {
                // 컴파일 오류 로깅
                StringBuilder errors = new StringBuilder("컴파일 오류:\n");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errors.append(String.format("Line %d: %s%n", 
                            diagnostic.getLineNumber(), 
                            diagnostic.getMessage(null)));
                }
                log.error(errors.toString());
                throw new RuntimeException(errors.toString());
            }
            
            log.info("컴파일 성공: {}", className);
            
            // 클래스 로드
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{outputDir.toUri().toURL()},
                    this.getClass().getClassLoader()
            );
            
            // package 이름을 포함한 전체 클래스 이름
            String fullClassName = "com.example.notification.rules." + className;
            Class<?> ruleClass = classLoader.loadClass(fullClassName);
            
            // NotificationRule 인터페이스 구현 확인
            if (!NotificationRule.class.isAssignableFrom(ruleClass)) {
                throw new IllegalArgumentException(
                        className + "는 NotificationRule 인터페이스를 구현하지 않습니다"
                );
            }
            
            // 인스턴스 생성
            NotificationRule rule = (NotificationRule) ruleClass.getDeclaredConstructor().newInstance();
            
            log.info("클래스 로드 및 인스턴스화 성공: {}", className);
            
            fileManager.close();
            
            return rule;
            
        } finally {
            // 임시 파일 정리 (옵션)
            // 프로덕션에서는 정기적으로 정리하거나 별도 관리 필요
            deleteDirectory(tempDir.toFile());
        }
    }
    
    /**
     * 소스 코드에서 클래스 이름을 추출합니다
     */
    private String extractClassName(String sourceCode) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 디렉토리를 재귀적으로 삭제합니다
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
