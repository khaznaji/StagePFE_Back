package com.example.backend.Repository;


import com.example.backend.Entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface ChatRepository extends JpaRepository<Chat,Long> {

    Chat findByName(String name);

    @Query("SELECT DISTINCT c FROM Chat c WHERE c.name like %:user% ")
    List<Chat> findByPartecipant(String user);
}