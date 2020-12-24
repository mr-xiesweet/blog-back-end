package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.xie.pojo.SobUserNoPassword;

public interface UserNoPasswordDao extends JpaRepository<SobUserNoPassword,String> , JpaSpecificationExecutor<SobUserNoPassword> {


}
