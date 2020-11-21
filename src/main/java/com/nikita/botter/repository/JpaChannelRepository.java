package com.nikita.botter.repository;

import com.nikita.botter.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaChannelRepository extends JpaRepository<Channel,String> {

    @Query("SELECT c FROM Channel c WHERE c.start=:start")
    public List<Channel> findAllByStart(@Param("start") boolean start);
}
