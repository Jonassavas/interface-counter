package jonassavas.RQ2;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class JarPackagePrinter {
    public static void main(String[] args) throws IOException {
        // Specify the directory containing JAR files
        String directoryPath = "C:\\kthcs\\MEX\\RQ2Gathering\\woodstox\\dependencies";
        // Specify the output file path
        String outputPath = "./RQ2-Identical_Package_Names/woodstox.txt";

        File directory = new File(directoryPath);
        File outputFile = new File(outputPath);

        // Ensure the output file exists
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        // Map to store packages and the JAR files in which they appear
        Map<String, Set<String>> packageToJarsMap = new HashMap<>();
        int totalPackages = 0; // Counter for total number of packages across all JARs
        int totalDuplicatePackages = 0; // Counter for total number of duplicate packages

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Check if the directory exists and contains files
            if (directory.exists() && directory.isDirectory()) {
                File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));

                if (jarFiles != null) {
                    for (File jarFile : jarFiles) {
                        writer.println("JAR File: " + jarFile.getName());
                        writer.println("Packages:");

                        Set<String> packages = new HashSet<>();
                        try (JarFile jar = new JarFile(jarFile)) {
                            Enumeration<JarEntry> entries = jar.entries();

                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.endsWith(".class")) {
                                    int lastSlash = name.lastIndexOf('/');
                                    if (lastSlash != -1) {
                                        String packageName = name.substring(0, lastSlash).replace('/', '.');
                                        packages.add(packageName);

                                        // Track the JAR file for the package
                                        packageToJarsMap.computeIfAbsent(packageName, k -> new HashSet<>()).add(jarFile.getName());
                                    }
                                }
                            }
                        }

                        // Write the sorted packages to the file with indentation
                        packages.stream().sorted().forEach(pkg -> writer.println("    " + pkg));

                        // Write the number of packages in this JAR file
                        writer.println("Number of packages in this JAR: " + packages.size());
                        writer.println(); // Add a blank line between JARs

                        // Update the total package count
                        totalPackages += packages.size();
                    }

                    // Analyze duplicates at the end
                    writer.println("Duplicate Packages Found:");
                    boolean hasDuplicates = false;
                    for (Map.Entry<String, Set<String>> entry : packageToJarsMap.entrySet()) {
                        Set<String> jarList = entry.getValue();
                        if (jarList.size() > 1) { // Only consider duplicates if a package appears in more than one JAR
                            hasDuplicates = true;
                            writer.println("Package: " + entry.getKey());
                            writer.println("    Appears in JARs:");
                            jarList.forEach(jar -> writer.println("        " + jar));
                            totalDuplicatePackages++;
                        }
                    }

                    if (!hasDuplicates) {
                        writer.println("No duplicate packages found across the JARs.");
                    }

                    // Write the total counts
                    writer.println();
                    writer.println("Total number of duplicate packages: " + totalDuplicatePackages);
                    writer.println("Total number of packages: " + totalPackages);
                } else {
                    writer.println("No JAR files found in the directory.");
                }
            } else {
                writer.println("The specified directory does not exist or is not valid.");
            }
        }

        System.out.println("Package information has been written to: " + outputPath);
    }
}
