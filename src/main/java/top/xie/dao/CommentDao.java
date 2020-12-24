package top.xie.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.xie.pojo.Comment;

public interface CommentDao extends JpaRepository<Comment,String>, JpaSpecificationExecutor<Comment> {
    Comment findOneById(String CommentId);

    int deleteAllByArticleId(String articleId);

    Page<Comment> findAllByArticleId(String articleId, Pageable pageable);
}
