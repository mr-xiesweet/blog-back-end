package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.SobUser;

public interface UserDao extends JpaRepository<SobUser,String> , JpaSpecificationExecutor<SobUser> {


    /**
     * 根据用户名userName查找当前用户是否存在
     * @param userName
     * @return
     */
    SobUser findOneByUserName(String userName);

    /**
     * 根据邮箱地址来找当前用户
     * @param email
     * @return
     */
    SobUser findOneByEmail(String email);

    /**
     * 根据userId 获取用户
     * @param userId
     * @return
     */
    SobUser findOneById(String userId);

    /**
     * 通过修改用户的状态来删除用户
     * @param state
     * @param userId
     * @return
     */
    @Modifying
    @Query(nativeQuery = true,value = "UPDATE `tb_user` SET `state` = ? WHERE `id` = ?")
    int deleteUserByState(String state,String userId);


    @Modifying
    @Query(nativeQuery = true,value = "UPDATE `tb_user` SET `password` = ? WHERE `email` = ?")
    int updatePasswordByEmail(String encode, String email);

    @Modifying
    @Query(nativeQuery = true, value = "update `tb_user` set `email` = ? where `id` = ?")
    int updateEmailById(String email, String id);
}
