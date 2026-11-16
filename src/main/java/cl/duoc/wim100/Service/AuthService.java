package cl.duoc.wim100.Service;

import cl.duoc.wim100.dto.LoginRequest;
import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.Exception.CredencialesInvalidasException;
import cl.duoc.wim100.Model.Usuario;
import cl.duoc.wim100.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = repository.findByUsuario(req.usuario().trim().toLowerCase())
                // matches() compara la contraseña escrita con el hash guardado
                .filter(x -> encoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));
        return new LoginResponse(u.getId(), u.getUsuario(), u.getNombre(), u.getRol());
    }
}
