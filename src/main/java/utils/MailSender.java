package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;

public class MailSender {

    public static String sendMailTo(String email){
        // Sender's email ID needs to be mentioned
        String from = "ChaoticDrive <xxx@qq.com>";
        String username = "xxxx@qq.com";
        String password = "password";
        // Get system properties
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.ssl.enable","true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.qq.com");
        properties.put("mail.smtp.port", "25");
        properties.put("mail.user",username);
        properties.put("mail.password",password);

        // Get the default Session object.
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true);
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));


            // Set Subject: header field
            message.setSubject("Your verification code");

            // Now set the actual message
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            String randomNumbers = getRandomNumbers(4);
            String msg = "<h1>"+randomNumbers+"<br>please input in 5 minutes</h1>";
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);
//             Send message
//            Transport.send(message);
            System.out.println("Sent message successfully....");
            return randomNumbers;
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return null;
        }
    }

    public static String getHTMLStringFromFile(String path){
        StringBuilder html = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(path));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String temp;
            while ((temp = bufferedReader.readLine()) != null){
                html.append(temp);
            }
            System.out.println(html);
            return html.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot open html file");
        } catch (IOException e) {
            throw new RuntimeException("IO Exception");
        }
    }

    public static String getRandomNumbers(int length){
        StringBuilder randomNumbers = new StringBuilder();
        for(int i = 0; i < length; i++){
            randomNumbers.append((int)Math.floor(Math.random()*9));
        }
        return randomNumbers.toString();
    }
}
