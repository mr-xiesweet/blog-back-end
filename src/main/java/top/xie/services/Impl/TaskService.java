package top.xie.services.Impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.xie.utils.EmailSender;

@Service
public class TaskService {
    @Async
    public void sendEmailVerifyCode(String verifyCode,String emailAddress) throws Exception {
        EmailSender.sendRegisterVerifyCode(verifyCode,emailAddress);
    }
}
