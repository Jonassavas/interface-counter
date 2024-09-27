package jonassavas;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JACTMissedClassesClassifier {

    // Global counters for all JAR files
    private static int totalInterfaceMissedCount = 0;
    private static int totalAbstractClassMissedCount = 0;
    private static int totalEnumMissedCount = 0;
    private static int totalRegularClassMissedCount = 0;
    private static int totalSyntheticClassMissedCount = 0;
    private static int totalAnnotationMissedCount = 0;
    private static int totalFoundCount = 0;
    private static int totalNotFoundCount = 0;

    public static void main(String[] args) throws IOException {
        String rootDirectoryPath = "/home/jonassavas/complete-jact-reports/helidon-openapi_jact-report/dependencies/";
        String jarDirPath = "/home/jonassavas/deptrim-experiments/pipeline/results/helidon/openapi/original/compile-scope-dependencies/dependency/";
        String logDirPath = "./Original_vs_JACT_RQ3/helidon/"; // Output directory for log files

        // Create the output directory if it doesn't exist
        File logDir = new File(logDirPath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Set to store the class names gathered from HTML files
        Set<String> htmlGeneratedClasses = new HashSet<>();

        // Process the root directory to extract class names from HTML files
        processDirectory(new File(rootDirectoryPath), "", htmlGeneratedClasses);

        // Get all JAR files in the JAR directory and process each one
        File jarDir = new File(jarDirPath);
        File[] jarFiles = jarDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No JAR files found in the directory: " + jarDirPath);
            return;
        }

        // Loop through each JAR file and perform the comparison
        for (File jarFile : jarFiles) {
            String jarFileName = jarFile.getName().replace(".jar", ""); // Remove .jar extension
            String logFilePath = logDirPath + jarFileName + ".txt"; // Create log file path

            // Compare the JAR with the HTML-generated classes and log the result
            checkJarFile(jarFile.getAbsolutePath(), htmlGeneratedClasses, logFilePath);
        }

        // Write the final summary to SUMMARY.txt
        writeFinalSummary(logDirPath + "SUMMARY.txt");
    }

    private static void processDirectory(File dir, String relativePath, Set<String> htmlGeneratedClasses) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Skip the 'jacoco-resources' directory
                if (file.getName().equals("jacoco-resources")) {
                    continue;
                }
                // Recur into the directory, appending the directory name to the relative path
                processDirectory(file, relativePath + file.getName() + ".", htmlGeneratedClasses);
            } else if (file.getName().endsWith(".html")) {
                String fileName = file.getName().replace(".html", "");
                // Skip 'index' and 'index.source'
                if (!fileName.equals("index") && !fileName.equals("index.source")) {
                    // Generate the class name format (replace '.' with '/')
                    String className = file.getParentFile().getName().replace(".", "/") + "/" + fileName + ".class";
                    htmlGeneratedClasses.add(className);
                }
            }
        }
    }

    private static void checkJarFile(String jarFilePath, Set<String> htmlGeneratedClasses, String logFilePath) throws IOException {
        // Open a file writer to log the output
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFilePath))) {
            // Open the JAR file
            try (JarFile jarFile = new JarFile(jarFilePath)) {
                Set<String> jarClasses = new HashSet<>();

                // Counters for each JAR file
                int interfaceMissedCount = 0;
                int abstractClassMissedCount = 0;
                int enumMissedCount = 0;
                int regularClassMissedCount = 0;
                int syntheticClassMissedCount = 0;
                int annotationMissedCount = 0;

                int foundCount = 0;
                int notFoundCount = 0;

                // Iterate through the entries in the JAR file using Enumeration
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();

                    // Skip directories, module-info, META-INF, and meta-information files
                    if (jarEntry.isDirectory() || jarEntry.getName().equals("module-info.class") || jarEntry.getName().startsWith("META-INF/")) {
                        continue;
                    }

                    // Only process .class files
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    jarClasses.add(jarEntry.getName());

                    // Check if the class exists in the HTML-generated class set
                    if (!htmlGeneratedClasses.contains(jarEntry.getName())) {
                        notFoundCount++;

                        // Use ASM to analyze the class file
                        try (InputStream classStream = jarFile.getInputStream(jarEntry)) {
                            ClassReader classReader = new ClassReader(classStream);
                            int access = classReader.getAccess();

                            if ((access & Opcodes.ACC_INTERFACE) != 0) {
                                if ((access & Opcodes.ACC_ANNOTATION) != 0) {
                                    annotationMissedCount++;
                                    logWriter.println("ANNOTATION missed: " + jarEntry.getName());
                                } else {
                                    interfaceMissedCount++;
                                    logWriter.println("INTERFACE missed: " + jarEntry.getName());
                                }
                            } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
                                abstractClassMissedCount++;
                                logWriter.println("ABSTRACT CLASS missed: " + jarEntry.getName());
                            } else if ((access & Opcodes.ACC_ENUM) != 0) {
                                enumMissedCount++;
                                logWriter.println("ENUM missed: " + jarEntry.getName());
                            } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                                syntheticClassMissedCount++;
                                logWriter.println("SYNTHETIC CLASS missed: " + jarEntry.getName());
                            } else {
                                regularClassMissedCount++;
                                logWriter.println("REGULAR CLASS missed: " + jarEntry.getName());
                            }
                        } catch (IOException e) {
                            logWriter.println("Could not process class: " + jarEntry.getName());
                        }
                    } else {
                        foundCount++;
                    }
                }

                // Update global totals
                totalInterfaceMissedCount += interfaceMissedCount;
                totalAbstractClassMissedCount += abstractClassMissedCount;
                totalEnumMissedCount += enumMissedCount;
                totalRegularClassMissedCount += regularClassMissedCount;
                totalSyntheticClassMissedCount += syntheticClassMissedCount;
                totalAnnotationMissedCount += annotationMissedCount;
                totalFoundCount += foundCount;
                totalNotFoundCount += notFoundCount;

                // Print and log the summary for the individual JAR
                logWriter.printf("\nSummary for %s:\n" +
                                "Classes found in both JAR and HTML: %d\n" +
                                "Classes in JAR but not found in HTML: %d\n" +
                                "Interfaces missed: %d\n" +
                                "Abstract classes missed: %d\n" +
                                "Enums missed: %d\n" +
                                "Annotations missed: %d\n" +
                                "Synthetic classes missed: %d\n" +
                                "Regular classes missed: %d\n",
                        jarFilePath, foundCount, notFoundCount, interfaceMissedCount, abstractClassMissedCount,
                        enumMissedCount, annotationMissedCount, syntheticClassMissedCount, regularClassMissedCount);
            }
        }
    }

    private static void writeFinalSummary(String summaryFilePath) throws IOException {
        try (PrintWriter summaryWriter = new PrintWriter(new FileWriter(summaryFilePath))) {
            summaryWriter.printf("Final Summary for all JAR files:\n" +
                    "Total classes found in both JAR and HTML: %d\n" +
                    "Total classes in JAR but not found in HTML: %d\n" +
                    "Total interfaces missed: %d\n" +
                    "Total abstract classes missed: %d\n" +
                    "Total enums missed: %d\n" +
                    "Total annotations missed: %d\n" +
                    "Total synthetic classes missed: %d\n" +
                    "Total regular classes missed: %d\n",
                    totalFoundCount, totalNotFoundCount, totalInterfaceMissedCount, totalAbstractClassMissedCount,
                    totalEnumMissedCount, totalAnnotationMissedCount, totalSyntheticClassMissedCount, totalRegularClassMissedCount);
        }
    }
}