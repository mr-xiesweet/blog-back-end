package top.xie.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.response.ResponseResult;
import top.xie.services.IImageService;


@RestController
@RequestMapping("/admin/image")
public class ImageAdminApi {

    @Autowired
    private IImageService imageService;

    /**
     * 关于图片（文件）上传
     * 一般来说，现在比较常用的是对象存储--->很简单，看文档就可以学会了
     * 使用 Nginx + fastDFS == > fastDFS -- > 处理文件上传， Nginx -- > 负责处理文件访问
     *
     * @param file
     * @return
     */

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PostMapping("/{original}")
    public ResponseResult uploadImage(@RequestParam("file") MultipartFile file,
                                      @PathVariable("original")String original){

        return imageService.uploadImage(file,original);
    }

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId") String imageId){

        return imageService.deleteById(imageId);
    }
/*
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId") String imageId){

        try {
            imageService.viewImage(imageId,response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listImages(@PathVariable("page") int page,
                                     @PathVariable("size") int size,
                                     @RequestParam(value = "original",required = false)String original){

        return imageService.listImages(page,size,original);
    }


}
