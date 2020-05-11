/*
 * Copyright (c) Glassbox. All rights reserved.                                       *
 * http://glassbox.com                                                                *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 */
package glassbox.installer;

import java.io.*;
import java.util.jar.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor;

public class CreateJavaAdapter {

    public static final String DEFAULT_FILE_PATH = "java14Adapter.jar";
    
    public static void main(String arg[]) {
        try {
            String path = arg.length>0 ? arg[0] : DEFAULT_FILE_PATH;
            new CreateJavaAdapter().run(path);
        } catch (Exception e) {
            failure(e.getMessage());
        }
    }
    
    void run(String path) throws Exception {
        String javaVersion = System.getProperty("java.version", "0.0");
        String javaHome = System.getProperty("java.home", ".");
    
        double version = Double.parseDouble(javaVersion.substring(0, 3));
        
        if (version >= 1.5) {
            throw new Exception("This script is only intended for use with a Java 1.4.x or earlier VM. For Java 1.5 and later, load-time weaving support is builtin.");
        }    
        createTarget(path);
    }

    /**
     * Converts an input stream to a byte[]
     */
    public static byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int b = is.read(); b != -1; b = is.read()) {
            os.write(b);
        }
        return os.toByteArray();
    }

    /**
     * Gets the bytecode of the modified java.lang.ClassLoader using given ClassLoaderPreProcessor class name
     */
    static byte[] getPatchedClassLoader(String preProcessorName) {
        byte[] abyte = null;
        InputStream is = null;
        try {
            is = ClassLoader.getSystemClassLoader().getParent().getResourceAsStream("java/lang/ClassLoader.class");
            abyte = inputStreamToByteArray(is);
        } catch (IOException e) {
            throw new Error("failed to read java.lang.ClassLoader: " + e.toString());
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
        if (preProcessorName != null) {
            try {
                ClassLoaderPreProcessor clpi = (ClassLoaderPreProcessor) Class.forName(preProcessorName).newInstance();
                abyte = clpi.preProcess(abyte);
            } catch (Exception e) {
                System.err.println("failed to instrument java.lang.ClassLoader: preprocessor not found");
                e.printStackTrace();
            }
        }
        return abyte;
    }

    /**
     * Dumps the modified java.lang.ClassLoader in destJar
     * The aspectcwerkz.classloader.clclasspreprocessor is used
     * if specified, else defaults to AspectWerkz layer 1
     *
     * @param destJar
     * @throws Exception
     */
    public void createTarget(String destJar) throws Exception {
        File dest = new File(destJar);
        if (dest.exists() && !dest.canWrite()) {
            throw new Exception(destJar + " exists and is not writable");
        }

        // patch the java.lang.ClassLoader
        byte[] patched = getPatchedClassLoader(org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl.class.getName());

        // pack the jar file
        Manifest mf = new Manifest();
        Attributes at = mf.getMainAttributes();
        at.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        at.putValue("Created-By", "AspectWerkz (c) Plug [java " + System.getProperty("java.version") + ']');
        ZipEntry entry = new ZipEntry("java/lang/ClassLoader.class");
        entry.setSize(patched.length);
        CRC32 crc = new CRC32();
        crc.update(patched);
        entry.setCrc(crc.getValue());
        JarOutputStream jar = new JarOutputStream(new FileOutputStream(dest), mf);
        jar.putNextEntry(entry);
        jar.write(patched);
        jar.closeEntry();
        jar.close();
        
        System.out.println("==\nSUCCESS: Created file "+dest.getCanonicalPath());
    }
    
    private static void failure(String str) {
        System.err.println(str+"\n==\nFAILURE: Script could not complete,  please correct the problem and try again or visit the Glassbox forums at http://www.glassbox.com/forum/forum/listforums");
        System.exit(1);
    }
        
}
