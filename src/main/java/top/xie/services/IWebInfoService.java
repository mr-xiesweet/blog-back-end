package top.xie.services;

import top.xie.response.ResponseResult;

public interface IWebInfoService {
    ResponseResult getWebSizeTitle();

    ResponseResult putWebSizeTitle(String title);

    ResponseResult getSeoInfo();

    ResponseResult putSeoInfo(String keyWords, String description);

    ResponseResult getWebSizeViewCount();

    void updateViewCount();

}
