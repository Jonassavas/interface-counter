package jonassavas.RQ2;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class JarDepPackagesNotInUber {
    public static void main(String[] args) throws IOException {
        // Specify the Uber-JAR file path
        String uberJarPath = "C:\\kthcs\\MEX\\RESULTS\\woodstox\\target\\woodstox-core-6.4.0-shaded.jar";
        // Specify the directory containing dependency JAR files
        String dependenciesPath = "C:\\kthcs\\MEX\\RQ2Gathering\\woodstox\\dependencies";
        // Specify the output file path
        String outputPath = "./RQ2-Data-Not-In-Uber/woodstox.txt";

        File uberJarFile = new File(uberJarPath);
        File dependenciesDirectory = new File(dependenciesPath);
        File outputFile = new File(outputPath);

        // Ensure the output file exists
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        // Set to store all packages in the Uber-JAR
        Set<String> uberJarPackages = new HashSet<>();
        int totalMissingPackages = 0; // Counter for total missing packages across all dependencies
        int totalPackagesInDependencies = 0; // Counter for total packages in all dependencies

        // Read packages from the Uber-JAR
        try (JarFile uberJar = new JarFile(uberJarFile)) {
            Enumeration<JarEntry> entries = uberJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    int lastSlash = name.lastIndexOf('/');
                    if (lastSlash != -1) {
                        String packageName = name.substring(0, lastSlash).replace('/', '.');
                        uberJarPackages.add(packageName);
                    }
                }
            }
        }

        // Analyze dependency JARs
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            if (dependenciesDirectory.exists() && dependenciesDirectory.isDirectory()) {
                File[] jarFiles = dependenciesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

                if (jarFiles != null) {
                    for (File jarFile : jarFiles) {
                        writer.println("Dependency JAR: " + jarFile.getName());
                        writer.println("Missing Packages:");

                        Set<String> dependencyPackages = new HashSet<>();
                        Set<String> missingPackages = new HashSet<>();
                        try (JarFile dependencyJar = new JarFile(jarFile)) {
                            Enumeration<JarEntry> entries = dependencyJar.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.endsWith(".class")) {
                                    int lastSlash = name.lastIndexOf('/');
                                    if (lastSlash != -1) {
                                        String packageName = name.substring(0, lastSlash).replace('/', '.');
                                        dependencyPackages.add(packageName);
                                        if (!uberJarPackages.contains(packageName)) {
                                            missingPackages.add(packageName);
                                        }
                                    }
                                }
                            }
                        }

                        // Write the sorted missing packages to the file with indentation
                        missingPackages.stream().sorted().forEach(pkg -> writer.println("    " + pkg));

                        // Write the number of missing and total packages in this JAR file
                        int totalPackagesInJar = dependencyPackages.size();
                        int missingPackagesInJar = missingPackages.size();
                        writer.println("Number of packages in this JAR: " + totalPackagesInJar);
                        writer.println("Number of missing packages in this JAR: " + missingPackagesInJar);
                        // writer.println("Percentage of missing packages: " +
                        //         (totalPackagesInJar > 0
                        //                 ? String.format("%.2f%%", (missingPackagesInJar * 100.0) / totalPackagesInJar)
                        //                 : "N/A"));
                        writer.println(); // Add a blank line between JARs

                        // Update totals
                        totalPackagesInDependencies += totalPackagesInJar;
                        totalMissingPackages += missingPackagesInJar;
                    }

                    // Write the total counts at the end
                    writer.println("Summary:");
                    writer.println("Total number of packages in all dependency JARs: " + totalPackagesInDependencies);
                    writer.println("Total number of missing packages across all dependency JARs: " + totalMissingPackages);
                    writer.println("Overall percentage of missing packages: " +
                            (totalPackagesInDependencies > 0
                                    ? String.format("%.2f%%", (totalMissingPackages * 100.0) / totalPackagesInDependencies)
                                    : "N/A"));
                } else {
                    writer.println("No JAR files found in the dependencies directory.");
                }
            } else {
                writer.println("The specified dependencies directory does not exist or is not valid.");
            }
        }

        System.out.println("Missing package information has been written to: " + outputPath);
    }
}