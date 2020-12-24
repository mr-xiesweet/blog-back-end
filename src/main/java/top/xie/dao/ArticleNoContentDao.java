package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.ArticleNoContent;

import java.util.List;

public interface ArticleNoContentDao extends JpaRepository<ArticleNoContent,String>, JpaSpecificationExecutor<ArticleNoContent> {
    ArticleNoContent findOneById(String articleId);

    @Query(value = "SELECT * FROM `tb_article` WHERE `labels` LIKE ?1 AND `id` != ?2 AND (`state` = '1' OR `state` = '3') LIMIT ?3 ",nativeQuery = true)
    List<ArticleNoContent> listArticleByLikeLabel(String label, String originalArticleId, int size);

    @Query(value = "SELECT * FROM `tb_article` where `id`!= ? AND (`state` = '1' OR `state` = '3') ORDER BY `create_time` DESC LIMIT ?",nativeQuery = true)
    List<ArticleNoContent> listLastedArticleBySize(String originalArticleId, int dxSize);
}
