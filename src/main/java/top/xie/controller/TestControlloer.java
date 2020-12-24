package top.xie.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.xie.dao.CommentDao;
import top.xie.pojo.Comment;
import top.xie.pojo.SobUser;
import top.xie.response.ResponseResult;
import top.xie.response.ResponseState;
import top.xie.services.IUserService;
import top.xie.services.Impl.SolrTestService;
import top.xie.utils.Constants;
import top.xie.utils.CookieUtils;
import top.xie.utils.IdWorker;
import top.xie.utils.RedisUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
@RestController
public class TestControlloer {

    @Autowired
    private IUserService userService;

    @Autowired
    private CommentDao commentDao;

    @RequestMapping(value = "/hello-world",method = RequestMethod.GET)
    public String helloWorld(){
        System.out.println("hellow world");
        String redis = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + "123456");
        return redis;
    }

    @GetMapping("/test")
    public ResponseResult testjson(){


        SobUser sobUser = new SobUser();
        sobUser.setUserName("特朗普");
        sobUser.setAvatar("asd");
        ResponseResult responseResult = new ResponseResult(ResponseState.SUCCESS);
        responseResult.setData(sobUser);
        return responseResult;
    }

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        // specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        specCaptcha.setFont(Captcha.FONT_1);
        // 设置类型，纯数字、纯字母、字母数字混合
        //specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String content = specCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);
        // 验证码存入session
        //request.getSession().setAttribute("captcha", content);
        //保存到redis里，5分钟有效
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT+"123456",content,60*5);

        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private IdWorker idWorker;

    @PostMapping("/comment")
    public ResponseResult testComment(@RequestBody Comment comment,
                                      HttpServletRequest request,
                                      HttpServletResponse response){

        String content = comment.getContent();
        //还得知道是谁的评论，对这个评论，身份确认
        String tokenKey = CookieUtils.getCookie(request,Constants.User.COOKIE_TOKEN_KEY);
        if (tokenKey == null) {
            return ResponseResult.FAILED("账号未登录！");
        }
        SobUser sobUser = userService.checkSobUser(); // 如果get不到rookie会报错
        if (sobUser == null) {
            return ResponseResult.FAILED("账号未登录！");
        }
        comment.setUserId(sobUser.getId());
        comment.setUserAvatar(sobUser.getAvatar());
        comment.setUserName(sobUser.getUserName());
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setId(idWorker.nextId()+"");
        commentDao.save(comment);
        return ResponseResult.SUCCESS("评论成功！");

    }

    @Autowired
    private SolrTestService solrTestService;

    @PostMapping("/solr")
    public ResponseResult solrAddTest(){

        solrTestService.add();
        return ResponseResult.SUCCESS("添加成功");
    }

    @PostMapping("/solr/all")
    public ResponseResult solrAddAllTest(){

        solrTestService.importAll();
        return ResponseResult.SUCCESS("添加全部成功");
    }

    @PutMapping("/solr")
    public ResponseResult solrUpdateTest(){

        solrTestService.update();
        return ResponseResult.SUCCESS("更新成功");
    }

    @DeleteMapping("/solr")
    public ResponseResult solrDeleteTest(){

        solrTestService.delete();
        return ResponseResult.SUCCESS("删除成功");
    }

    @DeleteMapping("/solr/all")
    public ResponseResult solrDeleteAllTest(){

        solrTestService.deleteAll();
        return ResponseResult.SUCCESS("删除全部成功");
    }


}
