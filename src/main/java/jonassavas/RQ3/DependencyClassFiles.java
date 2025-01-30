package jonassavas.RQ3;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class DependencyClassFiles {
    // Counters for class types
    private static int interfaceCount = 0, abstractCount = 0, enumCount = 0;
    private static int annotationCount = 0, syntheticCount = 0, regularCount = 0;

    // Total counters for summary
    private static int totalInterfaceCount = 0, totalAbstractCount = 0, totalEnumCount = 0;
    private static int totalAnnotationCount = 0, totalSyntheticCount = 0, totalRegularCount = 0;

    public static void main(String[] args) throws IOException {
        // Specify the directory containing JAR files
        String directoryPath = "C:\\kthcs\\MEX\\RQ2Gathering\\tika\\tika-core\\dependencies";
        // Specify the output file path
        String outputPath = "./RQ3/RQ3-Dep_Class_Files/tika.txt";

        File directory = new File(directoryPath);
        File outputFile = new File(outputPath);

        // Ensure the output file exists
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            if (directory.exists() && directory.isDirectory()) {
                File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));

                if (jarFiles != null) {
                    for (File jarFile : jarFiles) {
                        writer.println("JAR File: " + jarFile.getName());
                        writer.println("Class Types:");

                        try (JarFile jar = new JarFile(jarFile)) {
                            Enumeration<JarEntry> entries = jar.entries();

                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                if (entry.getName().endsWith(".class") && !isMetadataClass(entry.getName())) {
                                    countClassType(jar.getInputStream(entry));
                                }
                            }
                        }

                        // Write the class type counts for this JAR
                        writeCounts(writer);
                        accumulateTotals();
                        resetCounts();
                        writer.println(); // Add a blank line between JARs
                    }

                    // Write the summary of all class types
                    writeSummary(writer);
                } else {
                    writer.println("No JAR files found in the directory.");
                }
            } else {
                writer.println("The specified directory does not exist or is not valid.");
            }
        }

        System.out.println("Class type information has been written to: " + outputPath);
    }

    /**
     * Checks if a class file is a metadata class (e.g., `module-info` or `package-info`).
     * 
     * @param className The name of the class file.
     * @return True if the class is a metadata class, false otherwise.
     */
    private static boolean isMetadataClass(String className) {
        return className.equals("module-info.class") || className.endsWith("/module-info.class") ||
               className.equals("package-info.class") || className.endsWith("/package-info.class");
    }

    private static void countClassType(InputStream classStream) throws IOException {
        ClassReader classReader = new ClassReader(classStream);
        int access = classReader.getAccess();

        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            if ((access & Opcodes.ACC_ANNOTATION) != 0) {
                annotationCount++;
            } else {
                interfaceCount++;
            }
        } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            abstractCount++;
        } else if ((access & Opcodes.ACC_ENUM) != 0) {
            enumCount++;
        } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            syntheticCount++;
        } else {
            regularCount++;
        }
    }

    private static void writeCounts(PrintWriter writer) {
        writer.println("    Interfaces: " + interfaceCount);
        writer.println("    Abstract Classes: " + abstractCount);
        writer.println("    Enums: " + enumCount);
        writer.println("    Annotations: " + annotationCount);
        writer.println("    Synthetic Classes: " + syntheticCount);
        writer.println("    Regular Classes: " + regularCount);
    }

    private static void accumulateTotals() {
        totalInterfaceCount += interfaceCount;
        totalAbstractCount += abstractCount;
        totalEnumCount += enumCount;
        totalAnnotationCount += annotationCount;
        totalSyntheticCount += syntheticCount;
        totalRegularCount += regularCount;
    }

    private static void resetCounts() {
        interfaceCount = 0;
        abstractCount = 0;
        enumCount = 0;
        annotationCount = 0;
        syntheticCount = 0;
        regularCount = 0;
    }

    private static void writeSummary(PrintWriter writer) {
        int totalClasses = totalInterfaceCount + totalAbstractCount + totalEnumCount +
                           totalAnnotationCount + totalSyntheticCount + totalRegularCount;

        writer.println("Summary of All JAR Files:");
        writer.println("    Total Interfaces: " + totalInterfaceCount);
        writer.println("    Total Abstract Classes: " + totalAbstractCount);
        writer.println("    Total Enums: " + totalEnumCount);
        writer.println("    Total Annotations: " + totalAnnotationCount);
        writer.println("    Total Synthetic Classes: " + totalSyntheticCount);
        writer.println("    Total Regular Classes: " + totalRegularCount);
        writer.println("    Total Classes: " + totalClasses);
    }
}
