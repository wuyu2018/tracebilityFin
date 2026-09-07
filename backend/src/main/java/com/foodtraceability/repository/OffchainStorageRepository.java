package com.foodtraceability.repository;

import com.foodtraceability.entity.OffchainStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface OffchainStorageRepository extends JpaRepository<OffchainStorage, Long> {
    
    Optional<OffchainStorage> findByFoodId(String foodId);
    
    boolean existsByFoodId(String foodId);
    
    @Query("SELECT o FROM OffchainStorage o WHERE o.foodId = :foodId AND o.isDeleted = false")
    Optional<OffchainStorage> findActiveByFoodId(@Param("foodId") String foodId);
    
    @Query("SELECT o FROM OffchainStorage o WHERE o.ownerAgentId = :ownerId ORDER BY o.createdAt DESC")
    java.util.List<OffchainStorage> findByOwnerId(@Param("ownerId") Long ownerId);

    List<OffchainStorage> findByStorageKey(String storageKey);
}
