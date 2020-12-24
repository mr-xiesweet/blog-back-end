package top.xie.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xie.response.ResponseResult;
import top.xie.services.ICategoryService;
import top.xie.services.IFriendLinkService;
import top.xie.services.ILoopService;
import top.xie.services.IWebInfoService;

@RestController
@RequestMapping("/portal/web_size_info")
public class WebSizeInfoPortalApi {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IFriendLinkService friendLinkService;

    @Autowired
    private ILoopService loopService;

    @Autowired
    private IWebInfoService webInfoService;

    @GetMapping("/categories")
    public ResponseResult getCategories(){
        return categoryService.listCategories();
    }

    @GetMapping("/title")
    public ResponseResult getWebSizeTitle(){

        return webInfoService.getWebSizeTitle();
    }

    @GetMapping("/view_count")
    public ResponseResult getWebSizeViewCount(){

        return webInfoService.getWebSizeViewCount();
    }


    @GetMapping("/seo")
    public ResponseResult getWebSizeSeoInfo(){

        return webInfoService.getSeoInfo();
    }


    @GetMapping("/looper")
    public ResponseResult getLoops(){

        return loopService.listLoops();
    }


    @GetMapping("/friend_link")
    public ResponseResult getFriendLinks(){

        return friendLinkService.listFriendLinks();
    }

    /**
     * 统计访问页，每个页面都统一次，PV，page view.
     * 直接增加一个访问量，可以刷量
     * 根据ip进行一些过滤，可以集成第三方的一个统计工具
     * //
     * 递增的统计
     * 统计信息，通过redis来统计，数据也会保存在mysql里
     * 不会每次都更新到Mysql里，当用户去获取访问量的时候，会更新一次
     * 平时的调用，只增加redis里的访问量
     * <p>
     * redis时机：每个页面访问的时候，如果不在从mysql中读取数据，写到redis里
     * 如果，就自增
     * <p>
     * mysql的时机，用户读取网站总访问量的时候，我们就读取一redis的，并且更新到mysql中
     * 如果redis里没有，那就读取mysql写到reds里的
     */
    @PutMapping("view_count")
    public void updateViewCount(){
        webInfoService.updateViewCount();
    }
}
