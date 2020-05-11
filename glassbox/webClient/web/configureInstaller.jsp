<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% 
 glassbox.installer.GlassboxInstaller installer = (glassbox.installer.GlassboxInstaller)request.getAttribute("glassbox.installer");

 String error = (String) request.getAttribute("error");
 if (error == null) error="";

%>
<html>
<head>
<link href="css/install.css" rel="stylesheet" type="text/css">
<title>Glassbox - Agent Configure Installer</title>
</head>
<body>
<table width="777" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><div id="hdr"><h1><a href="index.html">Glassbox</a></h1>
    </div></td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td background="images/contentbg.gif" style="background-repeat:repeat-x"><table width="777" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td valign="top"><div id="contentleft">		
		<h3 class="product">Automated Glassbox Installation</h3>
		
		<% if(!error.equals("")) { %>
		<br/>
		<b><font id="error.text" color="red"><%=error%></font></b>		
		<br/>
		<% } %>
				
		<p>
        Glassbox detected a <b><%=installer.getTargetSystemLabel()%></b> application server running Java version <b><%=installer.getFullJavaVersion()%></b> 
        from <b><%=installer.getJavaVendor() %></b>.

		<br/>
		<jsp:include page="partial_previous_install.jsp"/>
        <br/>        

		<jsp:include page="<%= installer.getConfigureInstallerPageName()+\".jsp\" %>"/>

		<br/>	
		<br/>
   		Installation performs the following steps:
        <ul>
        <li> Copy the Glassbox Agent jars to your application server's lib directories.</li>
		<li> If Linux/Unix: Create a new java.policy file which allows external RMI connections.</li>
		<li> If 1.4 JVM: Creates an adapter file for AspectJ that allows it to run on Java 1.4.</li>
		<li> Create a test wrapper for you to call which starts many common app servers with Glassbox.</li>
        </ul>
        <br/>
		<br/>		
						
		<p></p>
		</div></td>
		<td valign="top">		
	<br/>
	&nbsp;			      		
<br><br>
		<div id="forumbox"><p><a href="http://www.glassbox.com/forum/forum/listforums" target="_blank">Visit for Questions, problems&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp and solutions.</a></p></div>
		    <div id="contactusbox"><p><a href="http://www.glassbox.com/glassbox/Contact.do">Reach Glassbox directly for questions or support</a></p></div>
		</td>
      </tr>
      <tr>
        <td colspan="2">&nbsp;</td>
        </tr>
    </table>
      <div id="footer">
        <p>Copyright &copy; 2005 Glassbox Corporation. All rights reserved. <a href="http://www.glassbox.com/glassbox/Terms.do">Terms of Use</a> l <a href="http://www.glassbox.com/glassbox/Privacy.do">Privacy Policy</a></p>
    </div></td>
  </tr>
</table>
</body>
</html>
