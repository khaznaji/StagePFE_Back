package com.example.backend.Service;

import com.example.backend.Entity.*;
import com.example.backend.Repository.GroupsRespository;
import com.example.backend.Repository.SessionFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.Optional;

@Service
public class SessionFormationService {

    @Autowired
    private SessionFormationRepository sessionFormationRepository;
    @Autowired
    private GroupsRespository groupsRepository;


    public SessionFormation addSessionFormation(Long groupId, String dateDebut, String dateFin, String roomId, ModaliteSession modaliteSession) {
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));

        SessionFormation sessionFormation = new SessionFormation();
        sessionFormation.setGroup(group);
        sessionFormation.setDateDebut(dateDebut);
        sessionFormation.setDateFin(dateFin);
        sessionFormation.setRoomId(roomId);
        sessionFormation.setModaliteSession(modaliteSession);
        return sessionFormationRepository.save(sessionFormation);
    }
}

