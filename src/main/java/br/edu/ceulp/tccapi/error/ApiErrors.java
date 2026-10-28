package br.edu.ceulp.tccapi.error;
import java.time.Instant;
import java.util.Map;
import jakarta.servlet.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
@Component
public class ApiErrors {
 public record Body(Instant timestamp,int status,String error,String message,Map<String,String> fields) {}
 public Body body(int status,Map<String,String> fields){
  String message=switch(status){
   case 400 -> "Requisicao invalida"; case 401 -> "Autenticacao necessaria ou invalida";
   case 403 -> "Acesso negado"; case 404 -> "Recurso nao encontrado";
   case 405 -> "Metodo nao permitido"; case 409 -> "Conflito de dados";
   case 413 -> "Corpo da requisicao excede o limite"; case 415 -> "Tipo de conteudo nao suportado";
   case 429 -> "Limite de requisicoes excedido"; case 503 -> "Servico temporariamente indisponivel";
   default -> status>=500?"Erro interno do servidor":"Requisicao rejeitada";
  };
  HttpStatus s=HttpStatus.resolve(status);
  return new Body(Instant.now(),status,s==null?"Error":s.getReasonPhrase(),message,fields);
 }
 private final ObjectMapper mapper;
 public ApiErrors(ObjectMapper mapper){this.mapper=mapper;}
 public void write(HttpServletResponse response,int status)throws java.io.IOException{
  response.setStatus(status);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
  response.setHeader("Cache-Control","no-store");mapper.writeValue(response.getOutputStream(),body(status,Map.of()));
 }
}
