package br.edu.ceulp.tccapi;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.profiles.active=test","app.limits.api=10000","app.limits.auth=10000","spring.datasource.url=jdbc:h2:mem:e4;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="})
@AutoConfigureMockMvc @Import(Entrega4IntegrationTest.FalhaControlada.class)
class Entrega4IntegrationTest {
 @RestController static class FalhaControlada {
  @GetMapping("/teste/falha") String falha(){throw new IllegalStateException("SEGREDO-NAO-EXIBIR");}
 }
 @Autowired MockMvc mvc;@Autowired ObjectMapper json;
 static String token;
 @BeforeEach void conta()throws Exception{
  if(token!=null)return;
  String c="{\"email\":\"validacao@example.test\",\"senha\":\"SenhaTeste123!\"}";
  mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(c)).andExpect(status().isCreated());
  token=json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(c)).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
 }
 ResultActions tarefa(String title,String desc,String state)throws Exception{
  return mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("titulo",title,"descricao",desc,"status",state))));
 }
 @Test void tituloVazio()throws Exception{tarefa("   ","ok","PENDENTE").andExpect(status().isBadRequest()).andExpect(jsonPath("fields.titulo").value("Valor invalido"));}
 @Test void tituloLongo()throws Exception{tarefa("a".repeat(256),"ok","PENDENTE").andExpect(status().isBadRequest());}
 @Test void descricaoLonga()throws Exception{tarefa("ok","a".repeat(1001),"PENDENTE").andExpect(status().isBadRequest());}
 @Test void limitesExatos()throws Exception{tarefa("a".repeat(255),"a".repeat(1000),"EM_ANDAMENTO").andExpect(status().isCreated());}
 @Test void enumInvalido()throws Exception{tarefa("ok","ok","FORA_DO_ENUM").andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400));}
 @Test void nulosObrigatorios()throws Exception{mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":null,\"status\":null}")).andExpect(status().isBadRequest());}
 @Test void jsonMalformado()throws Exception{mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{ERRADO")).andExpect(status().isBadRequest());}
 @Test void campoDesconhecido()throws Exception{mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"a\",\"status\":\"PENDENTE\",\"id\":88}")).andExpect(status().isBadRequest());}
 @Test void idNegativo()throws Exception{mvc.perform(get("/api/tarefas/-1").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());}
 @Test void idTexto()throws Exception{mvc.perform(get("/api/tarefas/abc").header("Authorization","Bearer "+token)).andExpect(status().isBadRequest());}
 @Test void emailInvalido()throws Exception{mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"sem-email\",\"senha\":\"SenhaTeste123!\"}")).andExpect(status().isBadRequest());}
 @Test void emailLongo()throws Exception{mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email","a".repeat(250)+"@ex.test","senha","SenhaTeste123!")))).andExpect(status().isBadRequest());}
 @Test void senhaCurta()throws Exception{mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"novo@example.test\",\"senha\":\"abc\"}")).andExpect(status().isBadRequest());}
 @Test void senhaUnicodeExcede72Bytes()throws Exception{for(String path:new String[]{"cadastro","login"})mvc.perform(post("/api/auth/"+path).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email","novo@example.test","senha","á".repeat(37))))).andExpect(status().isBadRequest());}
 @Test void corpoGrande()throws Exception{tarefa("ok","a".repeat(17000),"PENDENTE").andExpect(status().isPayloadTooLarge());}
 @Test void tipoIncorreto()throws Exception{mvc.perform(post("/api/tarefas").header("Authorization","Bearer "+token).contentType(MediaType.TEXT_PLAIN).content("teste")).andExpect(status().isUnsupportedMediaType());}
 @Test void metodoIncorreto()throws Exception{mvc.perform(patch("/api/tarefas/1").header("Authorization","Bearer "+token)).andExpect(status().isMethodNotAllowed());}
 @Test void autenticacaoPadronizada()throws Exception{for(String auth:new String[]{"","Bearer invalido"})mvc.perform(get("/api/tarefas").header("Authorization",auth)).andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401)).andExpect(header().string("WWW-Authenticate","Bearer"));}
 @Test void erroInternoSemVazamento()throws Exception{
  String body=mvc.perform(get("/teste/falha").header("Authorization","Bearer "+token)).andExpect(status().isInternalServerError()).andExpect(jsonPath("message").value("Erro interno do servidor")).andExpect(jsonPath("trace").doesNotExist()).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();
  assertFalse(body.contains("SEGREDO"));assertFalse(body.contains("Exception"));
 }
}
