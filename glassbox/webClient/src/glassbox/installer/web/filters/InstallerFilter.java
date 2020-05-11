package glassbox.installer.web.filters;

import glassbox.installer.GlassboxInstaller;
import glassbox.installer.GlassboxInstallerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class InstallerFilter implements Filter {

	private FilterConfig config;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletContext context = getServletContext();
        GlassboxInstaller installer = GlassboxInstallerFactory.getInstance().getInstaller(context);
        
        request.setAttribute("glassbox.installer", installer);

        chain.doFilter(request, response);
	}

	ServletContext getServletContext() {
		return this.config.getServletContext();
	}

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

}
