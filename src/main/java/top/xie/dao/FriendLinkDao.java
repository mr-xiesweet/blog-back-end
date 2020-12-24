package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.FriendLink;

import java.util.List;

public interface FriendLinkDao extends JpaRepository<FriendLink,String>, JpaSpecificationExecutor<FriendLink> {

    FriendLink findOneById(String id);

    int deleteAllById(String friendLinkId);

    @Query(nativeQuery = true,value = "select * from `tb_friends` where `state`=? ")
    List<FriendLink> listFriendLinkByState(String s);
}
