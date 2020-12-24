package top.xie.services;

import top.xie.pojo.SobUser;
import top.xie.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface IUserService {

    ResponseResult initManagerAccount(SobUser sobUser, HttpServletRequest request);

    void createCaptchaKey(HttpServletResponse response, String captchaKey) throws Exception;

    ResponseResult sendEmail(String type,HttpServletRequest request, String emailAddress);

    ResponseResult register(SobUser sobUser, String emailCode, String captchaCode,String captchaKey,HttpServletRequest request);

    ResponseResult doLogin(SobUser sobUser, String captcha, String captchaKey,String from);

    SobUser checkSobUser();

    ResponseResult getUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(String userId, SobUser sobUser);

    ResponseResult deleteUserById(String userId,String state);

    ResponseResult listUsers( int page, int size,String userName,String email);

    ResponseResult updateUserPassword(String verifyCode, SobUser sobUser);

    ResponseResult updateEmail(String email, String verifyCode);

    ResponseResult doLogout();

    ResponseResult getPcLoginQrCodeInfo();

    ResponseResult checkQrCodeLoginState(String loginId);

    ResponseResult updateQrCodeLoginState(String loginId);

    ResponseResult parseToken();

    ResponseResult resetPassword(String userId, String password);
}
