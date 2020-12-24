package top.xie.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.pojo.Article;
import top.xie.response.ResponseResult;
import top.xie.services.IArticleService;

@RestController
@RequestMapping("/admin/article")
public class ArticleAdminApi {

    @Autowired
    private IArticleService articleService;

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PostMapping
    public ResponseResult postArticle(@RequestBody Article article) {

        return articleService.postArticle(article);
    }

    /**
     * 如果是多用户，用户不可以删除，删除只是修改状态
     * 管理可以删除
     * <p>
     * 做成真的删除
     *
     * @param articleId
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId") String articleId) {

        return articleService.deleteArticleById(articleId);
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId") String articleId,
                                        @RequestBody Article article) {
        return articleService.updateArticle(articleId, article);
    }

    /**
     * 获取文章详情
     * 权限：任意用户
     * <p>
     * 内容过滤：只允许拿置顶的，或者已经发布的
     * 其他的获取：比如说草稿、只能对应用户获取。已经删除的，只有管理员才可以获取.
     *
     * @param articleId
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/{articleId}")
    public ResponseResult getArticle(@PathVariable("articleId") String articleId) {

        return articleService.getArticleById(articleId);
    }

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page") int page,
                                       @PathVariable("size") int size,
                                       @RequestParam(value = "state", required = false) String state,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "categoryId", required = false) String categoryId) {

        return articleService.listArticles(page, size, keyword, categoryId, state);
    }

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/state/{articleId}")
    public ResponseResult deleteArticleByUpdateState(@PathVariable("articleId") String articleId) {
        return articleService.deleteArticleByState(articleId);
    }

    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/top/{articleId}")
    public ResponseResult topArticle(@PathVariable("articleId") String articleId) {

        return articleService.topArticle(articleId);
    }


}
