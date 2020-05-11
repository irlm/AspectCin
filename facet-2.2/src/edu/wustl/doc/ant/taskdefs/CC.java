
package edu.wustl.doc.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;

import edu.wustl.doc.ant.taskdefs.cc.*;

import java.io.File;

/**
 * Task to compile Java source files. This task can take the following
 * arguments:
 * <ul>
 * <li>sourcedir
 * <li>destdir
 * <li>classpath
 * <li>bootclasspath
 * <li>extdirs
 * <li>optimize
 * <li>debug
 * <li>encoding
 * <li>target
 * <li>depend
 * <li>vebose
 * <li>failonerror
 * <li>includeantruntime
 * <li>includejavaruntime
 * </ul>
 * Of these arguments, the <b>sourcedir</b> and <b>destdir</b> are required.
 * <p>
 * When this task executes, it will recursively scan the sourcedir and
 * destdir looking for Java source files to compile. This task makes its
 * compile decision based on timestamp.
 *
 * @author James Davidson <a href="mailto:duncan@x180.com">duncan@x180.com</a>
 * @author Robin Green <a href="mailto:greenrd@hotmail.com">greenrd@hotmail.com</a>
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @author <a href="mailto:jayglanville@home.com">J D Glanville</a>
 */

public class CC extends MatchingTask {

    private static final String FAIL_MSG = 
        "Compile failed, messages should have been provided.";

    private Path includePath; //use
    private Commandline cmdl = new Commandline();
    private DefineList defines = new DefineList();
    private String inputExtension = "c";
    private String warningLevel;

  private Path src;
  private File destDir;
    private boolean debug = false;
    private boolean optimize = false;
    private boolean depend = false;
    private boolean verbose = false;
    private String target;
    private Path extdirs;
    private boolean includeAntRuntime = true;
    private boolean includeJavaRuntime = false;
    private boolean fork = false;
    private boolean nowarn = false;

    protected boolean failOnError = true;
    protected File[] compileList = new File[0];

    /**
     * Create a nested <src ...> element for multiple source path
     * support.
     *
     * @return a nexted src element.
     */
    public Path createSrc() {
        if (src == null) {
            src = new Path(project);
        }
        return src.createPath();
    }

