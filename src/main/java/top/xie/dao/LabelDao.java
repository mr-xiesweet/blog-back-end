package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.Labels;

public interface LabelDao extends JpaRepository<Labels,String>, JpaSpecificationExecutor<Labels> {
    Labels findOneByName(String name);

    Labels findOneById(String id);

    @Modifying
    @Query(value = "UPDATE `tb_labels` SET `count` = `count` +1 WHERE `name` = ?",nativeQuery = true)
    int updateCountByName(String labelName);
}
