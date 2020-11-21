package com.nikita.botter.service;

import com.nikita.botter.model.Payment;
import com.nikita.botter.repository.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {


    private final JpaPaymentRepository paymentRepository;

    public List<Payment> findAllByNotSuccessful(){
        return paymentRepository.findAllByNotSuccessful();
    }

    public Payment update(Payment payment){
        return paymentRepository.save(payment);
    }

    public Payment findById(int id){
        return paymentRepository.findById(id).get();
    }
}
