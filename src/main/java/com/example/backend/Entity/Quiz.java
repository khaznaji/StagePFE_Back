package com.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Getter
@Setter
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long qid;

    private String title;

    private String description;

    private String maxMarks;

    private String numberOfQuestions;

    private boolean active = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private Poste poste;

    @OneToMany(mappedBy = "quiz",cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Question> questions=new HashSet<>();
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Candidature> candidatures = new HashSet<>();

    public Quiz() {
    }

    public Long getQid() {
        return qid;
    }

    public void setQid(Long qid) {
        this.qid = qid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(String maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(String numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public Set<Question> getQuestions() {
        return questions;
    }
    @Transient // Cette annotation évite de persister cette propriété en base de données
    private Long candidatureId;
    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }
}

