package com.nikita.botter.repository;

import com.nikita.botter.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT p FROM Payment p where p.successful=false")
    List<Payment> findAllByNotSuccessful();
}
