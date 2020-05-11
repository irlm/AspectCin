<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
 glassbox.installer.GlassboxInstaller installer = ((glassbox.installer.GlassboxInstaller)request.getAttribute("glassbox.installer"));

 String defaultCustomScript = (String) request.getAttribute("custom.script");
 if (defaultCustomScript == null) defaultCustomScript="";

 String defaultCustomLibDir = (String) request.getAttribute("custom.lib.dir");
 if (defaultCustomLibDir == null) defaultCustomLibDir="";
%>
<html>
<body>
        <form name="configForm" action="Install.form" method="POST">

		<p>Please specify how Tomcat is installed on this system:</p>        
        <input type="radio" name="tomcatType" value="service">Installed as Windows Service</input><br>
		<input type="radio" name="tomcatType" value="script" checked="true">Using Startup Scripts (i.e. catalina.bat)</input><br>
        
        <br>
        <p>If installing Glassbox using startup scripts, the following choices may be specified:</p>
        
		<p>Optionally choose a custom script file to generate a wrapper for:</p>
		<input type="file" name="customScriptFile" value="<%=defaultCustomScript%>" size="30" onChange="customScript.value=customScriptFile.value">
		<input type="hidden" name="customScript" value="<%=defaultCustomScript%>">
		<% if(installer.getDefaultScript() != null) { %>
		<p>The default value is <%=installer.getDefaultScript().getAbsolutePath()%>
		<% } %>
        
		<p>Optionally choose a custom directory to install library JAR files:</p>
		<input type="file" name="customLibDirFile" value="<%=defaultCustomLibDir%>" size="30" onChange="customLibDir.value=customLibDirFile.value">
		<input type="hidden" name="customLibDir" value="<%=defaultCustomLibDir%>">
		<% if(installer.getDefaultLibDirectory() != null) { %>
		<p>The default value is <%=installer.getDefaultLibDirectory().getAbsolutePath()%>
		<% } else { %>
		<p>No default detected. This value must be supplied.</p>
		<% } %>

		<ul class="buttons">
		 <li><a id="install.now" title="Install Now" onClick="document.configForm.submit()">Install Now</a></li>
		</ul>
		</form>

</body>
</html>
