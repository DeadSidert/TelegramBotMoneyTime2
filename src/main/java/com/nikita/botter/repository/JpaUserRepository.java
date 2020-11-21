package com.nikita.botter.repository;

import com.nikita.botter.model.Usr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<Usr, Integer> {

    @Override
    Optional<Usr> findById(Integer integer);

    @Override
    List<Usr> findAll();

    @Override
    <S extends Usr> S save(S s);

    @Override
    void deleteById(Integer integer);

    @Query("SELECT count(u) FROM Usr u WHERE u.referId=:usr_id")
    int countPartners(@Param("usr_id") int id);

    @Query("SELECT u FROM Usr u WHERE u.bonus=true ")
    List<Usr> getAllNotBonus();
}
