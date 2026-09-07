package com.foodtraceability.repository;

import com.foodtraceability.entity.SmartContractState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmartContractStateRepository extends JpaRepository<SmartContractState, Long> {

    Optional<SmartContractState> findByContractIdAndStateKey(String contractId, String stateKey);
}
