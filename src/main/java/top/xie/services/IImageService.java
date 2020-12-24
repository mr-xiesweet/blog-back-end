package top.xie.services;

import org.springframework.web.multipart.MultipartFile;
import top.xie.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IImageService {
    ResponseResult uploadImage(MultipartFile file,String original);

    void viewImage(String imageId, HttpServletResponse response) throws IOException;

    ResponseResult listImages(int page, int size,String original);

    ResponseResult deleteById(String imageId);

    void createQrCode(String code, HttpServletResponse response, HttpServletRequest request);
}
