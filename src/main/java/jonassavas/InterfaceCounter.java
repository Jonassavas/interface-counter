package jonassavas;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InterfaceCounter 
{
    public static void main(String[] args) throws IOException {

        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\classgraph\\target\\classgraph-4.8.154-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\java-faker\\target\\javafaker-1.0.2-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\modelmapper\\core\\target\\modelmapper-3.1.1-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\immutables\\gson\\target\\gson-2.9.3-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\commons-validator\\target\\commons-validator-1.7-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\tablesaw\\json\\target\\tablesaw-json-0.43.1-shaded.jar";
        //String jarPath = "C:\\kthcs\\MEX\\RESULTS\\tika\\tika-core\\target\\tika-core-2.6.0-shaded.jar";
        String jarPath = "C:\\kthcs\\MEX\\RESULTS\\woodstox\\target\\woodstox-core-6.4.0-shaded.jar";

        Map<String, Integer> packageInterfaceCount = new HashMap<>();
        
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> e = jarFile.entries();

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
                }
            } catch (IOException ex) {
                System.err.println("Could not process class: " + je.getName());
            }
        }
        
        // Print the summary
        System.out.println("\nSUMMARY:");
        for (Map.Entry<String, Integer> entry : packageInterfaceCount.entrySet()) {
            System.out.println("Package: " + entry.getKey() + "  " + entry.getValue());
        }
    }
}
