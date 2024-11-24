package jonassavas.RQ3;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

public class IncludedAndJactOnly {
    static int nrClassFiles = 0;
    static int nrClassFiles2 = 0;
    static Map<String, int[]> includedMap = new HashMap<>();
    static Map<String, int[]> mismatchMap = new HashMap<>();
    private static int interfaceCount = 0, abstractCount = 0, enumCount = 0, regularCount = 0, annotationCount = 0, syntheticCount = 0;

    public static String stripExtension(String filename) {
        if (filename.lastIndexOf('.') > 0) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename;
    }

    public static List<String> readClassNamesFromTxtFiles(String txtDirPath) throws IOException {
        List<String> classNames = new ArrayList<>();
        File txtDir = new File(txtDirPath);

        File[] txtFiles = txtDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (txtFiles == null) {
            return classNames;
        }

        for (File txtFile : txtFiles) {
            List<String> lines = Files.readAllLines(txtFile.toPath());
            for (int i = 0; i < lines.size() - 1; i++) {
                classNames.add(lines.get(i).trim());
            }
        }
        return classNames;
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

    public static String generateClassName(File htmlFile) {
        String relativePath = htmlFile.getParentFile().getName().replace(".", "/") + "/" + stripExtension(htmlFile.getName()) + ".class";
        return relativePath;
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

    public static void checkHtmlFiles(String htmlDirPath, List<String> txtClassNames) throws IOException {
        File htmlDir = new File(htmlDirPath);
        if (!htmlDir.isDirectory()) {
            System.out.println("Provided path is not a directory: " + htmlDirPath);
            return;
        }

        List<File> htmlFiles = new ArrayList<>();
        processDirectoriesRecursively(htmlDir, htmlFiles);

        if (htmlFiles.isEmpty()) {
            System.out.println("No HTML files found in the directory: " + htmlDirPath);
            return;
        }

        for (File htmlFile : htmlFiles) {
            String fileName = stripExtension(htmlFile.getName());
            if (fileName.equals("index") || fileName.equals("index.source") || fileName.equals("indirect-dependencies")) {
                continue;
            }

            String fullClassName = generateClassName(htmlFile);
            boolean found = txtClassNames.stream().anyMatch(className -> className.equals(fullClassName));
            String htmlContent = readHtmlFileUpToTfoot(htmlFile);
            int[] numbers = extractNumbersInHtml(htmlContent);
            boolean numbersMatch = (numbers[0] == numbers[1]);

            if (found) {
                if (!numbersMatch) {
                    includedMap.put(fullClassName, numbers);
                    System.out.println(htmlFile.getName() + " - Included class by both tools: " + numbers[0] + ", " + numbers[1]);
                }
            } else {
                if (!numbersMatch) {
                    mismatchMap.put(fullClassName, numbers);
                    System.out.println(htmlFile.getName() + " - Mismatched class: " + numbers[0] + ", " + numbers[1]);
                }
            }
        }
    }

    public static void checkJarFile(String jarFilePath, Map<String, int[]> map, PrintWriter logWriter) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) continue;

                if (map.containsKey(jarEntry.getName())) {
                    int[] numbers = map.get(jarEntry.getName());

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

                        String message = String.format("%s: %s - JACT (NotCovered, Total): %d, %d",
                                classType, jarEntry.getName(), numbers[0], numbers[1]);
                        logWriter.println(message);

                    } catch (IOException e) {
                        logWriter.println("Could not process class: " + jarEntry.getName());
                    }
                }
            }
        }
    }

    public static void processAllDependencies(String htmlBaseDir, String txtBaseDir, String jarFilePath, String logFilePath, String mismatchFilePath) throws IOException {
        List<String> txtClassNames = readClassNamesFromTxtFiles(txtBaseDir);
        File includedFile = new File(logFilePath);
        File mismatchFile = new File(mismatchFilePath);
    
        File parentDirIncluded = includedFile.getParentFile();
        if (parentDirIncluded != null && !parentDirIncluded.exists()) {
            parentDirIncluded.mkdirs();
        }
    
        File parentDirMismatch = mismatchFile.getParentFile();
        if (parentDirMismatch != null && !parentDirMismatch.exists()) {
            parentDirMismatch.mkdirs();
        }
    
        try (PrintWriter includedWriter = new PrintWriter(new FileWriter(logFilePath));
             PrintWriter mismatchWriter = new PrintWriter(new FileWriter(mismatchFilePath))) {
    
            File htmlRootDir = new File(htmlBaseDir);
            if (!htmlRootDir.isDirectory()) {
                System.out.println("Invalid HTML base directory: " + htmlBaseDir);
                return;
            }
    
            File[] dependencyDirs = htmlRootDir.listFiles(File::isDirectory);
            if (dependencyDirs == null || dependencyDirs.length == 0) {
                System.out.println("No dependencies found in directory: " + htmlBaseDir);
                return;
            }
    
            for (File dependencyDir : dependencyDirs) {
                if (dependencyDir.getName().equals("jacoco-resources")) {
                    continue;
                }
    
                System.out.println("Processing dependency: " + dependencyDir.getName());
                checkHtmlFiles(dependencyDir.getAbsolutePath(), txtClassNames);
            }
    
            // Process includedMap
            resetCounters(); // Reset counters before processing
            checkJarFile(jarFilePath, includedMap, includedWriter);
            String includedSummary = generateSummary();
            includedWriter.println("\nSummary for Included Classes By Both Tools:");
            includedWriter.println(includedSummary);
            System.out.println("Summary for Included Classes By Both Tools:\n" + includedSummary);
    
            // Process mismatchMap
            resetCounters(); // Reset counters before processing
            checkJarFile(jarFilePath, mismatchMap, mismatchWriter);
            String mismatchSummary = generateSummary();
            mismatchWriter.println("\nSummary for Class Files Only In JACT:");
            mismatchWriter.println(mismatchSummary);
            System.out.println("Summary for Class Files Only In JACT:\n" + mismatchSummary);
        }
    }
    
    // Method to reset counters
    private static void resetCounters() {
        interfaceCount = 0;
        abstractCount = 0;
        enumCount = 0;
        annotationCount = 0;
        syntheticCount = 0;
        regularCount = 0;
    }
    
    // Method to generate a summary string
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
        String htmlBaseDir = "C:\\kthcs\\MEX\\CompleteJactResultsForRQ3\\guice-core_jact-report\\dependencies";
        String txtBaseDir = "C:\\kthcs\\MEX\\RQ3-Data\\DepTrim_Debloated\\guice";
        String jarFilePath = "C:\\Users\\jonas\\Downloads\\guice-5.1.0-shaded.jar";
        String includedFilePath = "./Included_By_Both_Tools/guice.txt";
        String mismatchFilePath = "./JACT_Only_Included/guice.txt";

        try {
            processAllDependencies(htmlBaseDir, txtBaseDir, jarFilePath, includedFilePath, mismatchFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String logOnlyInDepTrim = "./DepTrim_Only_Included/guice.txt";
        CompareTxtAndHtmlFilesWithCount.run(htmlBaseDir, txtBaseDir, jarFilePath, logOnlyInDepTrim);
    }
}
