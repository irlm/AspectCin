<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
 glassbox.installer.GlassboxInstaller installer = (glassbox.installer.GlassboxInstaller)request.getAttribute("glassbox.installer");
 String launchCommand = installer.getGlassboxLaunchCommand();
%>

<html>
<body>

        <div id="contentleft">

		<h3 class="product">Automated Install Complete</h3>
	    <b>Glassbox Agent Install is Complete</b>. 
        
   		<br/>
		<h3 class="product">Running Glassbox Troubleshooter on your Application Server: </h3>
        <ul>
        <li> Stop your <%=installer.getTargetSystemLabel()%> application server</li>
<% if(launchCommand != null) {%>
        <li> Run <b><%=launchCommand%></b> to restart your <%=installer.getTargetSystemLabel()%> server.  This generated file wraps 
        your normal startup script to set extra environment variables which start Glassbox.  You can continue to call this script
        whenever you start your application server OR use it only to verify the Glassbox Agent is configured correctly and immediately
        set the environment variables manually in your regular startup script.  Without setting variables manually or calling the generated
        script, Glassbox will not start or monitor your application. </li>
<% } else { %>
        <li> You must change the environment variables for your <%=installer.getTargetSystemLabel()%> server on startup.</li>
        <li>Insert the following environment variables for <%=installer.getTargetSystemLabel()%> 
        startup (note text from a single line may be wrapped):</li>
        <br/>
        <%=installer.getFormattedEnvVars()%>
<% } %>
        <li> Click <a id="verify.install" href="VerifyInstall.form">Glassbox Troubleshooter Verify</a> after your application server starts completely to 
        verify Glassbox is up and running.  Follow any further instructions on that page</li> 
        </ul>
        <br/>
        Thank you for installing Glassbox.
        <br/>
		</div>

</body>
</html>
