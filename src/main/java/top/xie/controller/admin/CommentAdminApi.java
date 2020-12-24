package top.xie.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.response.ResponseResult;
import top.xie.services.ICommentService;

@RequestMapping("/admin/comment")
@RestController
public class CommentAdminApi {

    @Autowired
    private ICommentService commentService;

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId){

        return commentService.deleteCommentById(commentId);
    }


    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listComments(@PathVariable("page") int page,
                                       @PathVariable("size") int size){

        return commentService.listComments(page, size);
    }

    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/top/{commentId}")
    public ResponseResult topComment(@PathVariable("commentId") String commentId){

        return commentService.topComment(commentId);
    }
}
