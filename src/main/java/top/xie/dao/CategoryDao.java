package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.Category;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category,String>, JpaSpecificationExecutor<Category> {
    Category findOneById(String categoryId);

    @Modifying
    @Query(nativeQuery = true,value = "update `tb_categories` set `status`= '0' where `id` = ?")
    int deleteCategoryByUpdateState(String categoryId);

    @Query(nativeQuery = true, value ="select * from `tb_categories` where `status`=? order by `create_time` DESC ")
    List<Category> listCategoriesByState(String status);
}
