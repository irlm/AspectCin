package glassbox.installer.perContainer;

import java.io.File;

import javax.servlet.ServletContext;

import glassbox.installer.BaseGlassboxInstaller;

/**
 * WARNING: work in progress. Not complete. Do not use. Delete this comment when finished!!
 *
 */
public class ResinInstaller extends BaseGlassboxInstaller {

    public File getContainerHome() {
        throw new IllegalStateException("resin support not migrated to new installer yet - should use generic installer");
    }

    public String getTargetSystemLabel() {
        return "resin";
    }

    public boolean matchesContext(ServletContext context) {
        return checkClassloadersNameMatchExactly("com.caucho.loader.EnvironmentClassLoader");
    }

}
