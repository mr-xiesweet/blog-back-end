package top.xie.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xie.services.IImageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/portal/image")
public class ImagePortalApi {

    @Autowired
    private IImageService imageService;

    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId") String imageId){
        try {
            imageService.viewImage(imageId,response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/qr-code/{code}")
    private void getQrCodeImage(@PathVariable("code") String code,
                                HttpServletResponse response,
                                HttpServletRequest request
                                ){
        imageService.createQrCode(code,response,request);

    }
}
