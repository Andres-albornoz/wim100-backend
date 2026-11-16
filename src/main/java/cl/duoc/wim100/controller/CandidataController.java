package cl.duoc.wim100.controller;

import cl.duoc.wim100.dto.EstadoRequest;
import cl.duoc.wim100.dto.CandidataRequest;
import cl.duoc.wim100.Model.Candidata;
import cl.duoc.wim100.Service.CandidataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Candidatas")
public class CandidataController {

    private final CandidataService service;

    public CandidataController(CandidataService service) {
        this.service = service;
    }

    @GetMapping
    public List<Candidata> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Candidata obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Candidata> crear(@Valid @RequestBody CandidataRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}")
    public Candidata actualizar(@PathVariable Long id, @Valid @RequestBody CandidataRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/estado")
    public Candidata cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
