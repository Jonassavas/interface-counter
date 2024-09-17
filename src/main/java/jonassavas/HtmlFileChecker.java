package jonassavas;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class HtmlFileChecker {

    static int printFirstNumber = 0;
    static int printSecondNumber = 0;

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
    public static boolean compareNumbersInHtml(String htmlContent) {
        // Regex to match the Total row and extract numbers
        Pattern pattern = Pattern.compile("<tr>\\s*<td>Total</td>\\s*<td[^>]*>(\\d+) of (\\d+)</td>");
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            int firstNumber = Integer.parseInt(matcher.group(1));
            int secondNumber = Integer.parseInt(matcher.group(2));
            printFirstNumber = firstNumber;
            printSecondNumber = secondNumber;
            // Compare the two numbers
            //System.out.println("NUMBERs:" + firstNumber + ", " + secondNumber);
            return firstNumber == secondNumber;
        }
        return true; // If no match is found, we assume it's valid
    }

    // Main method to iterate through the HTML files and check against the txt file
    public static void checkHtmlFiles(String htmlDirPath, String txtFilePath) throws IOException {
        File htmlDir = new File(htmlDirPath);
        if (!htmlDir.isDirectory()) {
            System.out.println("Provided path is not a directory: " + htmlDirPath);
            return;
        }

        // Read the lines from the txt file into a list
        List<String> txtFileLines = readLinesFromFile(txtFilePath);

        // Get all HTML files from the directory
        File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.endsWith(".html"));

        if (htmlFiles == null || htmlFiles.length == 0) {
            System.out.println("No HTML files found in the directory: " + htmlDirPath);
            return;
        }

        // Iterate through each HTML file
        for (File htmlFile : htmlFiles) {
            // Strip the .html extension
            String htmlBaseName = stripExtension(htmlFile.getName());

            // Check if the htmlBaseName is found in any line of the txt file (ignoring extensions)
            boolean found = txtFileLines.stream()
                                        .anyMatch(line -> line.contains(htmlBaseName));

            // If not found, proceed with checking the HTML content
            if (!found) {
                // Read the HTML content up to <tfoot>
                String htmlContent = readHtmlFileUpToTfoot(htmlFile);

                // Compare the numbers from the "Total" row
                boolean numbersMatch = compareNumbersInHtml(htmlContent);

                // If numbers don't match, print the message
                if (!numbersMatch) {
                    System.out.println(htmlFile.getName() + " - Total numbers do not match." + " : COVERAGE:" + printFirstNumber + ", " + printSecondNumber);
                }
            }
        }
    }

    public static void main(String[] args) {
        String htmlDirPath = "C:\\kthcs\\MEX\\CompleteJactResults\\commons-validator_jact-report\\dependencies\\commons.logging.commons.logging-v1.2\\org.apache.commons.logging.impl";
        String txtFilePath = "C:\\kthcs\\MEX\\rq3\\debloated\\commons-validator\\logging.txt";

        try {
            checkHtmlFiles(htmlDirPath, txtFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
