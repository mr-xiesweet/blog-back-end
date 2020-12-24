package top.xie.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import top.xie.dao.CategoryDao;
import top.xie.pojo.Category;
import top.xie.pojo.SobUser;
import top.xie.response.ResponseResult;
import top.xie.services.ICategoryService;
import top.xie.services.IUserService;
import top.xie.utils.Constants;
import top.xie.utils.IdWorker;
import top.xie.utils.TextUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CategoryServiceImpl extends BaseService implements ICategoryService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private IUserService userService;


    @Override
    public ResponseResult addCategory(Category category) {
        //先检查数据
        // 必须的数据有：
        //分类名称、分类的pinyin、顺序、描述
        if (TextUtils.isEmpty(category.getName())) {
            return ResponseResult.FAILED("分类名称不可以为空.");
        }
        if (TextUtils.isEmpty(category.getPinyin())) {
            return ResponseResult.FAILED("分类拼音不可以为空.");
        }
        if (TextUtils.isEmpty(category.getDescription())) {
            return ResponseResult.FAILED("分类描述不可以为空.");
        }

        //补全数据

        category.setId(idWorker.nextId() + "");
        category.setStatus("1");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        //保存数据
        categoryDao.save(category);
        return ResponseResult.SUCCESS("添加分类成功");
    }

    @Override
    public ResponseResult getCategory(String categoryId) {
        Category category = categoryDao.findOneById(categoryId);
        if (category == null) {
            return ResponseResult.FAILED("分类不存在.");
        }
        return ResponseResult.SUCCESS("获取分类成功.").setData(category);
    }

    @Override
    public ResponseResult listCategories() {
        //检查参数

        //创建条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime", "order");

        //判断用户角色：普通用户/未登录用户：只能获取正常的category
        //管理员能获取所有
        SobUser sobUser = userService.checkSobUser();
        List<Category> categories;
        if (sobUser == null|| !Constants.User.ROLE_ADMIN.equals(sobUser.getRoles())) {
            // 只能获取正常的category
            categories = categoryDao.listCategoriesByState("1");
        }else{
            //查询
            categories= categoryDao.findAll(sort);
        }

        return ResponseResult.SUCCESS("获取分类列表成功").setData(categories);
    }


    @Override
    public ResponseResult updateCategory(String categoryId,
                                         Category category) {
        //第一步是找出来
        Category categoryFromDb = categoryDao.findOneById(categoryId);
        if (categoryFromDb == null) {
            return ResponseResult.FAILED("分类不存在.");
        }
        //第二步是对内容判断，有些字段是不可以为空的
        String name = category.getName();
        if (!TextUtils.isEmpty(name)) {
            categoryFromDb.setName(name);
        }
        String pinyin = category.getPinyin();
        if (!TextUtils.isEmpty(pinyin)) {
            categoryFromDb.setPinyin(pinyin);
        }
        String description = category.getDescription();
        if (!TextUtils.isEmpty(description)) {
            categoryFromDb.setDescription(description);
        }
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setUpdateTime(new Date());
        categoryDao.save(categoryFromDb);

        return ResponseResult.SUCCESS("更新成功");
    }

    @Override
    public ResponseResult deleteCategory(String categoryId) {
        int result = categoryDao.deleteCategoryByUpdateState(categoryId);
        if (result == 0) {
            return ResponseResult.FAILED("该分类不存在.");
        }
        return ResponseResult.SUCCESS("删除分类成功.");
    }
}
