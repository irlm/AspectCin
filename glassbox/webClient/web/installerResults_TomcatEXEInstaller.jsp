<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
 glassbox.installer.perContainer.TomcatGlassboxInstaller installer = (glassbox.installer.perContainer.TomcatGlassboxInstaller)request.getAttribute("glassbox.installer");
%>

<html>
<body>

    <div id="contentleft">

	<h3 class="product">Automated Install Complete : Tomcat.exe detected </h3>
	<p>We detected you are running the Tomcat Windows Executable version. You must perform the following steps to complete the installation:
	<ul>
		<li> If the Tomcat monitor is not enabled, Click <i>Start -> All Programs -> Apache Tomcat -> Monitor Tomcat</i></li>
		<li> Right-click on the <i>Tomcat</i> icon in the Program Tray</li>
		<li> Click the <i>Configure...</i> menu item</li>
		<li> Click the <i>Java</i> tab</li>
		<li> In the <i>Java Options</i> text box, add the following lines:<br/>
			<br/>
	        <%=installer.getFormattedExeVars()%>
			<br/>
		</li>
		<br/>
		<li> In the <i>Maximum Memory Pool</i> box, enter 509</li>
		<li> Click OK</li>
	</ul>

    <li> Click <a id="verify.install" href="VerifyInstall.form">Glassbox Troubleshooter Verify</a> after your application server starts completely to 
    verify Glassbox is up and running.  Follow any further instructions on that page</li> 
    </ul>
    <br/>
    Thank you for installing Glassbox.
    <br/>
	</div>

</body>
</html>
