package com.spring.security.services;

import java.util.UUID;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.spring.security.entity.User;
import com.spring.security.repository.UserRepo;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;




@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	
	
	
	@Override
	public User saveUser(User user, String url) {
		
	String password = 	passwordEncoder.encode(user.getPassword());
	user.setPassword(password);	
	user.setRole("ROLE_ADMIN");
	
	user.setEnable(false);
	user.setVerificationCode(UUID.randomUUID().toString());
	
	User newUser  = userRepo.save(user);
	
	if(newUser != null) {
		sendEmail(newUser, url);
	}
	
		return newUser;
	}
	
	

	@Override
	public void removeSessionMessage() {
       HttpSession session	 = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession();
		
       session.removeAttribute("msg");
	}



	@Override
	public void sendEmail(User user, String url) {
		
	String from = "sahdeenmohd2415675@gmail.com";
	String to = user.getEmail();
	String subject = "Account Verification";
	String content = "Dear [[name]],<br>" + "Please click the link below to verify your rgistration:<br> "
	                  +"<h3><a href=\"[[URL]]\" target=\"_self\"> VERIFY </a></h3>" + "Thank you,<br>"+"Sahdeen";
	
	try {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		
		
		
		
		helper.setFrom(from,"Sahdeen");
		helper.setTo(to);
		helper.setSubject(subject);
		
		content = content.replace("[[name]]", user.getName());
		
		String siteUrl = url + "/verify?code=" + user.getVerificationCode();
		
		content = content.replace("[[URL]]", siteUrl);
		
		helper.setText(content,true);
		
		mailSender.send(message);
		
	} catch(Exception e) {
		e.printStackTrace();
	}
	
	}



	@Override
	public boolean verifyAccount(String verificationCode) {
		
		User user = userRepo.findByVerificationCode(verificationCode);
		
		if(user == null) {
			return false;
		}else {
			user.setEnable(true);
			user.setVerificationCode(null);
			
			userRepo.save(user);
			return true;
		}
		
		
	}
	
	

}
