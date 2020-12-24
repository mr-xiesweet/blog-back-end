package top.xie.services;

import top.xie.pojo.Article;
import top.xie.response.ResponseResult;

public interface ISolrService {
    ResponseResult doSearch(String keyword, int page, int size, String categoryId, Integer sort);

    void addArticle(Article article);

    void deleteArticle(String articleId);

    void updateArticle(String articleId,Article article);
}
