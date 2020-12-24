package top.xie.services.Impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import top.xie.dao.ArticleNoContentDao;
import top.xie.dao.CommentDao;
import top.xie.pojo.ArticleNoContent;
import top.xie.pojo.Comment;
import top.xie.pojo.PageList;
import top.xie.pojo.SobUser;
import top.xie.response.ResponseResult;
import top.xie.services.ICommentService;
import top.xie.services.IUserService;
import top.xie.utils.Constants;
import top.xie.utils.IdWorker;
import top.xie.utils.RedisUtil;
import top.xie.utils.TextUtils;

import javax.transaction.Transactional;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class CommentServiceImpl extends BaseService implements ICommentService {

    @Autowired
    private IUserService userService;

    @Autowired
    private ArticleNoContentDao articleNoContentDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CommentDao commentDao;

    /**
     * 发表评论
     *
     * @param comment 评论
     * @return
     */
    @Override
    public ResponseResult postComment(Comment comment) {
        //检查用户是否有登录
        SobUser sobUser = userService.checkSobUser();
        if (sobUser == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //检查内容
        String articleId = comment.getArticleId();
        if (TextUtils.isEmpty(articleId)) {
            return ResponseResult.FAILED("文章ID不可以为空.");
        }
        ArticleNoContent article = articleNoContentDao.findOneById(articleId);
        if (article == null) {
            return ResponseResult.FAILED("文章不存在.");
        }
        String content = comment.getContent();
        if (TextUtils.isEmpty(content)) {
            return ResponseResult.FAILED("评论内容不可以为空.");
        }
        //补全内容
        comment.setId(idWorker.nextId() + "");
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        comment.setUserAvatar(sobUser.getAvatar());
        comment.setUserName(sobUser.getUserName());
        comment.setUserId(sobUser.getId());
        //保存入库
        commentDao.save(comment);
        //清除对应文章的评论缓存
        redisUtil.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());
        //返回结果
        return ResponseResult.SUCCESS("评论成功");
    }

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    /**
     * 获取文章的评论
     * 评论的排序策略：
     * 最基本的就按时间排序-->升序和降序-->先发表的在前面或者后发表的在前面
     * <p>
     * 置顶的：一定在前最前面
     * <p>
     * 后发表的：前单位时间内会排在前面，过了此单位时间，会按点赞量和发表时间进行排序
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listCommentByArticleId(String articleId, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        //如果是第一页，那我先从缓存中那

        String cacheJson = (String) redisUtil.get(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId);
        if (!TextUtils.isEmpty(cacheJson)&&page==1) {

            PageList<Comment> result = gson.fromJson(cacheJson, new TypeToken<PageList<Comment>>() {
            }.getType());
            log.info("comment list from redis...");
            return ResponseResult.SUCCESS("评论列表获取成功.").setData(result);

        }
        //如果有就返回
        //如果没有就往下走
        Sort sort = new Sort(Sort.Direction.DESC, "state", "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAllByArticleId(articleId,pageable);
        //把结果转成pagelist
        PageList<Comment> reslut = new PageList<>();
        reslut.parsePage(all);
        if (page == 1) {
            redisUtil.set(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId, gson.toJson(reslut), Constants.TimeValueInSecend.HOUR / 2);
        }
        return ResponseResult.SUCCESS("评论列表获取成功.").setData(reslut);
    }

    @Override
    public ResponseResult deleteCommentById(String commentId) {
        //检查用户角色
        SobUser sobUser = userService.checkSobUser();
        if (sobUser == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //把评论找出来，比对用户权限
        Comment commentFromDb = commentDao.findOneById(commentId);
        if (commentFromDb == null) {
            return ResponseResult.FAILED("评论不存在.");
        }
        if (sobUser.getId().equals(commentFromDb.getUserId()) ||
                Constants.User.ROLE_ADMIN.equals(sobUser.getRoles())) {
            //判断角色
            //用户ID 一致，说明此评论是当前用户的
            //管理员
            commentDao.deleteById(commentId);
            return ResponseResult.SUCCESS("删除评论成功");
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }

    }

    @Override
    public ResponseResult listComments(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);


        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取评论列表成功.").setData(all);
    }

    @Override
    public ResponseResult topComment(String commentId) {
        Comment comment = commentDao.findOneById(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在.");
        }
        String state = comment.getState();
        if (Constants.Comment.STATE_PUBLISH.equals(state)) {
            comment.setState(Constants.Comment.STATE_TOP);
            return ResponseResult.SUCCESS("置顶成功.");
        } else if (Constants.Comment.STATE_TOP.equals(state)) {
            comment.setState(Constants.Comment.STATE_PUBLISH);
            return ResponseResult.SUCCESS("取消置顶.");
        } else {
            return ResponseResult.FAILED("评论状态非法.");
        }
    }
}
