package com.pharmacy.repository;

import com.pharmacy.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByOrderBySentAtAsc();
    List<ChatMessage> findByIsAnnouncementTrue();
}
