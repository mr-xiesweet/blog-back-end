package top.xie.services.Impl;

import top.xie.utils.Constants;

public class BaseService {
    int checkPage(int page){
        if (page< Constants.Page.DEFAULT_PAGE){
            page = Constants.Page.DEFAULT_PAGE;
        }
        return page;
    }
    int checkSize(int size){
        if (size< Constants.Page.MIN_SIZE){
            size = Constants.Page.MIN_SIZE;
        }
        return size ;
    }
}
