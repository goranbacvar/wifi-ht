package com.wifiadmin.repository;

import com.wifiadmin.entity.WifiConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WifiConfigurationRepository extends JpaRepository<WifiConfigurationEntity, String> {
}
