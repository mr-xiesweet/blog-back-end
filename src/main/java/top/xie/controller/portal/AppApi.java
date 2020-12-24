package top.xie.controller.portal;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 应用下载
 * 应用更新
 */
@RestController
@RequestMapping("/portal/app")
public class AppApi {
    /**
     * 给第三方扫描下载App用的接口
     * @return
     */
    @GetMapping("/{code}")
    public void downloadAppForThirdPartScan(@PathVariable("code")String code, HttpServletRequest request,
                                            HttpServletResponse response){
        //TODO:直接把最新的app写出去


    }
}
