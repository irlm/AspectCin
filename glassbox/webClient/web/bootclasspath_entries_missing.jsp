<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link href="css/install.css" rel="stylesheet" type="text/css">
<title>Glassbox - Agent Install</title>
</head>
<body>
<table width="777" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><div id="hdr"><h1><a href="InitializeInstall.form">Glassbox</a></h1>
    </div></td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td background="images/contentbg.gif" style="background-repeat:repeat-x"><table width="777" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td valign="top"><div id="contentleft">
	    <h3>Application Server Boot Classpath Error</h3>
	    <br/>
	    <b><%=request.getAttribute("ERROR")%></b><br/>
	    <br/>
	    Troubleshooting your installation....
	    <ul>
	    <li>Did you setup Glassbox on your Application Server? (Tomcat, WebSphere, etc)</li>
   	    <li>Did you restart your application server?</li>
   	    <li>Check the script used to launch your application server to verify the required boot classpath entries are present and set to valid jar files</li>
 	 	<li>Check on the Glassbox Forums</li>    
 	 	<li>Contact Glassbox Support</li>
   	    </ul>
    	<br/>	
   	    <br/>	
		Thank you for running Glassbox.  Please visit our <a href="http://www.glassbox.com/forum/forum/listforums" target="_blank">forums</a> for Questions, problems or more information.
						
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