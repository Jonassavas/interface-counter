package jonassavas.RQ2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class JarHtmlComparator {
    public static void main(String[] args) throws IOException {
        String jarDirPath = "C:\\kthcs\\MEX\\RQ2Gathering\\jooby\\jooby\\dependencies";
        String htmlReportPath = "C:\\kthcs\\MEX\\CompleteJactResults\\jooby_jact-report\\dependencies";
        String outputFilePath = "./RQ2-correct/jooby.txt";

        try (FileWriter fileWriter = new FileWriter(outputFilePath, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            Map<String, Map<String, Set<String>>> jarData = extractJarData(jarDirPath);
            validateAgainstHtmlReport(jarData, htmlReportPath, printWriter);
        }
    }

    public static Map<String, Map<String, Set<String>>> extractJarData(String dirPath) throws IOException {
        Map<String, Map<String, Set<String>>> jarData = new HashMap<>();
        File dir = new File(dirPath);
        File[] jarFiles = dir.listFiles((d, name) -> name.endsWith(".jar"));

        if (jarFiles == null) return jarData;

        for (File jarFile : jarFiles) {
            String jarName = jarFile.getName();
            String baseName = jarName.substring(0, jarName.lastIndexOf(".jar"));
            
            int lastDash = baseName.lastIndexOf('-');
            String searchableName = (lastDash != -1)
                ? baseName.substring(0, lastDash).replace('-', '.') + "-v" + baseName.substring(lastDash + 1)
                : baseName.replace('-', '.');

            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String classPath = entry.getName();
                        int lastSlash = classPath.lastIndexOf('/');
                        String packageName = (lastSlash == -1) ? "(default)" : classPath.substring(0, lastSlash).replace("/", ".");
                        String className = classPath.substring(lastSlash + 1);
                        jarData.computeIfAbsent(searchableName, k -> new HashMap<>())
                              .computeIfAbsent(packageName, k -> new HashSet<>())
                              .add(className);
                    }
                }
            }
        }
        return jarData;
    }

    public static void validateAgainstHtmlReport(Map<String, Map<String, Set<String>>> jarData, String reportPath, PrintWriter printWriter) throws IOException {
        File reportDir = new File(reportPath);
        if (!reportDir.isDirectory()) return;

        Map<String, Map<String, Integer>> affectedPackages = new HashMap<>();
        int totalMismatchedClasses = 0;
        int totalMatchedClasses = 0;
        int totalAffectedPackages = 0;
        int totalCorrectlyMatchedPackages = 0;  // New counter for correct packages

        for (File jarDir : Objects.requireNonNull(reportDir.listFiles(File::isDirectory))) {
            if (jarDir.getName().equals("jacoco-resources")) continue;

            String dirName = jarDir.getName();

            String matchedJar = jarData.keySet().stream()
                .filter(jarName -> {
                    int jarIndex = jarName.length() - 1;
                    int dirIndex = dirName.length() - 1;
                    while (jarIndex >= 0 && dirIndex >= 0) {
                        if (jarName.charAt(jarIndex) != dirName.charAt(dirIndex)) {
                            return false;
                        }
                        jarIndex--;
                        dirIndex--;
                    }
                    return jarIndex < 0;
                })
                .findFirst()
                .orElse(null);

            if (matchedJar == null) {
                printWriter.println("Could not find JAR for: " + dirName);
                System.out.println("Could not find JAR for: " + dirName);
                continue;
            }

            int dependencyMismatchCount = 0;

            for (File packageDir : Objects.requireNonNull(jarDir.listFiles(File::isDirectory))) {
                String packageName = packageDir.getName().replace("/", ".");
                if (!jarData.get(matchedJar).containsKey(packageName)) continue;

                Set<String> jarClasses = jarData.get(matchedJar).get(packageName);
                int packageMismatchCount = 0;

                for (File htmlFile : Objects.requireNonNull(packageDir.listFiles((d, name) -> name.endsWith(".html")))) {
                    if (htmlFile.getName().equals("index.html") || 
                        htmlFile.getName().equals("indirect-dependencies.html") ||
                        htmlFile.getName().equals("index.source.html")) continue;

                    String className = htmlFile.getName().replace(".html", ".class");
                    if (!jarClasses.contains(className)) {
                        printWriter.println("Mismatched class: " + className + " in " + matchedJar + " (package: " + packageName + ")");
                        packageMismatchCount++;
                        totalMismatchedClasses++;
                    } else {
                        totalMatchedClasses++;
                    }
                }

                if (packageMismatchCount > 0) {
                    affectedPackages.computeIfAbsent(matchedJar, k -> new HashMap<>()).put(packageName, packageMismatchCount);
                    dependencyMismatchCount += packageMismatchCount;
                    totalAffectedPackages++;
                } else {
                    totalCorrectlyMatchedPackages++;  // Increment for fully matched package
                }
            }

            if (dependencyMismatchCount > 0) {
                printWriter.println("Total mismatched classes for " + matchedJar + ": " + dependencyMismatchCount);
                System.out.println("Total mismatched classes for " + matchedJar + ": " + dependencyMismatchCount);
            }
        }

        printWriter.println("\nSummary of affected Java packages:");
        System.out.println("\nSummary of affected Java packages:");
        affectedPackages.forEach((dependency, packages) -> {
            printWriter.println("Dependency: " + dependency);
            System.out.println("Dependency: " + dependency);
            packages.forEach((pkg, count) -> {
                printWriter.println("  - " + pkg + " (" + count + " mismatched classes)");
                System.out.println("  - " + pkg + " (" + count + " mismatched classes)");
            });
        });

        printWriter.println("\nTotal mismatched classes: " + totalMismatchedClasses);
        printWriter.println("Total correctly matched classes: " + totalMatchedClasses);
        printWriter.println("Total affected packages with mismatches: " + totalAffectedPackages);
        printWriter.println("Total correctly matched packages: " + totalCorrectlyMatchedPackages);  // New summary line

        System.out.println("\nTotal mismatched classes: " + totalMismatchedClasses);
        System.out.println("Total correctly matched classes: " + totalMatchedClasses);
        System.out.println("Total affected packages with mismatches: " + totalAffectedPackages);
        System.out.println("Total correctly matched packages: " + totalCorrectlyMatchedPackages);  // New summary line
    }
}
