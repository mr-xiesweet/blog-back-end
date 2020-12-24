package top.xie.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.response.ResponseResult;
import top.xie.services.IWebInfoService;

@RequestMapping("/admin/web_size_info")
@RestController
public class WebSizeInfoAdminApi {



    @Autowired
    private IWebInfoService webInfoService;

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/title")
    public ResponseResult getWebSizeTitle(){

        return webInfoService.getWebSizeTitle();
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/title")
    public ResponseResult upWebSizeTitle(@RequestParam("title")String title){

        return webInfoService.putWebSizeTitle(title);
    }


    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/seo")
    public ResponseResult getSeoInfo(){

        return webInfoService.getSeoInfo();
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/seo")
    public ResponseResult putSeoInfo(@RequestParam("keywords") String keyWords,
                                     @RequestParam("description")String description){


        return webInfoService.putSeoInfo(keyWords,description);
    }


    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/view_count")
    public ResponseResult getWebSizeViewCount(){
        return webInfoService.getWebSizeViewCount();
    }
}
