<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
 glassbox.installer.GlassboxInstaller installer = ((glassbox.installer.GlassboxInstaller)request.getAttribute("glassbox.installer"));
%>
<html>
<body>
<% if(installer.isPartiallyInstalled()) {%>
    <b>We detected at least part of the glassbox application have previously been installed on your server</b>
   	<br/>
   	<br/>
   	To verify your Glassbox Agent has been properly installed, bounce your Application Server and
   	wait for it to start completely.  Then come back to this page and click 
   	<a href="VerifyInstall.form">Glassbox Troubleshooter Verify</a>.
   	<br/>
   	<br/>
   	To re-install, click 'Install Now' below.  
   	<br/>
   	<br/>
<% } else if(installer.isInstalled()) {%>
	<b>A Glassbox instance has already been installed! The version currently installed is:<%=installer.getVersion()%></b>
	<br/>
   	<br/>
	To re-install, click 'Install Now' below. To see your existing install log, 
	click <a href="<%=installer.getInstallLog().getName()%>"> here.</a>
	<br/>
   	<br/>
   	To verify your Glassbox Agent has started, bounce your Application Server and
   	wait for it to start completely.  Then come back to this page and click 
   	<a href="VerifyInstall.form">Glassbox Troubleshooter Verify</a>. 
   	<br/>
   	<br/>
<% } else {%>

	Glassbox Agent Install is automated, click the button below to begin.
<% } %>
</body>
</html>
