package jonassavas;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassChecker {

    public static void main(String[] args) throws IOException {
        String rootDirectoryPath = "C:\\kthcs\\MEX\\CompleteJactResults\\commons-validator_jact-report\\dependencies";
        String jarFilePath = "C:\\kthcs\\MEX\\RESULTS\\commons-validator\\target\\commons-validator-1.7-shaded.jar";

        // Set to store the class names gathered from HTML files
        Set<String> htmlGeneratedClasses = new HashSet<>();

        // Process the directory to extract class names
        processDirectory(new File(rootDirectoryPath), "", htmlGeneratedClasses);

        // Compare with JAR entries
        checkJarFile(jarFilePath, htmlGeneratedClasses);
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

    private static void checkJarFile(String jarFilePath, Set<String> htmlGeneratedClasses) throws IOException {
        // Open the JAR file
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Set<String> jarClasses = new HashSet<>();

            // Counters for different types of missing classes
            int interfaceMissedCount = 0;
            int abstractClassMissedCount = 0;
            int enumMissedCount = 0;
            int regularClassMissedCount = 0;
            int syntheticClassMissedCount = 0;
            int annotationMissedCount = 0;

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
                if (!htmlGeneratedClasses.contains(jarEntry.getName())) {
                    notFoundCount++;

                    // Use ASM to analyze the class file
                    try (InputStream classStream = jarFile.getInputStream(jarEntry)) {
                        ClassReader classReader = new ClassReader(classStream);
                        int access = classReader.getAccess();

                        if ((access & Opcodes.ACC_INTERFACE) != 0) {
                            if ((access & Opcodes.ACC_ANNOTATION) != 0) {
                                annotationMissedCount++;
                                System.out.println("ANNOTATION missed: " + jarEntry.getName());
                            } else {
                                interfaceMissedCount++;
                                System.out.println("INTERFACE missed: " + jarEntry.getName());
                            }
                        } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
                            abstractClassMissedCount++;
                            System.out.println("ABSTRACT CLASS missed: " + jarEntry.getName());
                        } else if ((access & Opcodes.ACC_ENUM) != 0) {
                            enumMissedCount++;
                            System.out.println("ENUM missed: " + jarEntry.getName());
                        } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                            syntheticClassMissedCount++;
                            System.out.println("SYNTHETIC CLASS missed: " + jarEntry.getName());
                        } else {
                            regularClassMissedCount++;
                            System.out.println("REGULAR CLASS missed: " + jarEntry.getName());
                        }
                    } catch (IOException e) {
                        System.err.println("Could not process class: " + jarEntry.getName());
                    }
                } else {
                    foundCount++;
                }
            }

            // Print the summary
            System.out.println("\nSummary:");
            System.out.println("Classes found in both JAR and HTML: " + foundCount);
            System.out.println("Classes in JAR but not found in HTML: " + notFoundCount);
            System.out.println("Interfaces missed: " + interfaceMissedCount);
            System.out.println("Abstract classes missed: " + abstractClassMissedCount);
            System.out.println("Enums missed: " + enumMissedCount);
            System.out.println("Annotations missed: " + annotationMissedCount);
            System.out.println("Synthetic classes missed: " + syntheticClassMissedCount);
            System.out.println("Regular classes missed: " + regularClassMissedCount);
        }
    }
}
