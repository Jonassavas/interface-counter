package jonassavas.RQ3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompareTxtAndHtmlFilesWithCount {
    private static int interfaceCount = 0, abstractCount = 0, enumCount = 0, regularCount = 0, annotationCount = 0, syntheticCount = 0;

    public static String stripExtension(String filename) {
        if (filename.lastIndexOf('.') > 0) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename;
    }

    public static String generateClassName(File htmlFile) {
        String relativePath = htmlFile.getParentFile().getName().replace(".", "/") + "/" + stripExtension(htmlFile.getName()) + ".class";
        return relativePath;
    }

    public static List<String> readClassNamesFromTxtFiles(String txtDirPath, int[] totalCountFromTxts) throws IOException {
        List<String> classNames = new ArrayList<>();
        File txtDir = new File(txtDirPath);

        File[] txtFiles = txtDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles == null) {
            return classNames;
        }

        for (File txtFile : txtFiles) {
            List<String> lines = Files.readAllLines(txtFile.toPath());
            if (lines.isEmpty()) continue;

            // Extract and accumulate the total count from the last line
            String lastLine = lines.get(lines.size() - 1).trim();
            try {
                // Use a regex to extract the number after "Number of .class files:"
                Pattern pattern = Pattern.compile("Number of \\.class files: (\\d+)");
                Matcher matcher = pattern.matcher(lastLine);
                if (matcher.find()) {
                    int countFromLastLine = Integer.parseInt(matcher.group(1));
                    totalCountFromTxts[0] += countFromLastLine;
                } else {
                    System.out.println("Warning: Could not extract number from the last line of " + txtFile.getName());
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Last line of " + txtFile.getName() + " is not a valid number.");
            }

            // Add all lines except the last one
            for (int i = 0; i < lines.size() - 1; i++) {
                classNames.add(lines.get(i).trim());
            }
        }
        return classNames;
    }

    public static void processDirectoriesRecursively(File dir, List<File> htmlFiles) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectoriesRecursively(file, htmlFiles);
            } else if (file.getName().endsWith(".html")) {
                htmlFiles.add(file);
            }
        }
    }

    public static String readHtmlFileUpToTfoot(File htmlFile) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
                if (line.contains("<tfoot>")) {
                    break;
                }
            }
        }
        Document doc = Jsoup.parse(content.toString());
        return doc.html();
    }

    public static int[] extractNumbersInHtml(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        String totalRow = doc.select("tr:contains(Total)").html();
        if (totalRow != null) {
            Pattern pattern = Pattern.compile("([\\d,]+) of ([\\d,]+)");
            Matcher matcher = pattern.matcher(totalRow);
            if (matcher.find()) {
                int firstNumber = Integer.parseInt(matcher.group(1).replaceAll(",", ""));
                int secondNumber = Integer.parseInt(matcher.group(2).replaceAll(",", ""));
                return new int[]{firstNumber, secondNumber};
            }
        }
        return new int[]{0, 0};
    }

    public static List<String> readClassNamesFromHtmlFiles(String htmlDirPath) throws IOException {
        List<String> htmlClassNames = new ArrayList<>();
        File htmlDir = new File(htmlDirPath);

        if (!htmlDir.isDirectory()) {
            System.out.println("Provided path is not a directory: " + htmlDirPath);
            return htmlClassNames;
        }

        List<File> htmlFiles = new ArrayList<>();
        processDirectoriesRecursively(htmlDir, htmlFiles);

        for (File htmlFile : htmlFiles) {
            String fileName = stripExtension(htmlFile.getName());
            if (fileName.equals("index") || fileName.equals("index.source") || fileName.equals("indirect-dependencies")) {
                continue;
            }

            String fullClassName = generateClassName(htmlFile);
            String htmlContent = readHtmlFileUpToTfoot(htmlFile);
            int[] numbers = extractNumbersInHtml(htmlContent);
            boolean numbersMatch = (numbers[0] == numbers[1]);
            if (!numbersMatch) {
                htmlClassNames.add(fullClassName);
                //System.out.println(htmlFile.getName() + " - Covered by JACT: " + numbers[0] + ", " + numbers[1]);
            }
        }
        return htmlClassNames;
    }

    public static void checkJarFile(String jarFilePath, List<String> missingClassNames, PrintWriter logWriter) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) continue;

                if (missingClassNames.contains(jarEntry.getName())) {

                    try (InputStream classStream = jarFile.getInputStream(jarEntry)) {
                        ClassReader classReader = new ClassReader(classStream);
                        int access = classReader.getAccess();
                        String classType = "";

                        if ((access & Opcodes.ACC_INTERFACE) != 0) {
                            if ((access & Opcodes.ACC_ANNOTATION) != 0) {
                                classType = "ANNOTATION";
                                annotationCount++;
                            } else {
                                classType = "INTERFACE";
                                interfaceCount++;
                            }
                        } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
                            classType = "ABSTRACT CLASS";
                            abstractCount++;
                        } else if ((access & Opcodes.ACC_ENUM) != 0) {
                            classType = "ENUM";
                            enumCount++;
                        } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                            classType = "SYNTHETIC CLASS";
                            syntheticCount++;
                        } else {
                            classType = "REGULAR CLASS";
                            regularCount++;
                        }

                        String message = String.format("%s: %s",
                                classType, jarEntry.getName());
                        logWriter.println(message);

                    } catch (IOException e) {
                        logWriter.println("Could not process class: " + jarEntry.getName());
                    }
                }
            }
        }
    }

    public static void compareAndLogDifferences(String txtBaseDir, String htmlBaseDir, String jarFilePath, String outputFilePath) throws IOException {
        int[] totalCountFromTxts = {0}; // Array to hold the cumulative total count from TXT files
        List<String> txtClassNames = readClassNamesFromTxtFiles(txtBaseDir, totalCountFromTxts);
        List<String> htmlClassNames = readClassNamesFromHtmlFiles(htmlBaseDir);

        // Convert HTML class names to a set for quick lookup
        Set<String> htmlClassNamesSet = new HashSet<>(htmlClassNames);

        // Find all TXT entries not in the HTML file set
        List<String> missingInHtml = new ArrayList<>();
        for (String txtClassName : txtClassNames) {
            if (!htmlClassNamesSet.contains(txtClassName)) {
                missingInHtml.add(txtClassName);
            }
        }

        // Write results to a file
        File outputFile = new File(outputFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
            // for (String className : missingInHtml) {
            //     writer.println(className);
            // }
        
            // Process the JAR file and classify the classes
            checkJarFile(jarFilePath, missingInHtml, writer);
            
            writer.println("\nTotal number of class files only in DepTrim: " + missingInHtml.size());
            writer.println("Total number of class files in DepTrim: " + totalCountFromTxts[0]);
            String summary = generateSummary();
            writer.println(summary);
            System.out.println(summary);
        }

        // Print summary to console
        System.out.println("Comparison complete. Results saved to " + outputFilePath);
        System.out.println("Summary:");
        System.out.println("Total number of class files only in DepTrim: " + missingInHtml.size());
        System.out.println("Total number of class files in DepTrim: " + totalCountFromTxts[0]);
    }

    private static String generateSummary() {
        return String.format("Interfaces found: %d\n" +
                        "Abstract classes found: %d\n" +
                        "Enums found: %d\n" +
                        "Annotations found: %d\n" +
                        "Synthetic classes found: %d\n" +
                        "Regular classes found: %d",
                interfaceCount, abstractCount, enumCount, annotationCount, syntheticCount, regularCount);
    }

    public static void main(String[] args) {
        String htmlBaseDir = "C:\\kthcs\\MEX\\CompleteJactResultsForRQ3\\woodstox_jact-report\\dependencies";
        String txtBaseDir = "C:\\kthcs\\MEX\\RQ3-Data\\DepTrim_Debloated\\woodstox";
        String jarFilePath = "C:\\kthcs\\MEX\\RESULTS\\woodstox\\target\\woodstox-core-6.4.0-shaded.jar";
        String outputFilePath = "./ComparisonResults/MissingInHtmlWithCounts.txt";

        try {
            compareAndLogDifferences(txtBaseDir, htmlBaseDir, jarFilePath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void run(String htmlBaseDir, String txtBaseDir, String jarFilePath, String outputFilePath) {
        try {
            compareAndLogDifferences(txtBaseDir, htmlBaseDir, jarFilePath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
