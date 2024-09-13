package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.UserNetworkFilter;
import io.playce.roro.jpa.entity.pk.UserNetworkFilterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNetworkFilterRepository extends JpaRepository<UserNetworkFilter, UserNetworkFilterId>, JpaSpecificationExecutor<UserNetworkFilter> {
}