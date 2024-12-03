package jonassavas;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;


import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class InterfaceCounter
{
   public static void main(String[] args) throws IOException {


       String resultFilePath = "interfaces/woodstox/xsdlib.txt";
       File resultFile = new File(resultFilePath);


       File parentDir = resultFile.getParentFile();
       if (parentDir != null && !parentDir.exists()) {
           parentDir.mkdirs(); // This will create the necessary directories
       }


       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\classgraph\\target\\classgraph-4.8.154-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\java-faker\\target\\javafaker-1.0.2-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\modelmapper\\core\\target\\modelmapper-3.1.1-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\immutables\\gson\\target\\gson-2.9.3-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\commons-validator\\target\\commons-validator-1.7-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\tablesaw\\json\\target\\tablesaw-json-0.43.1-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\tika\\tika-core\\target\\tika-core-2.6.0-shaded.jar";
       //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\woodstox\\target\\woodstox-core-6.4.0-shaded.jar";
       //String jarPath = "/home/jonassavas/guice/core/target/guice-5.1.0-shaded.jar";


       // DepTrim JARS:
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/classgraph/deptrim/libs-specialized/jvm-driver-9.4.2.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/commons-validator/deptrim/libs-specialized/commons-beanutils-1.9.4.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/guice/core/deptrim/libs-specialized/aopalliance-1.0.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/immutables/gson/deptrim/libs-specialized/gson-2.8.8.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/java-faker/deptrim/libs-specialized/automaton-1.11-8.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/modelmapper/core/deptrim/libs-specialized/asm-9.4.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/tablesaw/json/deptrim/libs-specialized/json-flattener-0.8.1.jar";
       //String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/tika/tika-core/deptrim/libs-specialized/slf4j-api-2.0.3.jar";
       String jarPath = "/home/jonassavas/deptrim-experiments/pipeline/results/woodstox/deptrim/libs-specialized/xsdlib-2013.6.1.jar";


       Map<String, Integer> packageInterfaceCount = new HashMap<>();
      
       JarFile jarFile = new JarFile(jarPath);
       Enumeration<JarEntry> e = jarFile.entries();


       try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {


           while (e.hasMoreElements()) {
               JarEntry je = e.nextElement();
               if (je.isDirectory() || !je.getName().endsWith(".class")) {
                   continue;
               }
  
               try (InputStream classStream = jarFile.getInputStream(je)) {
                   ClassReader classReader = new ClassReader(classStream);
                   int access = classReader.getAccess();
  
                   if ((access & Opcodes.ACC_INTERFACE) != 0) {
                       // Extract the package name, everything before the last '/'
                       String packageName = je.getName().substring(0, je.getName().lastIndexOf('/')).replace('/', '.');
  
                       // Increment the count of interfaces in the package
                       packageInterfaceCount.put(packageName, packageInterfaceCount.getOrDefault(packageName, 0) + 1);
  
                       System.out.println("INTERFACE: " + je.getName());
                       writer.write("INTERFACE: " + je.getName());
                       writer.newLine(); // Adds a new line after each entry
                   }
               } catch (IOException ex) {
                   System.err.println("Could not process class: " + je.getName());
               }
           }
          
           // Print the summary
           System.out.println("\nSUMMARY:");
           writer.write("\nSUMMARY:");
           writer.newLine(); // Adds a new line after each entry
           for (Map.Entry<String, Integer> entry : packageInterfaceCount.entrySet()) {
               System.out.println("Package: " + entry.getKey() + "  " + entry.getValue());
               writer.write("Package: " + entry.getKey() + "  " + entry.getValue());
               writer.newLine(); // Adds a new line after each entry
           }


  
           // for (Map.Entry<String, Integer> entry : packageInterfaceCount.entrySet()) {
           //     String summaryLine = "Package: " + entry.getKey() + "  " + entry.getValue();
              
           //     // Print to console
           //     System.out.println(summaryLine);
              
           //     // Write to the file
           //     writer.write(summaryLine);
           //     writer.newLine(); // Adds a new line after each entry
           // }
      
           System.out.println("Summary written to summary.txt");
      
       } catch (IOException err) {
           System.out.println("An error occurred while writing to the file.");
           err.printStackTrace();
       }       
   }
}



