package jonassavas.RQ2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JACTJavaPackages {
    public static void main(String[] args) {
        // Replace these with your actual 30 entries
        String[] paths = {
            "C:\\kthcs\\MEX\\CompleteJactResults\\checkstyle_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\chronicle-map_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\classgraph_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\commons-validator_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\corenlp_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\flink-flink-java_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\graphhopper-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\guice-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\helidon-openapi_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\httpcomponents-client-httpclient5_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\immutables-gson_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\jacop_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\java-faker_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\jcabi-github_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\jimfs-jimfs_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\jooby_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\lettuce-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\modelmapper-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\mybatis-3_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\openpdf-openpdf_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\pdfbox-pdfbox_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\pf4j-pf4j_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\poi-tl_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\recaf_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\rxrelay_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\scribejava-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\tablesaw-json_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\tika-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\undertow-core_jact-report\\dependencies",
            "C:\\kthcs\\MEX\\CompleteJactResults\\woodstox_jact-report\\dependencies"
        };

        String[] names = {
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

        String outputFilePath = "./java-package-counts.txt";

        if (paths.length != names.length) {
            System.err.println("Error: paths and names arrays must have the same length.");
            return;
        }

        int totalPackages = 0; // <-- Keep track of total across all projects

        try (FileWriter fileWriter = new FileWriter(outputFilePath);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            for (int i = 0; i < paths.length; i++) {
                String basePath = paths[i];
                String name = names[i];

                File baseDir = new File(basePath);
                if (!baseDir.exists() || !baseDir.isDirectory()) {
                    System.out.println("Skipping invalid path: " + basePath);
                    printWriter.println(name + ": INVALID PATH");
                    continue;
                }

                int packageCount = 0;

                File[] dependencyDirs = baseDir.listFiles(File::isDirectory);
                if (dependencyDirs != null) {
                    for (File dep : dependencyDirs) {
                        if (dep.getName().equals("jacoco-resources")) continue;

                        File[] packageDirs = dep.listFiles(File::isDirectory);
                        if (packageDirs != null) {
                            for (File pkg : packageDirs) {
                                if (!pkg.getName().equals("jacoco-resources")) {
                                    packageCount++;
                                }
                            }
                        }
                    }
                }

                totalPackages += packageCount; // <-- Add to total

                printWriter.println(name + ": " + packageCount + " Java packages");
                System.out.println(name + ": " + packageCount + " Java packages");
            }

            // Print and write the total count
            printWriter.println("\nTotal Java packages across all projects: " + totalPackages);
            System.out.println("\nTotal Java packages across all projects: " + totalPackages);

        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
