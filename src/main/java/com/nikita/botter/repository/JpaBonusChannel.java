package com.nikita.botter.repository;

import com.nikita.botter.model.BonusChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBonusChannel extends JpaRepository<BonusChannel, Integer> {
}
