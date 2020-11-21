package com.nikita.botter.repository;

import com.nikita.botter.model.ChannelCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaChannelCheckRepository extends JpaRepository<ChannelCheck, Integer> {

    @Query("SELECT c from ChannelCheck c WHERE c.userId=:userId")
    List<ChannelCheck> findAllByUserId(@Param("userId") Integer integer);
}
