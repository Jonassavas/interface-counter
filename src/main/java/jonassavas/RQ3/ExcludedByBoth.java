package jonassavas.RQ3;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class ExcludedByBoth {
    public static void main(String[] args) throws IOException {
        // Specify the directory containing dependency JAR files
        String dependenciesPath = "C:\\kthcs\\MEX\\RQ2Gathering\\jooby\\jooby\\dependencies";
        // Specify the input TXT file paths
        String projectString = "jooby.txt";
        String includedByBothPath = "./Included_By_Both_Tools/" + projectString;
        String jactOnlyPath = "./JACT_Only_Included/" + projectString;
        String depTrimOnlyPath = "./DepTrim_Only_Included/" + projectString;
        // Specify the output TXT file path
        String outputPath = "./RQ3-Excluded-By-Both/" + projectString;

        File dependenciesDirectory = new File(dependenciesPath);
        File outputFile = new File(outputPath);

        // Ensure the output file exists
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        // List to store all class names from dependencies
        Set<String> dependencyClassFiles = new HashSet<>();
        // List to store all class names from the input TXT files
        Set<String> txtClassFiles = new HashSet<>();
        int excludedClassCount = 0;

        // Step 1: Read class files from the dependencies
        int totalClassFilesInJars = 0; // Counter for total class files in JARs
        if (dependenciesDirectory.exists() && dependenciesDirectory.isDirectory()) {
            File[] jarFiles = dependenciesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    try (JarFile dependencyJar = new JarFile(jarFile)) {
                        Enumeration<JarEntry> entries = dependencyJar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.endsWith(".class") && !isMetadataClass(name)) {
                                dependencyClassFiles.add(name);
                                totalClassFilesInJars++; // Increment the counter
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("The specified dependencies directory does not exist or is not valid.");
            return;
        }

        // Step 2: Parse all TXT files and extract class names
        parseIncludedOrJactOnly(includedByBothPath, txtClassFiles); // Handles `Included_By_Both_Tools` format
        parseIncludedOrJactOnly(jactOnlyPath, txtClassFiles);       // Handles `JACT_Only_Included` format
        parseDepTrimOnly(depTrimOnlyPath, txtClassFiles);           // Handles `DepTrim_Only_Included` format

        // Step 3: Find missing class files
        Set<String> missingClassFiles = new HashSet<>(dependencyClassFiles);
        missingClassFiles.removeAll(txtClassFiles);

        // Step 4: Write missing class files to the output file
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            missingClassFiles.stream().sorted().forEach(writer::println);

            // Write the count of excluded class files
            excludedClassCount = missingClassFiles.size();
            writer.println();
            writer.println("Total number of excluded class files: " + excludedClassCount);

            // Write the total number of class files in the JARs
            writer.println("Total number of class files in all JARs: " + totalClassFilesInJars);
        }

        System.out.println("Missing class files information has been written to: " + outputPath);
    }

    /**
     * Parses the Included_By_Both_Tools or JACT_Only_Included file and adds class names to the set.
     * 
     * @param filePath Path to the file.
     * @param classSet Set to store the parsed class names.
     * @throws IOException If an error occurs while reading the file.
     */
    private static void parseIncludedOrJactOnly(String filePath, Set<String> classSet) throws IOException {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            System.out.println("The specified TXT file does not exist: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    break; // Stop reading at the first blank line
                }

                int colonIndex = line.indexOf(":");
                if (colonIndex != -1) {
                    String className = line.substring(colonIndex + 2).trim(); // Remove the part before colon and one space
                    int classSuffixIndex = className.indexOf(".class");
                    if (classSuffixIndex != -1) {
                        className = className.substring(0, classSuffixIndex + 6); // Include ".class", exclude anything after
                    }
                    classSet.add(className);
                }
            }
        }
    }

    /**
     * Parses the DepTrim_Only_Included file and adds class names to the set.
     * 
     * @param filePath Path to the DepTrim_Only_Included file.
     * @param classSet Set to store the parsed class names.
     * @throws IOException If an error occurs while reading the file.
     */
    private static void parseDepTrimOnly(String filePath, Set<String> classSet) throws IOException {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            System.out.println("The specified TXT file does not exist: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    break; // Stop reading at the first blank line
                }

                int colonIndex = line.indexOf(":");
                if (colonIndex != -1) {
                    String className = line.substring(colonIndex + 2).trim(); // Remove the part before colon and one space
                    classSet.add(className); // DepTrim entries do not have extra data after ".class"
                }
            }
        }
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
}
