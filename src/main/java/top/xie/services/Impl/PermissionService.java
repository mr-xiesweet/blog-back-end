package top.xie.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.xie.pojo.SobUser;
import top.xie.services.IUserService;
import top.xie.utils.Constants;
import top.xie.utils.CookieUtils;
import top.xie.utils.TextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service("permission")
public class PermissionService {

    @Autowired
    private IUserService userService;

    /**
     * 判断是不是为管理员
     * @return
     */
    public boolean adminPermission() {
        // 获取到当前权限所有的角色，进行角色对比即可确定权限
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        //如果token返回false
        String token = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        SobUser sobUser = userService.checkSobUser();
        if (sobUser == null || TextUtils.isEmpty(sobUser.getRoles())) {
            return false;
        }
        if (Constants.User.ROLE_ADMIN.equals(sobUser.getRoles())) {
            log.info("=====  是管理员");
            return true;
        }
        return false;
    }
}
