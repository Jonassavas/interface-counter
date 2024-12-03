package jonassavas.RQ3;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class ExcludedByBoth {
    public static void main(String[] args) throws IOException {
        // Specify the directory containing dependency JAR files
        String dependenciesPath = "C:\\kthcs\\MEX\\RQ2Gathering\\classgraph\\dependencies";
        // Specify the input TXT file paths
        String inputTxtPath = "./Included_By_Both_Tools/classgraph.txt";
        String inputTxtPath2 = "./JACT_Only_Included/classgraph.txt";
        String inputTxtPath3 = "./DepTrim_Only_Included/classgraph.txt";
        // Specify the output TXT file path
        String outputPath = "./RQ3-Excluded-By-Both/classgraph.txt";

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
        if (dependenciesDirectory.exists() && dependenciesDirectory.isDirectory()) {
            File[] jarFiles = dependenciesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    try (JarFile dependencyJar = new JarFile(jarFile)) {
                        Enumeration<JarEntry> entries = dependencyJar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.endsWith(".class")) {
                                dependencyClassFiles.add(name);
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
        readClassNamesFromTxt(inputTxtPath, txtClassFiles, true);  // Handles the `Included_By_Both_Tools` format
        readClassNamesFromTxt(inputTxtPath2, txtClassFiles, false); // Handles the `JACT_Only_Included` format
        readClassNamesFromTxt(inputTxtPath3, txtClassFiles, false); // Handles the `DepTrim_Only_Included` format

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
        }

        System.out.println("Missing class files information has been written to: " + outputPath);
    }

    /**
     * Reads class names from the specified TXT file and adds them to the provided set.
     *
     * @param filePath     Path to the TXT file.
     * @param classSet     Set to store the class names.
     * @param skipLastLine If true, skip the last line of the file.
     * @throws IOException If an error occurs while reading the file.
     */
    private static void readClassNamesFromTxt(String filePath, Set<String> classSet, boolean skipLastLine) throws IOException {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            System.out.println("The specified TXT file does not exist: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            List<String> allLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    break; // Stop reading at the first blank line
                }
                allLines.add(line);
            }
            // Skip the last line if required (e.g., total counts)
            int linesToRead = skipLastLine ? allLines.size() - 1 : allLines.size();

            for (int i = 0; i < linesToRead; i++) {
                line = allLines.get(i);
                int colonIndex = line.indexOf(":");
                if (colonIndex != -1) {
                    String className = line.substring(colonIndex + 2).trim(); // Remove the part before colon and one space
                    classSet.add(className);
                }
            }
        }
    }
}
