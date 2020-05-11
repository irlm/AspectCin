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
        <form name="configForm" action="Install.form" method="post">
        
        <p>Proceed with installation:</p>        
        
		<ul class="buttons">
		 <li><a id="install.now" title="Install Now" onClick="document.configForm.submit()">Install Now</a></li>
		</ul>	
		</form>

</body>
</html>
