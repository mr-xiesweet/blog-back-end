package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.xie.pojo.Setting;


public interface SettingDao extends JpaRepository<Setting,String>, JpaSpecificationExecutor<Setting> {

    Setting findOneByKey(String key);
}
