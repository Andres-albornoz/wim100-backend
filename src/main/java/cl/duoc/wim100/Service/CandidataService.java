package cl.duoc.wim100.Service;

import cl.duoc.wim100.dto.CandidataRequest;
import cl.duoc.wim100.Exception.CorreoDuplicadoException;
import cl.duoc.wim100.Exception.RecursoNoEncontradoException;
import cl.duoc.wim100.Model.EstadoCandidata;
import cl.duoc.wim100.Model.Candidata;
import cl.duoc.wim100.Repository.CandidataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidataService {

    private final CandidataRepository repository;

    public CandidataService(CandidataRepository repository) {
        this.repository = repository;
    }

    public List<Candidata> listar() {
        return repository.findAllByOrderByFechaRegistroDesc();
    }

    public Candidata obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el Candidata " + id));
    }

    public Candidata crear(CandidataRequest req) {
        // Regla de negocio: no se registra dos veces el mismo correo
        if (repository.existsByCorreoIgnoreCase(req.correo().trim())) {
            throw new CorreoDuplicadoException("Ya existe una candidatura con este correo");
        }
        Candidata p = new Candidata();
        copiarDatos(req, p);
        p.setEstado(EstadoCandidata.POSTULADA);
        p.setFechaRegistro(System.currentTimeMillis());
        p.setRegistradaPor(req.registradaPor().trim());
        return repository.save(p);
    }

    public Candidata actualizar(Long id, CandidataRequest req) {
        Candidata p = obtener(id);
        if (repository.existsByCorreoIgnoreCaseAndIdNot(req.correo().trim(), id)) {
            throw new CorreoDuplicadoException("Ya existe una candidatura con este correo");
        }
        copiarDatos(req, p); // no cambia estado, fecha ni quién la registró
        return repository.save(p);
    }

    public Candidata cambiarEstado(Long id, EstadoCandidata estado) {
        Candidata p = obtener(id);
        p.setEstado(estado);
        return repository.save(p);
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id)); // lanza 404 si no existe
    }

    private void copiarDatos(CandidataRequest r, Candidata p) {
        p.setNombre(r.nombre().trim().replaceAll("\\s+", " "));
        p.setCorreo(r.correo().trim().toLowerCase());
        p.setTelefono(r.telefono().trim());
        p.setArea(r.area().trim());
        p.setAniosExperiencia(r.aniosExperiencia());
        p.setResumenExperiencia(r.resumenExperiencia().trim());
        p.setLinkedinUrl(r.linkedinUrl().trim());
        p.setCvNombre(r.cvNombre());
    }
}
