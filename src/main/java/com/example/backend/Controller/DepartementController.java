package com.example.backend.Controller;
import com.example.backend.Entity.Departement;
import com.example.backend.Service.IDepartementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Departement")
public class DepartementController {
    @Autowired
    private IDepartementService departementService;

    @PostMapping
    public ResponseEntity<Departement> addDepartement(@RequestBody Departement departement) {
        Departement newDepartement = departementService.addDepartement(departement);
        return new ResponseEntity<>(newDepartement, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Long id) {
        Departement departement = departementService.getDepartementById(id);
        return departement != null
                ? new ResponseEntity<>(departement, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<Departement>> getAllDepartements() {
        List<Departement> departements = departementService.getAllDepartements();
        return new ResponseEntity<>(departements, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Departement> updateDepartement(@PathVariable Long id, @RequestBody Departement newDepartement) {
        Departement updatedDepartement = departementService.updateDepartement(id, newDepartement);
        return updatedDepartement != null
                ? new ResponseEntity<>(updatedDepartement, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Long id) {
        departementService.deleteDepartement(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
