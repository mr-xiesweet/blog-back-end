package top.xie.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.pojo.Category;
import top.xie.response.ResponseResult;
import top.xie.services.ICategoryService;

/**
 * 管理中心 分类Api
 * 需要管理员权限
 */

@RestController
@RequestMapping("/admin/category")
public class CategoryAdminApi {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 添加分类
     * 需要管理员权限
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PostMapping
    public ResponseResult addCategory(@RequestBody Category category){

        return categoryService.addCategory(category);
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{categoryId}")
    public ResponseResult deleteCategory(@PathVariable("categoryId") String categoryId) {

        return categoryService.deleteCategory(categoryId);
    }

    /**
     * 更新分类
     *
     * @param categoryId
     * @param category
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/{categoryId}")
    public ResponseResult updateCategory(@PathVariable("categoryId") String categoryId,
                                         @RequestBody Category category){
        return categoryService.updateCategory(categoryId, category);
    }

    /**
     * 获取分类
     *
     * 使用的case:获取一下 ，填充弹窗
     * 不获取也是可以的，从列表里获取数据
     *
     * 管理权限
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/{categoryId}")
    public ResponseResult getCategory(@PathVariable("categoryId") String categoryId){

        return categoryService.getCategory(categoryId);
    }

    /**
     * 获取分类列表
     * <p>
     * 权限：管理员权限
     *
     * @return
     */
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list")
    public ResponseResult listCategories() {
        return categoryService.listCategories();
    }

}
