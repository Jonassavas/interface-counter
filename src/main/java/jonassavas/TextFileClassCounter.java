package jonassavas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileClassCounter {

    public static void main(String[] args) {
        // Specify the directory containing the .txt files
        String directoryPath = "C:\\kthcs\\MEX\\RQ3-Data\\DepTrim_Debloated\\woodstox"; 

        try {
            File folder = new File(directoryPath);
            File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

            int totalClassFiles = 0;

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    totalClassFiles += processFile(file);
                }
            }

            System.out.println("Total .class files across all .txt files: " + totalClassFiles);

        } catch (IOException e) {
            System.out.println("An error occurred while reading files.");
            e.printStackTrace();
        }
    }

    private static int processFile(File file) throws IOException {
        int classFileCount = 0;
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        // The second to last line should be a class file, and the last line is the summary
        String summaryLine = lines.get(lines.size() - 1);
        classFileCount = extractClassCount(summaryLine);

        // Check for 'module-info.class' or any file starting with 'META-INF/' and reduce the count
        for (String line : lines) {
            if (line.contains("module-info.class") || line.startsWith("META-INF/")) {
                classFileCount--;
            }
        }

        System.out.println("Processed file: " + file.getName() + ", .class files: " + classFileCount);

        return classFileCount;
    }

    private static int extractClassCount(String summaryLine) {
        // Example: "Number of .class files: 27"
        String[] parts = summaryLine.split(":");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing class file count from summary line: " + summaryLine);
            }
        }
        return 0;
    }
}

