package glassbox.installer.util;

import glassbox.installer.GlassboxInstallerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

public class WebappResourceLocator {
    private static final Log log = LogFactory.getLog(GlassboxInstallerFactory.class);

    static WebappResourceLocator instance = new WebappResourceLocator();

    public static WebappResourceLocator getInstance() {
    	return instance;
    }
    
    String applicationHome;

    public String getApplicationHome(ServletContext context) {
        String applicationHome = context.getRealPath("/");
        log.debug(" Getting the context path: " + applicationHome);
        if (applicationHome == null) {
            try {
                Enumeration en = getClass().getClassLoader().getResources("glassbox.properties");
                search: while (en.hasMoreElements()) {
                    URL found = (URL) en.nextElement();
                    String foundPath = found.getPath();
                    int pos = foundPath.indexOf('!');
                    if (pos > 0) {
                        foundPath = foundPath.substring(0, pos);
                    }
                    File markerFile = new File(foundPath);
                    File targetDir = null;
                    while ((targetDir = markerFile.getParentFile()) != null) {
                        File[] files = targetDir.listFiles();
                        if (files != null) {
                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().equals("glassbox.war")) {
                                    log.debug("found glassbox war file: " + files[i].getCanonicalPath());
                                    File tmpDir = makeTempDir();
                                    unzipGlassboxWar(files[i], tmpDir);
                                    applicationHome = tmpDir.getCanonicalPath();
                                    break search;
                                } else if (files[i].getName().equals("install")) {
                                    log.debug("Found install parent: " + files[i].getCanonicalPath());
                                    String path = targetDir.getCanonicalPath();
                                    applicationHome = path;
                                    break search;
                                }
                            }
                        }
                        markerFile = targetDir;
                    }
                }
            } catch (Exception e) {
                log.error("Problem unpacking install files", e);
            }
        }
        if (applicationHome == null) {
            // sometimes Tomcat & other containers will unpack only class files,
            // in that case we need to unpack all the resources (yuck)
            log.debug("unpacking glassbox install info from servlet context resources");
            try {
                applicationHome = unpackFromResourcePaths(context);
            } catch (IOException e) {
                log.error("Problem unpacking install files from resource paths", e);
            }
        }
        if (!(applicationHome.endsWith(File.separator))) {
            applicationHome += File.separator;
        }
        
        log.info("ApplicationHome="+applicationHome);
        
        return applicationHome;
    }

    private String unpackFromResourcePaths(ServletContext context) throws IOException {
        File tmpDir = makeTempDir();
        byte[] buffer = new byte[8192];
        unpackFromResourcePaths(context, tmpDir, "/install", buffer);
        try {
            return tmpDir.getCanonicalPath();
        } catch (IOException e) {
            return tmpDir.getAbsolutePath();
        }
    }

    private void unpackFromResourcePaths(ServletContext context, File tmpDir, String base, byte[] buffer)
            throws IOException {
        Set resources = context.getResourcePaths(base);
        for (Iterator it = resources.iterator(); it.hasNext();) {
            String res = (String) it.next();
            File file = new File(tmpDir, res);
            if (res.endsWith("/")) {
                file.mkdirs();
                unpackFromResourcePaths(context, tmpDir, res, buffer);
            } else {
                OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                try {
                    InputStream input = context.getResourceAsStream(res);
                    try {
                        for (;;) {
                            int bytes = input.read(buffer);
                            if (bytes <= 0) {
                                break;
                            }
                            output.write(buffer, 0, bytes);
                        }
                    } finally {
                        input.close();
                    }
                } finally {
                    output.close();
                }
            }
        }
    }

    // TODO JDH testme
    static File makeTempDir() throws IOException {
    	File temp = File.createTempFile("glassbox", ".tmp");
    	temp.delete();
    	temp.mkdir();
    	temp.deleteOnExit();
    	return temp;
    }

    private void unzipGlassboxWar(File src, File dest) {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        Project p = new Project();
        Class expand = null;
        try {
            expand = Class.forName("org.apache.tools.ant.taskdefs.Expand");
        } catch (ClassNotFoundException e) {
            log.error("Could not find the Expand class", e);
        }
        p.addTaskDefinition("unzip", expand);
        Expand e = (Expand) p.createTask("unzip");
        e.setSrc(src);
        e.setDest(dest);
        e.setOverwrite(true);
        e.execute();
    }
}
