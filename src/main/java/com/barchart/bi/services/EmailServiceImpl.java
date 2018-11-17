package com.barchart.bi.services;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

	
	@Autowired
	public Environment env;
	
	@Autowired
    public JavaMailSender emailSender;
	
	public void sendmail(String text) throws AddressException, MessagingException, IOException {
		
		
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(env.getProperty("mail.recepient"));
		helper.setText(text, true);
		helper.setSubject(env.getProperty("mail.subject"));
		
        emailSender.send(message);
	}
}
