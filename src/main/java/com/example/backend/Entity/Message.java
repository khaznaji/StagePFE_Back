package com.example.backend.Entity;


import lombok.*;

import javax.persistence.*;

@Entity(name="Message")
@Table(name = "MESSAGES")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Message {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ms_id")
    private long ms_id;

    @Column(name = "chat_id")
    private long chat_id;

    @Column(name = "sender")
    private String sender;

    @Column(name = "id")
    private String id;

    @Column(name = "t_stamp")
    private String t_stamp;

    @Column(name = "content")
    private String content;
}