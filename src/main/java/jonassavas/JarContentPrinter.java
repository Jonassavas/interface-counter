package jonassavas;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.*;

public class JarContentPrinter {

    // Method to process all JAR files in the input directory and write their .class files to txt files
    public static void processJarFiles(String jarDirPath, String outputDirPath) throws IOException {
        File jarDir = new File(jarDirPath);
        File[] jarFiles = jarDir.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No JAR files found in the directory: " + jarDirPath);
            return;
        }
        
        // Loop over each JAR file in the directory
        for (File jarFile : jarFiles) {
            String jarFileName = jarFile.getName().replace(".jar", "");
            File outputFile = new File(outputDirPath, jarFileName + ".txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                // Process the JAR file and write class files to output file
                listClassFilesInJar(jarFile, writer);
            }
        }
    }

    // Method to extract .class files from the JAR and write to a txt file, including count of class files
    private static void listClassFilesInJar(File jarFile, BufferedWriter writer) throws IOException {
        int classCount = 0;

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                // Only process .class files
                if (entryName.endsWith(".class")) {
                    writer.write(entryName);
                    writer.newLine();
                    classCount++;
                }
            }
        }

        // Append the count of class files at the end
        writer.write("Number of .class files: " + classCount);
        writer.newLine();
        System.out.println("Processed " + jarFile.getName() + " with " + classCount + " .class files.");
    }

    public static void main(String[] args) {
        String jarDirPath = "/home/jonassavas/deptrim-experiments/pipeline/results/poi-tl/poi-tl/deptrim/libs-specialized/";
        String outputDirPath = "./rq3Debloated/poi-tl/";

        // Create the output directory if it doesn't exist
        File resultDir = new File(outputDirPath);
        if (!resultDir.exists()) {
            resultDir.mkdirs(); // This will create the output directory
        }

        try {
            processJarFiles(jarDirPath, outputDirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
