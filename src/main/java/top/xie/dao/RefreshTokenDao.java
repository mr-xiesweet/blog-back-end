package top.xie.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import top.xie.pojo.RefreshToken;

public interface RefreshTokenDao extends JpaRepository<RefreshToken,String>, JpaSpecificationExecutor<RefreshToken> {

    RefreshToken findOneByTokenKey(String tokenKey);

    RefreshToken findOneByUserId(String userId);

    RefreshToken findOneByMobileTokenKey(String mobileTokenKey);


    @Modifying
    @Query(value = "update `tb_refresh_token` set `mobile_token_key` =' ' where `mobile_token_key` = ?",nativeQuery = true)
    void deleteMobileTokenKey(String tokenKey);

    @Modifying
    @Query(value = "update `tb_refresh_token` set `token_key` =' ' where `token_key` = ?",nativeQuery = true)
    void deletePcTokenKey(String tokenKey);


}
