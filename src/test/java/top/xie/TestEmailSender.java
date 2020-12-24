package top.xie;

import top.xie.utils.EmailSender;

import javax.mail.MessagingException;

public class TestEmailSender {
    public static void main(String[] args) throws MessagingException {
        EmailSender.subject("测试邮件发送")
                .from("阳光沙滩")
                .text("这是发送内容：测试邮件")
                .to("xxboom_xie@163.com").send();
    }
}
