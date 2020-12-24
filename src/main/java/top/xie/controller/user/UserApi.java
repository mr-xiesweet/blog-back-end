package top.xie.controller.user;


import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.pojo.SobUser;
import top.xie.response.ResponseResult;
import top.xie.services.IUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private IUserService userService;

    /**
     * 帐户初始化（没有注册，首次使用的时候设置账号和密码以及邮箱）
     * 初始化管理员账号
     * @param sobUser
     * @return
     */

    @PostMapping("/admin_account")
    public ResponseResult initManagerAccount(@RequestBody SobUser sobUser, HttpServletRequest request){

        return userService.initManagerAccount(sobUser,request);
    }

    /**
     * 注册_join_in
     * @param sobUser
     * @return
     */

    @PostMapping("/join_in")
    public ResponseResult register(@RequestBody SobUser sobUser,
                                   @RequestParam("email_code")String emailCode,
                                   @RequestParam("captcha_code")String captchaCode,
                                   @RequestParam("captcha_key")String captchaKey,
                                   HttpServletRequest request){

        return  userService.register(sobUser,emailCode,captchaCode,captchaKey,request);
    }

    /**
     * 登录sign_in
     *
     * 需要提交的数据
     * 1. 用户账号-用户名/邮箱 -->唯一处理
     * 2. 密码
     * 3. 图灵验证码
     * 4. 图灵验证码的key
     * @param captchaKey 图灵验证码的key
     * @param captcha 图灵验证码
     * @param sobUser 用户Bean类 封装这账户和密码
     * @return
     */
    @PostMapping("/login/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha_key") String captchaKey,
                                @PathVariable("captcha") String captcha,
                                @RequestBody SobUser sobUser,
                                @RequestParam(value = "from",required = false)String from ){
        return userService.doLogin(sobUser,captcha,captchaKey,from);
    }

    /**
     * 获取图灵验证码
     * 有效时间5分钟
     * @return
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response,@RequestParam("captcha_key")String captchaKey) {
        try {
            userService.createCaptchaKey(response,captchaKey);
        }catch (Exception e){
            log.error(e.toString());
        }
    }

    /**
     * 发送邮件email
     *
     * 使用场景：注册、找回密码、修改邮箱（输出新的邮箱）
     * 注册： 如果已经注册了，就提示说，该邮箱已经注册过
     * 找回密码：如果没有注册过，提示该邮箱没有注册过
     * 修改邮箱（新的邮箱地址）：如果已经注册了，提示该邮箱已经注册了
     * @param request
     * @param type
     * @param emailAddress
     * @return
     */

    @GetMapping("/verify_code")
    public ResponseResult sendVerifyCode(HttpServletRequest request,
                                         @RequestParam("type") String type,
                                         @RequestParam("email") String emailAddress){
        log.info("email==>" + emailAddress);

        return userService.sendEmail(type,request,emailAddress);
    }


    /**
     * 修改密码password
     * 修改密码
     * 普通做法：通过旧密码对比来更新密码
     * <p>
     * 即可以找回密码，也可以修改密码
     * 发送验证码到邮箱/手机---> 判断验证码是否真确来判断
     * 对应邮箱/手机号码所注册的账号是否属于你。
     * <p>
     * 步骤：
     * 1、用户填写邮箱
     * 2、用户获取验证码type=forget
     * 3、填写验证码
     * 4、填写新的密码
     * 5、提交数据
     * <p>
     * 数据包括：
     * <p>
     * 1、邮箱和新密码
     * 2、验证码
     * <p>
     * 如果验证码正确-->所用邮箱注册的账号就是你的，可以修改密码
     *
     * @return
     */

    @PutMapping("/password/{verify_code}")
    public ResponseResult updatePassword(@PathVariable("verify_code") String verifyCode,@RequestBody SobUser sobUser){
        return userService.updateUserPassword(verifyCode,sobUser);
    }

    /**
     * 获取作者信息user_info
     * @param userId
     * @return
     */

    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId){
        return userService.getUserInfo(userId);
    }

    /**
     * 修改用户信息 user_info
     * 允许用户修改的内容，
     * 1. 头像
     * 4. 签名
     * 2. 用户名 （唯一的）
     * 3. 密码 （单独修改）
     * 5. Email （单独修改）（唯一的）
     * @param sobUser
     * @return
     */

    @PutMapping("/user_info/{userId}")
    public ResponseResult updateUserInfo(@PathVariable("userId")String userId,
                                         @RequestBody SobUser sobUser){
        return userService.updateUserInfo(userId,sobUser);
    }

    /**
     * 获取用户列表
     * 权限：需要管理员权限
     *
     * 获取的列表不包含密码
     *
     * 所以，同学们要学会自定义查询字段内容
     *
     * API接口
     * @param page
     * @param size
     * @return
     */

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list")
    public ResponseResult listUsers(@RequestParam("page") int page,
                                    @RequestParam("size") int size,
                                    @RequestParam(value = "userName",required = false)String userName,
                                    @RequestParam(value = "email",required = false)String email
                                    ){

        return userService.listUsers(page,size,userName,email);
    }

    /**
     * 删除用户
     * 需要管理员权限
     * @param userId
     * @return
     */

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{userId}/{state}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId,
                                     @PathVariable("state") String state){
        //判断当前的操作用户是谁
        //根据用户角色判断是否可以删除

        return userService.deleteUserById(userId,state);
    }



    /**
     * 检查用户该Email 是否注册
     * @param email 邮箱地址
     * @return SUCCESS ==> 表示已经注册，FALSE ==> 表示未注册
     */
    @ApiResponses({
            @ApiResponse(code = 20000,message = "表示当前邮箱已经注册了"),
            @ApiResponse(code = 40000,message = "表示当前邮箱未注册")
    })
    @GetMapping("/email")
    public ResponseResult checkEmail(@RequestParam("email") String email){
        return userService.checkEmail(email);
    }

    /**
     * 检查用户该用户名 是否注册
     * @param userName 用户名
     * @return SUCCESS ==> 表示已经注册，FALSE ==> 表示未注册
     */
    @ApiResponses({
            @ApiResponse(code = 20000,message = "表示该用户名已经注册了"),
            @ApiResponse(code = 40000,message = "表示用户名未注册")
    })
    @GetMapping("/user_name")
    public ResponseResult checkUserName(@RequestParam("userName") String userName){
        return userService.checkUserName(userName);
    }

    /**
     * 1、必须已经登录了
     * 2、新的邮箱没有注册过
     * <p>
     * 用户的步骤：
     * 1、已经登录
     * 2、输入新的邮箱地址
     * 3、获取验证码 type=update
     * 4、输入验证码
     * 5、提交数据
     * <p>
     * 需要提交的数据
     * 1、新的邮箱地址
     * 2、验证码
     * 3、其他信息我们可以token里获取
     *
     * @return
     */
    @PutMapping("/email")
    public ResponseResult updateEmail(@RequestParam("email") String email,
                                      @RequestParam("verify_code") String verifyCode){
        return userService.updateEmail(email,verifyCode);
    }

    /**
     * 退出登录
     * <p>
     * 拿到token_key
     * -> 删除redis里对应的token
     * -> 删除mysql里对应的refreshToken
     * -> 删除cookie里的token_key
     *
     * @return
     */
    @GetMapping("/logout")
    public ResponseResult logout() {
        return userService.doLogout();
    }

    /***
     * 获取二维码：
     * 二维码的图片路径
     * 二维码的内容字符串
     * 要防止太频繁的请求
     * //todo:要防止太频繁
     * @return
     */

    @GetMapping("/pc-login-qr-code")
    public ResponseResult getPcLoginQrCode() {
        return userService.getPcLoginQrCodeInfo();
    }

    /**
     * 检查二维码登录状态
     * @return
     */

    @GetMapping("/qr-code-state/{loginId}")
    public ResponseResult checkQrCodeLoginState(@PathVariable("loginId")String loginId){
        return userService.checkQrCodeLoginState(loginId);
    }

    @PutMapping("/qr-code-state/{loginId}")
    public ResponseResult updateQrCodeLoginState(@PathVariable("loginId")String loginId){
        return userService.updateQrCodeLoginState(loginId);
    }


    @GetMapping("/check-token")
    public ResponseResult parseToken(){
        return userService.parseToken();
    }

    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/reset-passoword/{userId}")
    public ResponseResult resetPassword(@PathVariable("userId") String userId,
                                        @RequestParam("password")String password){
        return userService.resetPassword(userId,password);
    }

}
