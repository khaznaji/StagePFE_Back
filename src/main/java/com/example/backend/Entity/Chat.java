package com.example.backend.Entity;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name="Chat")
@Table(name = "CHATS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private long chat_id;

    @Column(name = "name")
    private String name;

    @Column
    private LocalDateTime lastMessage;

    public Chat(String name) {
        this.name = name;
    }
}