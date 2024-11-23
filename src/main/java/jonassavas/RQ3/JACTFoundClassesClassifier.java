package jonassavas.RQ3;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


// Checks for dependency classes found in JACT
public class JACTFoundClassesClassifier {


   public static void main(String[] args) throws IOException {
       String rootDirectoryPath = "C:\\kthcs\\MEX\\CompleteJactResults\\poi-tl_jact-report\\dependencies";
       String jarFilePath = "C:\\kthcs\\MEX\\RESULTS\\poi-tl\\poi-tl\\target\\poi-tl-1.12.2-SNAPSHOT-shaded.jar";
       String logFilePath = "./JACT_Class_Types/poi-tl.txt"; // Output log file


       File resultFile = new File(logFilePath);


       File parentDir = resultFile.getParentFile();
       if (parentDir != null && !parentDir.exists()) {
           parentDir.mkdirs(); // This will create the necessary directories
       }


       // Set to store the class names gathered from HTML files
       Set<String> htmlGeneratedClasses = new HashSet<>();


       // Process the directory to extract class names
       processDirectory(new File(rootDirectoryPath), "", htmlGeneratedClasses);


       // Compare with JAR entries and log output to file
       checkJarFile(jarFilePath, htmlGeneratedClasses, logFilePath);
   }


   private static void processDirectory(File dir, String relativePath, Set<String> htmlGeneratedClasses) {
       if (!dir.isDirectory()) {
           return;
       }


       File[] files = dir.listFiles();
       if (files == null) {
           return;
       }


       for (File file : files) {
           if (file.isDirectory()) {
               // Skip the 'jacoco-resources' directory
               if (file.getName().equals("jacoco-resources")) {
                   continue;
               }
               // Recur into the directory, appending the directory name to the relative path
               processDirectory(file, relativePath + file.getName() + ".", htmlGeneratedClasses);
           } else if (file.getName().endsWith(".html")) {
               String fileName = file.getName().replace(".html", "");
               // Skip 'index' and 'index.source'
               if (!fileName.equals("index") && !fileName.equals("index.source")) {
                   // Generate the class name format (replace '.' with '/')
                   String className = file.getParentFile().getName().replace(".", "/") + "/" + fileName + ".class";
                   htmlGeneratedClasses.add(className);
               }
           }
       }
   }


   private static void checkJarFile(String jarFilePath, Set<String> htmlGeneratedClasses, String logFilePath) throws IOException {
       // Open a file writer to log the output
       try (PrintWriter logWriter = new PrintWriter(new FileWriter(logFilePath))) {
           // Open the JAR file
           try (JarFile jarFile = new JarFile(jarFilePath)) {
               Set<String> jarClasses = new HashSet<>();


               // Counters for different types of found classes
               int interfaceFoundCount = 0;
               int abstractClassFoundCount = 0;
               int enumFoundCount = 0;
               int regularClassFoundCount = 0;
               int syntheticClassFoundCount = 0;
               int annotationFoundCount = 0;


               // Counters for found and not found classes
               int foundCount = 0;
               int notFoundCount = 0;


               // Iterate through the entries in the JAR file using Enumeration
               Enumeration<JarEntry> entries = jarFile.entries();
               while (entries.hasMoreElements()) {
                   JarEntry jarEntry = entries.nextElement();


                   // Skip directories, module-info, META-INF, and any meta-information files
                   if (jarEntry.isDirectory() || jarEntry.getName().equals("module-info.class") || jarEntry.getName().startsWith("META-INF/")) {
                       continue;
                   }


                   // Only process .class files
                   if (!jarEntry.getName().endsWith(".class")) {
                       continue;
                   }


                   jarClasses.add(jarEntry.getName());


                   // Check if the class exists in the HTML-generated class set
                   if (htmlGeneratedClasses.contains(jarEntry.getName())) {
                       foundCount++;


                       // Use ASM to analyze the class file
                       try (InputStream classStream = jarFile.getInputStream(jarEntry)) {
                           ClassReader classReader = new ClassReader(classStream);
                           int access = classReader.getAccess();


                           if ((access & Opcodes.ACC_INTERFACE) != 0) {
                               if ((access & Opcodes.ACC_ANNOTATION) != 0) {
                                   annotationFoundCount++;
                                   String message = "ANNOTATION found: " + jarEntry.getName();
                                   System.out.println(message);
                                   logWriter.println(message);
                               } else {
                                   interfaceFoundCount++;
                                   String message = "INTERFACE found: " + jarEntry.getName();
                                   System.out.println(message);
                                   logWriter.println(message);
                               }
                           } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
                               abstractClassFoundCount++;
                               String message = "ABSTRACT CLASS found: " + jarEntry.getName();
                               System.out.println(message);
                               logWriter.println(message);
                           } else if ((access & Opcodes.ACC_ENUM) != 0) {
                               enumFoundCount++;
                               String message = "ENUM found: " + jarEntry.getName();
                               System.out.println(message);
                               logWriter.println(message);
                           } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                               syntheticClassFoundCount++;
                               String message = "SYNTHETIC CLASS found: " + jarEntry.getName();
                               System.out.println(message);
                               logWriter.println(message);
                           } else {
                               regularClassFoundCount++;
                               // Print and log the regular class found
                               String message = "REGULAR CLASS found: " + jarEntry.getName();
                               System.out.println(message);
                               logWriter.println(message);
                           }
                       } catch (IOException e) {
                           String errorMessage = "Could not process class: " + jarEntry.getName();
                           System.err.println(errorMessage);
                           logWriter.println(errorMessage);
                       }
                   } else {
                       notFoundCount++;
                   }
               }


               // Print and log the summary
               String summary = String.format("\nSummary:\n" +
                       "Classes found in both JAR and HTML: %d\n" +
                       "Classes in JAR but not found in HTML: %d\n" +
                       "Interfaces found: %d\n" +
                       "Abstract classes found: %d\n" +
                       "Enums found: %d\n" +
                       "Annotations found: %d\n" +
                       "Synthetic classes found: %d\n" +
                       "Regular classes found: %d\n",
                       foundCount, notFoundCount, interfaceFoundCount, abstractClassFoundCount,
                       enumFoundCount, annotationFoundCount, syntheticClassFoundCount, regularClassFoundCount);


               System.out.println(summary);
               logWriter.println(summary);
           }
       }
   }
}