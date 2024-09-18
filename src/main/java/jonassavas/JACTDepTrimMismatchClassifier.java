package jonassavas;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

public class JACTDepTrimMismatchClassifier {

    // Map to store mismatched class names with their corresponding numbers
    static Map<String, int[]> mismatchMap = new HashMap<>();

    // Method to remove the file extension (e.g. .html) from the filename
    public static String stripExtension(String filename) {
        if (filename.lastIndexOf('.') > 0) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename;
    }

    // Method to read all lines from the txt file into a List
    public static List<String> readLinesFromFile(String txtFilePath) throws IOException {
        return Files.readAllLines(Paths.get(txtFilePath));
    }

    // Method to read the HTML file up to the <tfoot> section
    public static String readHtmlFileUpToTfoot(File htmlFile) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(htmlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
                if (line.contains("<tfoot>")) {
                    break; // Stop reading once we reach <tfoot>
                }
            }
        }
        return content.toString();
    }

    // Method to extract and compare the first set of numbers after "Total" in the HTML content
    public static int[] extractNumbersInHtml(String htmlContent) {
        // Regex to match the Total row and extract numbers
        Pattern pattern = Pattern.compile("<tr>\\s*<td>Total</td>\\s*<td[^>]*>(\\d+) of (\\d+)</td>");
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            int firstNumber = Integer.parseInt(matcher.group(1));
            int secondNumber = Integer.parseInt(matcher.group(2));
            return new int[]{firstNumber, secondNumber};
        }
        return new int[]{0, 0}; // Default value if no match is found
    }

    // Method to process and store mismatched class names
    public static String generateClassName(File htmlFile) {
        String relativePath = htmlFile.getParentFile().getName().replace(".", "/") + "/" + stripExtension(htmlFile.getName()) + ".class";
        return relativePath;
    }

    // Recursively process directories and locate HTML files down multiple layers
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

    // Main method to iterate through the HTML files and check against the txt file
    public static void checkHtmlFiles(String htmlDirPath, String txtFilePath) throws IOException {
        File htmlDir = new File(htmlDirPath);
        if (!htmlDir.isDirectory()) {
            System.out.println("Provided path is not a directory: " + htmlDirPath);
            return;
        }

        List<String> txtFileLines = readLinesFromFile(txtFilePath);
        List<File> htmlFiles = new ArrayList<>();
        processDirectoriesRecursively(htmlDir, htmlFiles); // Recursively fetch HTML files

        if (htmlFiles.isEmpty()) {
            System.out.println("No HTML files found in the directory: " + htmlDirPath);
            return;
        }

        for (File htmlFile : htmlFiles) {
            String htmlBaseName = stripExtension(htmlFile.getName());

            // Skip files named "index" and "index.source"
            if (htmlBaseName.equals("index") || htmlBaseName.equals("index.source")) {
                continue;
            }

            boolean found = txtFileLines.stream().anyMatch(line -> line.contains(htmlBaseName));

            if (!found) {
                String htmlContent = readHtmlFileUpToTfoot(htmlFile);
                int[] numbers = extractNumbersInHtml(htmlContent);
                boolean numbersMatch = (numbers[0] == numbers[1]);

                if (!numbersMatch) {
                    String className = generateClassName(htmlFile);
                    mismatchMap.put(className, numbers); // Store mismatched class name and its numbers
                    System.out.println(htmlFile.getName() + " - Total numbers do not match: " + numbers[0] + ", " + numbers[1]);
                }
            }
        }
    }

    // Method to check JAR file and classify the mismatched classes
    public static void checkJarFile(String jarFilePath, String logFilePath) throws IOException {
        try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFilePath))) {
            try (JarFile jarFile = new JarFile(jarFilePath)) {
                int interfaceCount = 0, abstractCount = 0, enumCount = 0, regularCount = 0, annotationCount = 0, syntheticCount = 0;

                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) continue;

                    if (mismatchMap.containsKey(jarEntry.getName())) {
                        int[] numbers = mismatchMap.get(jarEntry.getName());

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

                            // Log the class type and mismatch information with the correct numbers
                            String message = String.format("%s mismatch: %s - Total numbers do not match: %d, %d",
                                    classType, jarEntry.getName(), numbers[0], numbers[1]);
                            System.out.println(message);
                            logWriter.println(message);

                        } catch (IOException e) {
                            logWriter.println("Could not process class: " + jarEntry.getName());
                        }
                    }
                }

                // Log summary
                String summary = String.format("\nSummary:\n" +
                        "Interfaces found: %d\n" +
                        "Abstract classes found: %d\n" +
                        "Enums found: %d\n" +
                        "Annotations found: %d\n" +
                        "Synthetic classes found: %d\n" +
                        "Regular classes found: %d\n",
                        interfaceCount, abstractCount, enumCount, annotationCount, syntheticCount, regularCount);
                System.out.println(summary);
                logWriter.println(summary);
            }
        }
    }

    public static void main(String[] args) {
        String htmlDirPath = "C:\\kthcs\\MEX\\CompleteJactResults\\modelmapper-core_jact-report\\dependencies\\net.bytebuddy.byte.buddy.dep-v1.12.3";
        String txtFilePath = "C:\\kthcs\\MEX\\RQ3-Data\\DepTrimResults\\debloated\\modelmapper\\byte-buddy.txt";
        String jarFilePath = "C:\\kthcs\\MEX\\RESULTS\\modelmapper\\core\\target\\modelmapper-3.1.1-shaded.jar";
        String logFilePath = "./DepTrim_Mismatch_Types/modelmapper/byte-buddy.txt";

        File resultFile = new File(logFilePath);

        File parentDir = resultFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // This will create the necessary directories
        }

        try {
            // Step 1: Check HTML files
            checkHtmlFiles(htmlDirPath, txtFilePath);

            // Step 2: Check mismatched classes in JAR
            checkJarFile(jarFilePath, logFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
