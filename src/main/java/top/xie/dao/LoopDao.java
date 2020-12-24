package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.Looper;

import java.util.List;

public interface LoopDao extends JpaRepository<Looper,String>, JpaSpecificationExecutor<Looper> {

    Looper findOneById(String loopId);

    @Query(value = "select * from `tb_looper` where `state`=?",nativeQuery = true)
    List<Looper> listFriendLinkByState(String s);
}
