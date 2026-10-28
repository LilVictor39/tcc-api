package br.edu.ceulp.tccapi.controller;
import br.edu.ceulp.tccapi.model.Usuario;
import br.edu.ceulp.tccapi.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.Locale;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UsuarioRepository users; private final PasswordEncoder passwords; private final JwtEncoder encoder;
 public AuthController(UsuarioRepository u,PasswordEncoder p,JwtEncoder e){users=u;passwords=p;encoder=e;}
 public record Credenciais(@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(min=8,max=72) String senha){}
 public record Conta(Long id,String email){}
 public record Token(String accessToken,String tokenType,long expiresIn){}
 @PostMapping("/cadastro") public ResponseEntity<Conta> cadastro(@Valid @RequestBody Credenciais c){
  if(c.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Senha excede 72 bytes");
  try {var u=users.saveAndFlush(new Usuario(c.email().trim().toLowerCase(Locale.ROOT),passwords.encode(c.senha())));
   return ResponseEntity.status(201).body(new Conta(u.getId(),u.getEmail()));
  }catch(DataIntegrityViolationException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Cadastro indisponivel");}
 }
 @PostMapping("/login") public ResponseEntity<Token> login(@Valid @RequestBody Credenciais c){
  if(c.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  var u=users.findByEmail(c.email().trim().toLowerCase(Locale.ROOT));
  if(u.isEmpty() || !passwords.matches(c.senha(),u.get().getSenhaHash()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais invalidas");
  Instant now=Instant.now();
  var claims=JwtClaimsSet.builder().issuer("tcc-api").subject(u.get().getId().toString()).issuedAt(now).expiresAt(now.plusSeconds(900)).build();
  var jwt=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims));
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Token(jwt.getTokenValue(),"Bearer",900));
 }
}
