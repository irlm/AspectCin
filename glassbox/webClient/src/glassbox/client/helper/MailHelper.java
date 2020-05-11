/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import glassbox.client.persistence.jdbc.MailDAO;
import glassbox.remoting.GlassboxRemoteMessage;
import glassbox.remoting.GlassboxRemoteService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MailHelper extends BaseHelper {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(MailHelper.class);    
	
	protected GlassboxRemoteService remoteService = null;
	protected JavaMailSender mailSender = null;
	protected MailDAO mailDAO = null;
	
	public JavaMailSender getMailSender() {
		return mailSender;
	}
	
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public GlassboxRemoteService getRemoteService() {
		return remoteService;
	}
	
	public void setRemoteService(GlassboxRemoteService remoteService) {
		this.remoteService = remoteService;
	}
	
	public boolean isMailConfigured() {
		return mailDAO.isMailConfigured();
	}
	
	public void configureMail(HttpSession session, String smtpServer, String userName, String password) {
		
	}
	
	public void sendMessageViaGlassbox(HttpSession session, String toString, String subject, final String message) {
		final String[] toPeople = getToPeople(toString);			
		GlassboxRemoteMessage remoteMessage = new GlassboxRemoteMessage(toPeople, subject, message);			
		remoteService.sendGlassboxMessage(remoteMessage);			
	}
	
	/*
	 * Sends a message based on the settings the user has specified.
	 * Given an config based on user input, use that.
	 * 
	 */
	public void sendMessage(HttpSession session, String toString, String subject, final String message) {
		final String[] toPeople = getToPeople(toString);
		
		if(mailDAO.isMailConfigured()) {			
			//Send it the spring template way....
		    MimeMessagePreparator preparator = new MimeMessagePreparator() {
		            public void prepare(MimeMessage mimeMessage) throws MessagingException {
		            	Address[] addresses = new Address[toPeople.length];
		            	for(int i = 0; i < toPeople.length; i++) {
		            		addresses[i] = new InternetAddress(toPeople[i]); 
		            	}
		            	
		            	mimeMessage.setRecipients(Message.RecipientType.TO, addresses);		                
		            	mimeMessage.setFrom(new InternetAddress("mail@mycompany.com"));
		                mimeMessage.setText(message);
		            }
		        };
		        
		        try {
		            mailSender.send(preparator);
		        } catch (MailException ex) {
		          log.equals(ex.getMessage());            
		        }		        
		} 
	}
	
	
	protected String[] getToPeople(String to) {
		String[] toPeople = to.split(",");
		
		//TODO: Validate....!
		
		return toPeople;
	}

	public MailDAO getMailDAO() {
		return mailDAO;
	}

	public void setMailDAO(MailDAO mailDAO) {
		this.mailDAO = mailDAO;
	}	
}