    /**
     * Set the source dirs to find the source Java files.
     */
    public void setSrcdir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }

    /** Gets the source dirs to find the source java files. */
    public Path getSrcdir() {
        return src;
    }

    /**
     * Set the destination directory into which the Java source
     * files should be compiled.
     */
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Gets the destination directory into which the java source files
     * should be compiled.
     */
    public File getDestdir() {
        return destDir;
    }

  /**
   * Add a nested arg element - a command line argument.
   */
  public Commandline.Argument createArg() {
    return cmdl.createArgument();
  }

  public Commandline getArg() {
    return cmdl;
  }

  /**
   * Add a nested arg element - a command line argument.
   */
  public DefineList.Argument createDefine() {
    return defines.createArgument();
  }

  public DefineList getDefine() {
    return defines;
  }

  /**
   * Set the includepath to be used for this compilation.
   */
  public void setIncludepath(Path includes) {
    if (includePath == null) {
      includePath = includes;
    } else {
      includePath.append(includes);
    }
  }
  
  /** Gets the includepath to be used for this compilation. */
  public Path getIncludepath() {
    return includePath;
  }
  
  /**
   * Maybe creates a nested includepath element.
   */
  public Path createIncludepath() {
    if (includePath == null) {
      includePath = new Path(project);
    }
    return includePath.createPath();
  }
  
  /**
   * Adds a reference to a includepath defined elsewhere.
   */
  public void setIncludepathRef(Reference r) {
    createIncludepath().setRefid(r);
  }

    /**
     * Sets the extension directories that will be used during the
     * compilation.
     */
    public void setExtdirs(Path extdirs) {
        if (this.extdirs == null) {
            this.extdirs = extdirs;
        } else {
            this.extdirs.append(extdirs);
        }
    }

    /**
     * Gets the extension directories that will be used during the
     * compilation.
     */
    public Path getExtdirs() {
        return extdirs;
    }

    /**
     * Maybe creates a nested classpath element.
     */
    public Path createExtdirs() {
        if (extdirs == null) {
            extdirs = new Path(project);
        }
        return extdirs.createPath();
    }

    /**
     * Throw a BuildException if compilation fails
     */
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }

    /**
     * Proceed if compilation fails
     */
    public void setProceed(boolean proceed) {
        failOnError = !proceed;
    }

    /**
     * Gets the failonerror flag.
     */
    public boolean getFailonerror() {
        return failOnError;
    }

    public void setInputExtension(String ext) {
        inputExtension = ext;
    }

    public String getInputExtension() {
        return inputExtension;
    }

    public void setWarningLevel(String level) {
        warningLevel = level;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    /**
     * Set the debug flag.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /** Gets the debug flag. */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Set the optimize flag.
     */
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /** Gets the optimize flag. */
    public boolean getOptimize() {
        return optimize;
    }

    /**
     * Set the depend flag.
     */
    public void setDepend(boolean depend) {
        this.depend = depend;
    }

    /** Gets the depend flag. */
    public boolean getDepend() {
        return depend;
    }

    /**
     * Set the verbose flag.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** Gets the verbose flag. */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Sets the target VM that the classes will be compiled for. Valid
     * strings are "1.1", "1.2", and "1.3".
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /** Gets the target VM that the classes will be compiled for. */
    public String getTarget() {
        return target;
    }

    /**
     * Include ant's own classpath in this task's classpath?
     */
    public void setIncludeantruntime( boolean include ) {
        includeAntRuntime = include;
    }

    /**
     * Gets whether or not the ant classpath is to be included in the
     * task's classpath.
     */
    public boolean getIncludeantruntime() {
        return includeAntRuntime;
    }

    /**
     * Sets whether or not to include the java runtime libraries to this
     * task's classpath.
     */
    public void setIncludejavaruntime( boolean include ) {
        includeJavaRuntime = include;
    }

    /**
     * Gets whether or not the java runtime should be included in this
     * task's classpath.
     */
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
    }

    /**
     * Sets whether to fork the javac compiler.
     */
    public void setFork(boolean fork)
    {
        this.fork = fork;
    }


    /**
     * Sets whether the -nowarn option should be used.
     */
    public void setNowarn(boolean flag) {
        this.nowarn = flag;
    }

    /**
     * Should the -nowarn option be used.
     */
    public boolean getNowarn() {
        return nowarn;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException {

      // first off, make sure that we've got a srcdir
      if (src == null) {
        throw new BuildException("srcdir attribute must be set!", location);
      }
      String [] list = src.list();
      if (list.length == 0) {
            throw new BuildException("srcdir attribute must be set!", location);
      }
      
      if (destDir != null && !destDir.isDirectory()) {
        throw new BuildException("destination directory \"" + destDir + "\" does not exist or is not a directory", location);
      }

      // Get information on the compiler.
      String compiler = project.getProperty("build.cc.compiler");
      if (compiler == null) {
        compiler = "msvc";
      }
      CompilerAdapter adapter = 
          CompilerAdapterFactory.getCompiler(compiler, this );
      
      // scan source directories and dest directory to build up
      // compile lists
      resetFileLists();
      for (int i=0; i<list.length; i++) {
        File srcDir = (File)project.resolveFile(list[i]);
        if (!srcDir.exists()) {
          throw new BuildException("srcdir \"" + srcDir.getPath() + "\" does not exist!", location);
        }
        
        DirectoryScanner ds = this.getDirectoryScanner(srcDir);
        
        String[] files = ds.getIncludedFiles();
        
        scanDir(srcDir, 
                destDir != null ? destDir : srcDir, 
                files,
                adapter.getOutputExtension());
      }
            
      // compile the source files
      if (compileList.length > 0) {
        
        log("Compiling " + compileList.length +
            " source file"
            + (compileList.length == 1 ? "" : "s")
                + (destDir != null ? " to " + destDir : ""));
        
        // now we need to populate the compiler adapter
        adapter.setCC(this);
        
        // finally, lets execute the compiler!!
        if (!adapter.execute()) {
          if (failOnError) {
            throw new BuildException(FAIL_MSG, location);
          }
          else {
                    log(FAIL_MSG, Project.MSG_ERR);
          }
        }
      }
    }
  
  /**
   * Clear the list of files to be compiled and copied..
   */
  protected void resetFileLists() {
    compileList = new File[0];
  }
  
  /**
   * Scans the directory looking for source files to be compiled.
   * The results are returned in the class variable compileList
   */
  protected void scanDir(File srcDir, 
                         File destDir, 
                         String files[],
                         String outputExtension) {
    GlobPatternMapper m = new GlobPatternMapper();
    m.setFrom("*." + this.getInputExtension());
    m.setTo("*." + outputExtension);
    SourceFileScanner sfs = new SourceFileScanner(this);
    File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

    if (newFiles.length > 0) {
      File[] newCompileList = new File[compileList.length +
                                      newFiles.length];
      System.arraycopy(compileList, 0, newCompileList, 0,
                       compileList.length);
      System.arraycopy(newFiles, 0, newCompileList,
                       compileList.length, newFiles.length);
      compileList = newCompileList;
    }
  }
  
  /** Gets the list of files to be compiled. */
  public File[] getFileList() {
    return compileList;
  }
}


