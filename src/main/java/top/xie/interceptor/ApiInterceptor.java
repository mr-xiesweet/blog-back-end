package top.xie.interceptor;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import top.xie.response.ResponseResult;
import top.xie.utils.Constants;
import top.xie.utils.CookieUtils;
import top.xie.utils.RedisUtil;
import top.xie.utils.TextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Slf4j
@Component
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            CheckTooFrequentCommit methodAnnotation = handlerMethod.getMethodAnnotation(CheckTooFrequentCommit.class);
            String methodName = handlerMethod.getMethod().getName();
            if (methodAnnotation != null) {
                //所有提交内容的方法，必须用户登录，所以使用token作为key来记录请求频率
                String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
                if (!TextUtils.isEmpty(tokenKey)) {
                    String hasCommit = (String) redisUtil.get(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey+ methodName);
                    if (!TextUtils.isEmpty(hasCommit)) {
                        //从redis里获取，判断是否存在，如果存在，则返回提交太频繁
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        ResponseResult failed = ResponseResult.FAILED("提交太频繁,请稍后！");

                        PrintWriter writer = response.getWriter();
                        writer.write(gson.toJson(failed));
                        writer.flush();
                        return false;
                    } else {
                        //如果不存在，说明可以提交，并且记录此次提交，有效期为30秒
                        redisUtil.set(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey + methodName,
                                "true", Constants.TimeValueInSecend.SECOND_10);
                    }
                }
                log.info("token Key");
                //去判断是否提交太频繁
            }
        }
        //true 表示放行
        //false 表示拦截
        return true;
    }
}
