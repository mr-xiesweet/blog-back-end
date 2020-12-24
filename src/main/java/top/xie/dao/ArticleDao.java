package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.Article;

public interface ArticleDao extends JpaRepository<Article,String>, JpaSpecificationExecutor<Article> {
    Article findOneById(String articleId);

    @Modifying
    int deleteAllById(String articleId);

    @Modifying
    @Query(nativeQuery = true,value = "update `tb_article` set `state`='0' where `id`=? ")
    int deleteArticleByState(String articleId);

    @Modifying
    @Query(nativeQuery = true,value = "update `tb_article` set `state`='3' where `id`=? ")
    int topArticle(String articleId);


    @Query(value = "select `labels` from `tb_article` where `id` = ? ",nativeQuery = true)
    String listArticleLabelsById(String articleId);
}
