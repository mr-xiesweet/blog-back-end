package top.xie.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.pojo.Comment;
import top.xie.response.ResponseResult;
import top.xie.services.ICommentService;

@RestController
@RequestMapping("/portal/comment")
public class CommentPortalApi {

    @Autowired
    private ICommentService commentService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult postComment(@RequestBody Comment comment){

        return commentService.postComment(comment);
    }
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId){
        return commentService.deleteCommentById(commentId);
    }



    @GetMapping("/list/{articleId}/{page}/{size}")
    public ResponseResult listComment(@PathVariable("articleId") String articleId,
                                      @PathVariable("page") int page,
                                      @PathVariable("size") int size){

        return commentService.listCommentByArticleId(articleId, page, size);
    }
}
