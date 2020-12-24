package top.xie.services.Impl;

import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.xie.dao.RefreshTokenDao;
import top.xie.dao.SettingDao;
import top.xie.dao.UserDao;
import top.xie.dao.UserNoPasswordDao;
import top.xie.pojo.RefreshToken;
import top.xie.pojo.Setting;
import top.xie.pojo.SobUser;
import top.xie.pojo.SobUserNoPassword;
import top.xie.response.ResponseResult;
import top.xie.response.ResponseState;
import top.xie.services.IUserService;
import top.xie.utils.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@Transactional
public class UserServiceImpl extends BaseService implements IUserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SettingDao settingsDao;

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Autowired
    private Gson gson;

    @Override
    public ResponseResult initManagerAccount(SobUser sobUser, HttpServletRequest request) {

        //检查是否有初始化
        Setting managerAccountState = settingsDao.findOneByKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        if (managerAccountState != null) {

            return ResponseResult.FAILED("管理员账号已经初始化了");
        }

        //检查数据
        if (TextUtils.isEmpty(sobUser.getUserName())) {
            return ResponseResult.FAILED("用户名不能为空");
        }
        if (TextUtils.isEmpty(sobUser.getPassword())) {
            return ResponseResult.FAILED("密码不能为空");
        }
        if (TextUtils.isEmpty(sobUser.getEmail())) {
            return ResponseResult.FAILED("邮箱不能为空");
        }

        //补充数据
        sobUser.setId(String.valueOf(idWorker.nextId()));
        sobUser.setRoles(Constants.User.ROLE_ADMIN);
        sobUser.setAvatar(Constants.User.DEFAULT_AVATAR);
        sobUser.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr = request.getRemoteAddr();
        sobUser.setLoginIp(remoteAddr);
        sobUser.setRegIp(remoteAddr);
        sobUser.setCreateTime(new Date());
        sobUser.setUpdateTime(new Date());
        //对密码进行加密
        //原密码
        String password = sobUser.getPassword();
        //加密
        String encode = bCryptPasswordEncoder.encode(password);
        sobUser.setPassword(encode);
        //保存到数据库里
        userDao.save(sobUser);
        //更新已添加的标记
        Setting setting = new Setting();
        setting.setId(idWorker.nextId() + "");
        setting.setCreateTime(new Date());
        setting.setUpdateTime(new Date());
        setting.setKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        setting.setValue("1");
        settingsDao.save(setting);
        return ResponseResult.SUCCESS("初始化成功");
    }


    public static final int[] captcha_font_types = {Captcha.FONT_1,
            Captcha.FONT_2,
            Captcha.FONT_3,
            Captcha.FONT_4,
            Captcha.FONT_5,
            Captcha.FONT_6,
            Captcha.FONT_7,
            Captcha.FONT_8,
            Captcha.FONT_9,
            Captcha.FONT_10};

    @Autowired
    private Random random;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void createCaptchaKey(HttpServletResponse response, String captchaKey) throws Exception {
        if (TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13) {
            return;
        }
        long key;
        try {
            key = Long.parseLong(captchaKey);
        } catch (Exception e) {
            return;
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        Captcha targetCaptcha = null;
        int catpchaType = random.nextInt(3);

        int width = 120;
        int height = 40;
        if (catpchaType == 0) {
            // 三个参数分别为宽、高、位数
            targetCaptcha = new SpecCaptcha(width, height, 5);
        } else if (catpchaType == 1) {
            //gif类型
            targetCaptcha = new GifCaptcha(width, height);
        } else {
            //算术类型
            targetCaptcha = new ArithmeticCaptcha(width, height);
            targetCaptcha.setLen(2);//几位数运算
        }

        int index = random.nextInt(captcha_font_types.length);
        log.info("captcha_font_types index ==>" + index);
        targetCaptcha.setFont(captcha_font_types[index]);
        targetCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String content = targetCaptcha.text().toLowerCase();
        log.info("catpcha content ==>" + content);
        //保存到redis
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT + key + "", content, 10 * 60);
        targetCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private TaskService taskService;

    /**
     * 使用场景：注册、找回密码、修改邮箱（输出新的邮箱）
     * 注册（register）： 如果已经注册了，就提示说，该邮箱已经注册过
     * 找回密码（forget）：如果没有注册过，提示该邮箱没有注册过
     * 修改邮箱（update）（新的邮箱地址）：如果已经注册了，提示该邮箱已经注册了
     *
     * @param type
     * @param request
     * @param emailAddress
     * @return
     */
    @Override
    public ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress) {
        if (emailAddress == null) {
            return ResponseResult.FAILED("邮箱地址不可以为空");
        }
        //根据类型，查询邮箱是否存在
        if ("register".equals(type) || "update".equals(type)) {
            SobUser userByEmail = userDao.findOneByEmail(emailAddress);
            if (userByEmail != null) {
                return ResponseResult.FAILED("该邮箱已经注册了！");
            }
        } else if ("forget".equals(type)) {
            SobUser userByEmail = userDao.findOneByEmail(emailAddress);
            if (userByEmail == null) {
                return ResponseResult.SUCCESS("该邮箱未注册.");
            }
        }
        //1. 防止暴力发送，就是不断地发送：同一个邮箱，间隔要超过30秒发送一次，1小时内同一个IP只能最多发送10次
        String remoteAddr = request.getRemoteAddr();

        if (remoteAddr != null) {
            remoteAddr = remoteAddr.replaceAll(":", "_");
        }
        log.info("remoteAddr ==> " + remoteAddr);
        //拿出来，如果没有，那就过
        String ipSendTimeValue = (String) redisUtil.get(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        Integer ipSendTime=null;
        if (!TextUtils.isEmpty(ipSendTimeValue)) {
            ipSendTime = Integer.parseInt(ipSendTimeValue);
        }else{
            ipSendTime = 1;
        }
        if (ipSendTime > 10) {
            return ResponseResult.FAILED("请不要太频繁");
        }
        Object hasSendEmail = redisUtil.get(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress);
        if (hasSendEmail != null) {
            return ResponseResult.FAILED("请不要太频繁");
        }

        //2. 检查邮箱地址是否正确
        boolean isEmailFormatOk = TextUtils.isEmailAddressOk(emailAddress);
        if (!isEmailFormatOk) {
            return ResponseResult.FAILED("邮箱地址格式不正确!");
        }
        //0-999999
        int code = random.nextInt(999999);
        if (code < 100000) {
            code += 100000;
        }
        log.info("sendEmail ==>  code ==> :" + code);
        //3. 发送验证码,6位数100000-999999
        try {
            taskService.sendEmailVerifyCode(String.valueOf(code), emailAddress);
        } catch (Exception e) {
            return ResponseResult.FAILED("验证码发送失败！请稍后重试.");
        }
        //4. 做记录
        //包括发送记录，code
        if (ipSendTime == null) {
            ipSendTime = 0;
        }
        ipSendTime++;
        //一个小时有效期
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr, String.valueOf(ipSendTime), 60 * 60);
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress, "true", 30);
        //保存code 10分钟内有效
        redisUtil.set(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress, String.valueOf(code), 10 * 60);

        return ResponseResult.SUCCESS("发送成功");
    }

    /**
     * * 注册用户
     *
     * @param sobUser
     * @param emailCode
     * @param captchaCode
     * @return
     */

    @Override
    public ResponseResult register(SobUser sobUser, String emailCode, String captchaCode, String captchaKey, HttpServletRequest request) {

        //第一步：检查当前用户名是否已经注册
        String userName = sobUser.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("用户名不可以为空！");
        }
        SobUser userByUserName = userDao.findOneByUserName(userName);
        if (userByUserName != null) {
            return ResponseResult.FAILED("该用户已注册！");
        }
        //第二步：检查邮箱格式是否正确
        String email = sobUser.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱地址不可以为空！");
        }
        if (!TextUtils.isEmailAddressOk(email)) {
            return ResponseResult.FAILED("邮箱地址格式不正确！");
        }
        //第三步：检查该邮箱是否已经注册
        SobUser userByEmail = userDao.findOneByEmail(email);
        if (userByEmail != null) {
            return ResponseResult.FAILED("该邮箱已注册！");
        }
        //第四步：检查邮箱验证码是否正确
        String emailVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(emailVerifyCode)) {
            return ResponseResult.FAILED("邮箱验证码已过期！");
        }
        if (!emailVerifyCode.equals(emailCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确！");
        } else {
            //正确 干掉redis的内容
            redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);

        }
        //第五步：检查图灵验证码是否正确
        String captchaVerifyCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (TextUtils.isEmpty(captchaVerifyCode)) {
            return ResponseResult.FAILED("人类验证码已过期！");
        }
        if (!captchaVerifyCode.equals(captchaCode)) {
            return ResponseResult.FAILED("人类验证码不正确！");
        } else {
            redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);

        }

        //达到可以注册的条件
        //第六步：对密码进行加密
        String password = sobUser.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不可以为空！");
        }
        sobUser.setPassword(bCryptPasswordEncoder.encode(sobUser.getPassword()));

        //第七步：补全数据
        //包括：注册IP,登录IP,角色,头像,创建时间,更新时间
        String ipAddress = request.getRemoteAddr();
        sobUser.setRegIp(ipAddress);
        sobUser.setLoginIp(ipAddress);
        sobUser.setCreateTime(new Date());
        sobUser.setUpdateTime(new Date());
        sobUser.setAvatar(Constants.User.DEFAULT_AVATAR);
        sobUser.setRoles(Constants.User.ROLE_NORMAL);
        sobUser.setState(Constants.User.DEFAULT_STATE);
        sobUser.setId(idWorker.nextId() + "");
        //第八步：保存到数据库中
        userDao.save(sobUser);
        //第九步：返回结果
        return ResponseResult.GET(ResponseState.JOIN_IN_SUCCESS);
    }

    /**
     * 登录
     *
     * @param sobUser
     * @param captcha
     * @param captchaKey
     * @return
     */
    @Override
    public ResponseResult doLogin(SobUser sobUser,
                                  String captcha,
                                  String captchaKey, String from) {
        //from 可能不有值
        //如果没有值，就给它一个默认值
        if (TextUtils.isEmpty(from)
                || (!Constants.FROM_MOBILE.equals(from) && !Constants.FROM_PC.equals(from))) {
            from = Constants.FROM_MOBILE;
        }

        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        String captchaValue = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (!captcha.equals(captchaValue)) {
            return ResponseResult.FAILED("人类验证码不正确！");
        }
        //验证成功
        redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        //有可能是用户名，也有可能是邮箱
        String userName = sobUser.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("账户不可以为空！");
        }

        String password = sobUser.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不可以为空！");
        }

        SobUser userFromDb = userDao.findOneByUserName(userName);
        if (userFromDb == null) {
            userFromDb = userDao.findOneByEmail(userName);
        }
        if (userFromDb == null) {
            return ResponseResult.FAILED("用户名或密码错误！");
        }
        //用户存在
        //对比密码
        boolean matches = bCryptPasswordEncoder.matches(password, userFromDb.getPassword());
        if (!matches) {
            return ResponseResult.FAILED("用户名或密码错误！");
        }
        //密码正确
        //判断用户状态，如果是非正常，则返回结果
        if (!"1".equals(userFromDb.getState())) {
            return ResponseResult.ACCOUNT_DENIED();
        }
        //修改更新时间和登录IP
        userFromDb.setLoginIp(request.getRemoteAddr());
        userFromDb.setUpdateTime(new Date());
        userDao.save(userFromDb);
        createToken(response, userFromDb, from);
        return ResponseResult.SUCCESS("登录成功！");
    }

    /**
     * @param response
     * @param userFromDb
     * @param from
     * @return tokenKey
     */
    private String createToken(HttpServletResponse response, SobUser userFromDb, String from) {
        String oldTokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);

        //不能干掉
        RefreshToken oldRefreshToken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        if (Constants.FROM_MOBILE.equals(from)) {
            //确保单端登录，删除redis里的token
            if (oldRefreshToken != null) {
                log.info("mobile down pc up ===>" + oldTokenKey);
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToken.getTokenKey());
            }
            refreshTokenDao.deleteMobileTokenKey(oldTokenKey);
        } else if (Constants.FROM_PC.equals(from)) {
            if (oldRefreshToken != null) {
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToken.getMobileTokenKey());
                log.info("mobile up pc down ===>" + oldTokenKey);
            }
            //根据来源删除refreshTokenKey
            refreshTokenDao.deletePcTokenKey(oldTokenKey);//TODO:删除不了，好奇怪
        }

        //int deleteResult = refreshTokenDao.deleteAllByUserId(userFromDb.getId());
        //log.info("createToken deleteResult ==>" + deleteResult);
        //生成token，包含from
        Map<String, Object> claims = ClaimsUtils.sobUser2Claims(userFromDb, from);
        //token默认2个小时
        String token = JwtUtil.createToken(claims);
        //返回token的MD5值 token 会保存在redis里
        //如果前端访问的时候，携带MD5_key，从redis中获取即可
        String tokenKey = from + DigestUtils.md5DigestAsHex(token.getBytes());
        //把token保存到redis里，有效期两个小时 key是这个md5 tokenKey
        redisUtil.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValueInSecend.HOUR_2);
        //把这个tokenKey写到rookies里面去
        //Cookie cookie = new Cookie(Constants.User.COOKIE_TOKEN_KEY,tokenKey);
        //域名：这个会有动态获取，可以从request获取，
        CookieUtils.setUpCookie(response, Constants.User.COOKIE_TOKEN_KEY, tokenKey);//2周
        //先判断数据库里有没有
        //如果有就更新
        //如果没有就创建
        RefreshToken refreshToken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setId(idWorker.nextId() + "");
            refreshToken.setCreateTime(new Date());
            refreshToken.setUserId(userFromDb.getId());

        }
        //不管是过期了，还是新登录，都会生成和更新
        //去生成refreshToken 有效期1个月
        String refreshTokenValue = JwtUtil.createRefreshToken(userFromDb.getId(), Constants.TimeValueInMillions.MOUTH);
        //保存到数据库里
        //refreshToken，tokenKey，用户ID，创建时间，更新时间

        refreshToken.setRefreshToken(refreshTokenValue);
        //要判断来源,如果是移动端的就设置到移动端，
        //如果是PC的就设置到默认
        if (Constants.FROM_PC.equals(from)) {

            refreshToken.setTokenKey(tokenKey);
        } else {
            refreshToken.setMobileTokenKey(tokenKey);
        }
        refreshToken.setUpdateTime(new Date());
        refreshTokenDao.save(refreshToken);
        return tokenKey;
    }

    /**
     * 本质，通过携带的token_key，检查用户是否有登录，如果登录了，就返回用户信息
     *
     * @return
     */
    @Override
    public SobUser checkSobUser() {

        //拿到token_key, 从rookie
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        log.info("checkSobUser tokenKey ==>" + tokenKey);
        if (TextUtils.isEmpty(tokenKey)) {
            return null;
        }
        //说明有token，解析token
        SobUser sobUser = parseByTokenKey(tokenKey);
        //从token中解析出此请求啊什么端
        String from = tokenKey.startsWith(Constants.FROM_PC) ? Constants.FROM_PC : Constants.FROM_MOBILE;
        if (sobUser == null) {
            //说明解析出错了，过期了
            //1. 去musql查询refreshToken
            //如果是从PC，我们就以pc的tokenKey 来查
            //如果是从mobile，我们就以mobile的tokenKey 来查
            RefreshToken refreshToken;
            if (Constants.FROM_PC.equals(from)) {
                refreshToken = refreshTokenDao.findOneByTokenKey(tokenKey);
            } else {
                refreshToken = refreshTokenDao.findOneByMobileTokenKey(tokenKey);
            }
            //2. 如果不存在，就是当前访问没有的挡路
            if (refreshToken == null) {
                log.info("refreshToken is  null....  ");
                return null;
            }
            //3. 如果存在呢，就解析refreshToken
            try {
                //这个解析有可能出错
                JwtUtil.parseJWT(refreshToken.getRefreshToken());
                //5. 如果refreshToken有效，创建新的token,和新的refreshToken
                String userId = refreshToken.getUserId();
                SobUser userFromDb = userDao.findOneById(userId);
                //删掉refreshToken的记录
                refreshTokenDao.deleteById(refreshToken.getId());
                String newTokenKey = createToken(getResponse(), userFromDb, from);
                //干掉redis的tokenKey
                redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
                //返回token
                log.info("create new token  and refresh token ");
                return parseByTokenKey(newTokenKey);
            } catch (Exception e1) {
                //4. 如果refreshToken过期了，就当前访问没有登录，提示用户登录
                log.info("refreshToken is  过期  ");
                return null;
            }
        }
        return sobUser;
    }

    @Override
    public ResponseResult getUserInfo(String userId) {
        //数据库里获取
        SobUser user = userDao.findOneById(userId);
        // 判断结果
        if (user == null) {
            // 如果不存在，就返回不存在
            return ResponseResult.FAILED("用户不存在！");
        }
        // 如果存在，就返回结果，赋值对象，清空密码，Email,登录IP，注册IP
        String userJson = gson.toJson(user);
        SobUser newSobUser = gson.fromJson(userJson, SobUser.class);
        newSobUser.setPassword("");
        newSobUser.setEmail("");
        newSobUser.setLoginIp("");
        newSobUser.setRegIp("");
        // 返回结果
        return ResponseResult.SUCCESS("获取成功").setData(newSobUser);
    }

    @Override
    public ResponseResult checkEmail(String email) {
        SobUser user = userDao.findOneByEmail(email);
        return user == null ? ResponseResult.FAILED("该邮箱未注册！") : ResponseResult.SUCCESS("该邮箱已被注册！");
    }

    @Override
    public ResponseResult checkUserName(String userName) {
        SobUser user = userDao.findOneByUserName(userName);
        return user == null ? ResponseResult.FAILED("该用户名未注册！") : ResponseResult.SUCCESS("该用户名已被注册！");
    }

    /**
     * 更新用户信息
     *
     * @param userId
     * @param sobUser
     * @return
     */
    @Override
    public ResponseResult updateUserInfo(String userId, SobUser sobUser) {
        //从Token里解析出来的user,为了检验权限
        //只有用户自己才可以修改
        SobUser userFromTokenKey = checkSobUser();
        if (userFromTokenKey == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        SobUser userFromDb = userDao.findOneById(userFromTokenKey.getId());
        //可以判断用户的ID和当前修改的用户ID 是否一致，一致才可以修改
        if (!userFromDb.getId().equals(userId)) {
            return ResponseResult.PERMISSION_DENIED();
        }
        //可以修改
        //可以修改的项
        //用户名
        String userName = sobUser.getUserName();
        if (!TextUtils.isEmpty(sobUser.getUserName())&&!userName.equals(userFromTokenKey.getUserName())) {
            //检查是否已经存在
            SobUser userByUserName = userDao.findOneByUserName(userName);
            if (userByUserName != null) {
                return ResponseResult.FAILED("用户名已注册！");
            }
            userFromDb.setUserName(sobUser.getUserName());
        }
        //头像
        if (!TextUtils.isEmpty(sobUser.getAvatar())) {
            userFromDb.setAvatar(sobUser.getAvatar());
        }
        //签名 可以为空
        userFromDb.setSign(sobUser.getSign());
        userFromDb.setUpdateTime(new Date());
        userDao.save(userFromDb);

        //干掉redis 里的token,下一次请求，需要解析token的，就会根据refreshToken重新创建一个
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        return ResponseResult.SUCCESS("用户信息更新成功.");
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    /**
     * 删除用户
     * 删除用户，并不是真的删除
     * 而是修改状态
     * <p>
     * PS：需要管理员权限
     *
     * @param userId
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @Override
    public ResponseResult deleteUserById(String userId, String state) {
        //可以删除用户
        int result = userDao.deleteUserByState(state, userId);

        if (result > 0) {
            return ResponseResult.SUCCESS("删除成功");
        }
        return ResponseResult.FAILED("用户不存在");
    }

    @Autowired
    private UserNoPasswordDao userNoPasswordDao;

    /**
     * 需要管理员权限
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @Override
    public ResponseResult listUsers(int page, int size, String userName, String email) {

        //可以获取用户列表
        //分页查询
        page = checkPage(page);
        size = checkSize(size);
        //根据注册日期来排序
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<SobUserNoPassword> all = userNoPasswordDao.findAll(new Specification<SobUserNoPassword>() {
            @Override
            public Predicate toPredicate(Root<SobUserNoPassword> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (!TextUtils.isEmpty(userName)) {
                    Predicate preUser = cb.like(root.get("userName").as(String.class), "%" + userName + "%");
                    predicates.add(preUser);
                }
                if (!TextUtils.isEmpty(email)) {
                    Predicate preEmail = cb.like(root.get("email").as(String.class), "%" + email + "%");
                    predicates.add(preEmail);
                }
                Predicate[] preArray = new Predicate[predicates.size()];
                predicates.toArray(preArray);
                return cb.and(preArray);
            }
        }, pageable);


        return ResponseResult.SUCCESS("获取用户列表成功").setData(all);
    }

    /**
     * 更新密码
     *
     * @param verifyCode
     * @param sobUser
     * @return
     */
    @Override
    public ResponseResult updateUserPassword(String verifyCode, SobUser sobUser) {
        //检查邮箱是否有填写
        String email = sobUser.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱不可以为空.");
        }
        //根据邮箱去redis里拿验证
        //进行对比
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误.");
        }
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        int result = userDao.updatePasswordByEmail(bCryptPasswordEncoder.encode(sobUser.getPassword()), email);
        //修改密码
        return result > 0 ? ResponseResult.SUCCESS("密码修改成功") : ResponseResult.FAILED("密码修改失败");
    }

    /**
     * 更新邮箱
     *
     * @param email
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updateEmail(String email, String verifyCode) {
        //1、确保用户已经登录了
        SobUser sobUser = this.checkSobUser();
        //没有登录
        if (sobUser == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、对比验证码，确保新的邮箱地址是属于当前用户的
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(redisVerifyCode) || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误");
        }
        //正确
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        //可以修改邮箱
        int result = userDao.updateEmailById(email, sobUser.getId());
        return result > 0 ? ResponseResult.SUCCESS("邮箱修改成功") : ResponseResult.FAILED("邮箱修改失败");
    }

    @Override
    public ResponseResult doLogout() {
        //拿到token_key
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //删除reids里的token,因为各端是独立的，所以可以删除
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        //删除Mysql 里的token
        //这个不做删除，只做更新
        //int result = refreshTokenDao.deleteAllByTokenKey(tokenKey);
        if (Constants.FROM_PC.startsWith(tokenKey)) {
            refreshTokenDao.deletePcTokenKey(tokenKey);
        } else {
            refreshTokenDao.deleteMobileTokenKey(tokenKey);
        }
        //删除cookie里的token_key
        CookieUtils.deleteCookie(getResponse(), Constants.User.COOKIE_TOKEN_KEY);
        return ResponseResult.SUCCESS("退出登录成功.");
    }

    @Override
    public ResponseResult getPcLoginQrCodeInfo() {
        //尝试取出上一次的loginId
        String lastLoginId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_REQUESTL_LOGIN_ID);
        if (!TextUtils.isEmpty(lastLoginId)) {
            //先把redis删除
            redisUtil.del(Constants.User.KEY_PC_LOGIN_ID + lastLoginId);
            //检查上次的请求时间，如果太频繁
            Object lastGetTime = redisUtil.get(Constants.User.LAST_REQUESTL_LOGIN_ID + lastLoginId);
            if (lastGetTime != null) {
                return ResponseResult.FAILED("服务器繁忙，请重试");
            }
        }

        // 1、生成一个唯一的ID
        long code = idWorker.nextId();
        // 2、保存到redis里，值为false，时间为5分钟（二维码的有效期）
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + code,
                Constants.User.KEY_PC_LOGIN_STATE_FALSE,
                Constants.TimeValueInSecend.MIN_5);
        Map<String, Object> result = new HashMap<>();
        String originalDomain = TextUtils.getDomain(getRequest());
        result.put("code", code);
        result.put("url", originalDomain + "/portal/image/qr-code/" + code);
        CookieUtils.setUpCookie(getResponse(), Constants.User.LAST_REQUESTL_LOGIN_ID, String.valueOf(code));
        redisUtil.set(Constants.User.LAST_REQUESTL_LOGIN_ID + String.valueOf(code),
                "true", Constants.TimeValueInSecend.SECOND_10);

        return ResponseResult.SUCCESS("获取成功.").setData(result);
    }

    @Autowired
    private CountDownLatchManager countDownLatchManager;

    /**
     * 检查二维码的登录状态
     * 结果有多种
     * 1. 登录成功（loginId对应的值为有id）
     * 2. 等待扫描（loginId对应的值为false）
     * 3. 二维码已经过期了（loginId对应的值为null）
     * <p>
     * 是被PC端轮询调用的
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult checkQrCodeLoginState(String loginId) {
        //从redis里取值，检查状态
        ResponseResult result = checkLoginIdState(loginId);
        if (result != null) return result;
        //先等待一段时间，再去检查
        //如果超出了这个时间，我们就返回等待扫码
        Callable<ResponseResult> callable = new Callable<ResponseResult>() {
            @Override
            public ResponseResult call() throws Exception {
                log.info("start waiting for scan...");
                try {
                    //先阻塞
                    countDownLatchManager.getLatch(loginId).await(Constants.User.QR_CODE_STATE_CHECK_WAITING_TIME, TimeUnit.SECONDS);
                    //收到状态更新的通知，我们 检查loginId对应的状态
                    log.info("start check login state...");
                    ResponseResult check = checkLoginIdState(loginId);
                    if (check != null) return check;
                    //超时则返回等待扫描
                    //完事后，删除对应的latch
                    return ResponseResult.WAITING_FOR_SCAN();
                } finally {
                    log.info("delete latch...");
                    countDownLatchManager.deleteLatch(loginId);
                }

            }
        };
        try {

            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.WAITING_FOR_SCAN();
    }

    /**
     * 更新二维码的登录状态
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult updateQrCodeLoginState(String loginId) {
        //1、检查用户是否登录
        SobUser sobUser = checkSobUser();
        if (sobUser == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、改变loginId对应的值=true
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + loginId, sobUser.getId());
        //2.1、通知正在等待的扫描任务
        countDownLatchManager.onPhoneDoLogin(loginId);
        //3、返回结果
        return ResponseResult.SUCCESS("登录成功.");
    }

    @Override
    public ResponseResult parseToken() {
        SobUser sobUser = checkSobUser();
        if (sobUser == null) {
            return ResponseResult.FAILED("用户未登录！");
        }
        return ResponseResult.SUCCESS("获取用户成功").setData(sobUser);
    }

    @Override
    public ResponseResult resetPassword(String userId, String password) {
        //查询出用户
        SobUser user = userDao.findOneById(userId);
        if (user == null) {
            //判断是否存在
            return ResponseResult.FAILED("用户不存在");

        }
        //密码加密
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userDao.save(user);
        //处理结果
        return ResponseResult.SUCCESS("重置密码成功");
    }

    private ResponseResult checkLoginIdState(String loginId) {
        String loginState = (String) redisUtil.get(Constants.User.KEY_PC_LOGIN_ID + loginId);
        if (loginState == null) {
            //二维码过期
            return ResponseResult.QR_CODE_DEPRECATE();
        }
        //不为false 且 不为null，那么就是用户的ID了，也就是登录成功了
        if (!TextUtils.isEmpty(loginState) && !Constants.User.KEY_PC_LOGIN_STATE_FALSE.equals(loginState)) {

            //创建token，也就走PC端的登录
            SobUser userFromDb = userDao.findOneById(loginState);
            if (userFromDb == null) {
                return ResponseResult.QR_CODE_DEPRECATE();
            }
            createToken(getResponse(), userFromDb, Constants.FROM_PC);
            return ResponseResult.LOGIN_SUCCESS();
            //登录成功
        }
        return null;
    }

    /**
     * 解析 此token是从哪里来的，PC 还是mobile
     *
     * @param tokenKey
     * @return
     */
    private String parseFrom(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        log.info("parseByTokenKey token ==>" + token);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.getFrom(claims);
            } catch (Exception e) {
                log.info("parseByTokenKey ==>" + tokenKey + " 过期了。。。");
            }
        }
        return null;
    }

    private SobUser parseByTokenKey(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        log.info("parseByTokenKey token ==>" + token);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.claims2SobUser(claims);
            } catch (Exception e) {
                log.info("parseByTokenKey ==>" + tokenKey + " 过期了。。。");
                return null;
            }
        }
        return null;
    }
}
