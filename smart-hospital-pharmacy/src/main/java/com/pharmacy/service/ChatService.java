package com.pharmacy.service;

import com.pharmacy.entity.ChatMessage;
import com.pharmacy.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    // SSE emitters for real-time push
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public ChatMessage sendMessage(String senderName, String senderEmail,
                                    String senderRole, String message,
                                    boolean isAnnouncement) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderName(senderName);
        msg.setSenderEmail(senderEmail);
        msg.setSenderRole(senderRole);
        msg.setMessage(message);
        msg.setAnnouncement(isAnnouncement);
        msg.setMessageType(isAnnouncement ? ChatMessage.MessageType.ANNOUNCEMENT : ChatMessage.MessageType.TEXT);
        ChatMessage saved = chatMessageRepository.save(msg);

        // Push to all SSE subscribers
        pushToAll(saved);
        return saved;
    }

    private void pushToAll(ChatMessage msg) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                String payload = String.format("{\"id\":%d,\"sender\":\"%s\",\"role\":\"%s\",\"message\":\"%s\",\"time\":\"%s\",\"announcement\":%b}",
                        msg.getId(),
                        escapeJson(msg.getSenderName()),
                        msg.getSenderRole(),
                        escapeJson(msg.getMessage()),
                        msg.getSentAt() != null ? msg.getSentAt().toString() : "",
                        msg.isAnnouncement());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(Objects.requireNonNull(payload, "payload must not be null")));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public List<ChatMessage> getRecentMessages() {
        return chatMessageRepository.findTop100ByOrderBySentAtAsc();
    }
}
