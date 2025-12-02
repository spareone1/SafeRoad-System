package com.teamroute.saferoad.repository;

import com.teamroute.saferoad.domain.SocialUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SocialUserInfoRepository extends JpaRepository<SocialUserInfo, Long> {
    Optional<SocialUserInfo> findByProviderAndPuserid(String provider, String puserid);
}
