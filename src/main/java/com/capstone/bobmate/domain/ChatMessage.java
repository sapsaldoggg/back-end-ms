package com.capstone.bobmate.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class ChatMessage {

    @Id @GeneratedValue
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;      // 전송할 파티 방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private String member;      // 전송자

    private String message;     // 전송 메시지

    @CreatedDate
    private LocalDateTime sendTime; // 보내는 시간

}
