package jonassavas.RQ2;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class JarDepPackagesNotInUberBatch {
    public static void main(String[] args) throws IOException {
        // Arrays for multiple projects
        String[] projectNames = {
            "checkstyle",
            "Chronicle-Map",
            "classgraph",
            "commons-validator",
            "CoreNLP",
            "flink",
            "graphhopper",
            "guice",
            "helidon",
            "httpcomponents",
            "immutables",
            "jacop",
            "java-faker",
            "jcabi-github",
            "jimfs",
            "jooby",
            "lettuce",
            "modelmapper",
            "mybatis-3",
            "OpenPDF",
            "pdfbox",
            "pf4j",
            "poi-tl",
            "Recaf",
            "RxRelay",
            "scribejava",
            "tablesaw",
            "tika",
            "undertow",
            "woodstox"
        };
        String[] uberJarPaths = {
            "C:\\Users\\jonas\\Downloads\\checkstyle-10.5.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\Chronicle-Map\\target\\chronicle-map-3.25ea6-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\classgraph\\target\\classgraph-4.8.154-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\commons-validator\\target\\commons-validator-1.7-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\CoreNLP\\target\\stanford-corenlp-4.5.7-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\flink\\flink-java\\target\\flink-java-1.19.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\graphhopper\\core\\target\\graphhopper-core-6.0-SNAPSHOT-shaded.jar",
            "C:\\Users\\jonas\\Downloads\\guice-5.1.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\helidon\\openapi\\target\\helidon-openapi-2.5.6-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\httpcomponents-client\\httpclient5\\target\\httpclient5-5.2.1-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\immutables\\gson\\target\\gson-2.9.3-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\jacop\\target\\jacop-4.9.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\java-faker\\target\\javafaker-1.0.2-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\jcabi-github\\target\\jcabi-github-1.3.2-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\jimfs\\jimfs\\target\\jimfs-HEAD-SNAPSHOT-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\jooby\\jooby\\target\\jooby-2.16.1-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\lettuce\\target\\lettuce-core-6.2.2.RELEASE-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\modelmapper\\core\\target\\modelmapper-3.1.1-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\mybatis-3\\target\\mybatis-3.5.16-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\OpenPDF\\openpdf\\target\\openpdf-1.3.30-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\pdfbox\\pdfbox\\target\\pdfbox-2.0.31-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\pf4j\\pf4j\\target\\pf4j-3.8.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\poi-tl\\poi-tl\\target\\poi-tl-1.12.2-SNAPSHOT-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\Recaf\\target\\recaf-2.21.13-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\RxRelay-e9fc1586192ca1ecdbc41ae39036cbf0d09428b5\\target\\rxrelay-3.0.2-SNAPSHOT-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\scribejava\\scribejava-core\\target\\scribejava-core-8.3.3-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\tablesaw\\json\\target\\tablesaw-json-0.43.1-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\tika\\tika-core\\target\\tika-core-2.6.0-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\undertow\\core\\target\\undertow-core-2.3.13.Final-shaded.jar",
            "C:\\kthcs\\MEX\\RESULTS\\woodstox\\target\\woodstox-core-6.4.0-shaded.jar"
        };
        String[] dependenciesPaths = {
            "C:\\kthcs\\MEX\\RQ2Gathering\\checkstyle\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\Chronicle-Map\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\classgraph\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\commons-validator\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\CoreNLP\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\flink\\flink-java\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\graphhopper\\core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\guice\\core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\helidon\\openapi\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\httpcomponents-client\\httpclient5\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\immutables\\gson\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\jacop\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\java-faker\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\jcabi-github\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\jimfs\\jimfs\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\jooby\\jooby\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\lettuce\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\modelmapper\\core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\mybatis-3\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\OpenPDF\\openpdf\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\pdfbox\\pdfbox\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\pf4j\\pf4j\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\poi-tl\\poi-tl\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\Recaf\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\RxRelay\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\scribejava\\scribejava-core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\tablesaw\\json\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\tika\\tika-core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\undertow\\core\\dependencies",
            "C:\\kthcs\\MEX\\RQ2Gathering\\woodstox\\dependencies"
        };
        
        // Output file path
        String outputPath = "./RQ2-Data-Not-In-Uber/batch-summary.txt";
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs(); // Ensure parent directory exists
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        int grandTotalPackagesInDependencies = 0;
        int grandTotalMissingPackages = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (int i = 0; i < projectNames.length; i++) {
                String projectName = projectNames[i];
                String uberJarPath = uberJarPaths[i];
                String dependenciesPath = dependenciesPaths[i];

                File uberJarFile = new File(uberJarPath);
                File dependenciesDirectory = new File(dependenciesPath);

                if (!uberJarFile.exists() || !dependenciesDirectory.exists()) {
                    writer.println("Skipping project '" + projectName + "' due to missing files.");
                    writer.println();
                    continue;
                }

                // Set to store all packages in the Uber-JAR
                Set<String> uberJarPackages = new HashSet<>();

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
                int totalPackagesInDependencies = 0;
                int totalMissingPackages = 0;

                if (dependenciesDirectory.isDirectory()) {
                    File[] jarFiles = dependenciesDirectory.listFiles((dir, name) -> name.endsWith(".jar"));

                    if (jarFiles != null) {
                        for (File jarFile : jarFiles) {
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

                            // Update totals
                            totalPackagesInDependencies += dependencyPackages.size();
                            totalMissingPackages += missingPackages.size();
                        }
                    }
                }

                // Update grand totals
                grandTotalPackagesInDependencies += totalPackagesInDependencies;
                grandTotalMissingPackages += totalMissingPackages;

                // Write summary for this project
                writer.println("Project: " + projectName);
                writer.println("Total number of packages in all dependency JARs: " + totalPackagesInDependencies);
                writer.println("Total number of missing packages across all dependency JARs: " + totalMissingPackages);
                writer.println("Overall percentage of missing packages: " +
                        (totalPackagesInDependencies > 0
                                ? String.format("%.2f%%", (totalMissingPackages * 100.0) / totalPackagesInDependencies)
                                : "N/A"));
                writer.println(); // Blank line between projects
            }

            // Write grand total summary
            writer.println("===== Overall Summary =====");
            writer.println("Grand total number of packages in all dependency JARs: " + grandTotalPackagesInDependencies);
            writer.println("Grand total number of missing packages: " + grandTotalMissingPackages);
            writer.println("Overall percentage of missing packages: " +
                    (grandTotalPackagesInDependencies > 0
                            ? String.format("%.2f%%", (grandTotalMissingPackages * 100.0) / grandTotalPackagesInDependencies)
                            : "N/A"));
        }

        System.out.println("Batch missing package summary has been written to: " + outputPath);
    }
}
