package br.edu.ceulp.tccapi;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import br.edu.ceulp.tccapi.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.profiles.active=test","app.limits.api=10000","app.limits.auth=10000","spring.datasource.url=jdbc:h2:mem:tcc;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="})
@AutoConfigureMockMvc
class SegurancaIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UsuarioRepository users; @Autowired TarefaRepository tarefas;
 @Autowired JwtEncoder encoder; @Autowired PasswordEncoder passwords;
 String a,b; long owner;
 String conta(String email)throws Exception{
  String body="{\"email\":\""+email+"\",\"senha\":\"SenhaTeste123!\"}";
  mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("senhaHash").doesNotExist());
  String response=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  return json.readTree(response).get("accessToken").asText();
 }
 @BeforeEach void preparar()throws Exception{tarefas.deleteAll();users.deleteAll();a=conta("a@example.test");b=conta("b@example.test");owner=users.findByEmail("a@example.test").orElseThrow().getId();}
 String dados="{\"titulo\":\"Tarefa A\",\"descricao\":\"Dado ficticio\",\"status\":\"PENDENTE\"}";
 long criar()throws Exception{return json.readTree(mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+a).contentType(MediaType.APPLICATION_JSON).content(dados)).andExpect(status().isCreated()).andExpect(jsonPath("usuarioId").value(owner)).andReturn().getResponse().getContentAsString()).get("id").asLong();}
 @Test void semToken()throws Exception{mvc.perform(get("/api/tarefas")).andExpect(status().isUnauthorized());}
 @Test void tokenInvalido()throws Exception{mvc.perform(get("/api/tarefas").header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized());}
 @Test void assinaturaAdulterada()throws Exception{String t=a.substring(0,a.lastIndexOf('.')+1)+"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";mvc.perform(get("/api/tarefas").header("Authorization","Bearer "+t)).andExpect(status().isUnauthorized());}
 String token(String issuer,Instant expiry){return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),JwtClaimsSet.builder().issuer(issuer).subject(""+owner).issuedAt(Instant.now().minusSeconds(3600)).expiresAt(expiry).build())).getTokenValue();}
 @Test void expirado()throws Exception{mvc.perform(get("/api/tarefas").header("Authorization","Bearer "+token("tcc-api",Instant.now().minusSeconds(300)))).andExpect(status().isUnauthorized());}
 @Test void emissorInvalido()throws Exception{mvc.perform(get("/api/tarefas").header("Authorization","Bearer "+token("outro",Instant.now().plusSeconds(300)))).andExpect(status().isUnauthorized());}
 @Test void loginIncorreto()throws Exception{mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.test\",\"senha\":\"SenhaErrada123\"}")).andExpect(status().isUnauthorized());}
 @Test void senhaProtegida(){String hash=users.findById(owner).orElseThrow().getSenhaHash();assertNotEquals("SenhaTeste123!",hash);assertTrue(passwords.matches("SenhaTeste123!",hash));}
 @Test void duplicado()throws Exception{mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"A@example.test\",\"senha\":\"SenhaTeste123!\"}")).andExpect(status().isConflict());}
 @Test void donoConsulta()throws Exception{long id=criar();mvc.perform(get("/api/tarefas/"+id).header("Authorization","Bearer "+a)).andExpect(status().isOk());}
 @Test void listagemIsolada()throws Exception{criar();mvc.perform(get("/api/tarefas").header("Authorization","Bearer "+b)).andExpect(status().isOk()).andExpect(content().json("[]"));}
 @Test void outroNaoConsulta()throws Exception{long id=criar();mvc.perform(get("/api/tarefas/"+id).header("Authorization","Bearer "+b)).andExpect(status().isNotFound());}
 @Test void outroNaoAltera()throws Exception{long id=criar();mvc.perform(put("/api/tarefas/"+id).header("Authorization","Bearer "+b).contentType(MediaType.APPLICATION_JSON).content(dados.replace("Tarefa A","Invadida"))).andExpect(status().isNotFound());assertEquals("Tarefa A",tarefas.findById(id).orElseThrow().getTitulo());}
 @Test void outroNaoExclui()throws Exception{long id=criar();mvc.perform(delete("/api/tarefas/"+id).header("Authorization","Bearer "+b)).andExpect(status().isNotFound());assertTrue(tarefas.existsById(id));}
 @Test void donoAlteraEExclui()throws Exception{long id=criar();mvc.perform(put("/api/tarefas/"+id).header("Authorization","Bearer "+a).contentType(MediaType.APPLICATION_JSON).content(dados.replace("Tarefa A","Alterada"))).andExpect(status().isOk()).andExpect(jsonPath("titulo").value("Alterada"));mvc.perform(delete("/api/tarefas/"+id).header("Authorization","Bearer "+a)).andExpect(status().isNoContent());assertFalse(tarefas.existsById(id));}
 @Test void naoForjaProprietario()throws Exception{mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+a).contentType(MediaType.APPLICATION_JSON).content(dados.replace("}",",\"usuarioId\":999}"))).andExpect(status().isBadRequest());assertEquals(0,tarefas.count());}
 @Test void naoTrocaProprietario()throws Exception{long id=criar();mvc.perform(put("/api/tarefas/"+id).header("Authorization","Bearer "+a).contentType(MediaType.APPLICATION_JSON).content(dados.replace("}",",\"usuarioId\":999}"))).andExpect(status().isBadRequest());assertEquals(owner,tarefas.findById(id).orElseThrow().getUsuarioId());}
 @Test void cabecalhoNosniff()throws Exception{mvc.perform(get("/api/tarefas").header("Authorization","Bearer "+a)).andExpect(header().string("X-Content-Type-Options","nosniff"));}
}
