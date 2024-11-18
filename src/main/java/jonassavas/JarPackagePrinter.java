package jonassavas;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class JarPackagePrinter {
    public static void main(String[] args) throws IOException {
        // Specify the directory containing JAR files
        String directoryPath = "C:\\Users\\jonas\\Downloads";
        // Specify the output file path
        String outputPath = "./jar_packages.txt";

        File directory = new File(directoryPath);
        File outputFile = new File(outputPath);

        // Ensure the output file exists
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

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
                                    }
                                }
                            }
                        }

                        // Write the sorted packages to the file with indentation
                        packages.stream().sorted().forEach(pkg -> writer.println("    " + pkg));
                        writer.println(); // Add a blank line between JARs
                    }
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
