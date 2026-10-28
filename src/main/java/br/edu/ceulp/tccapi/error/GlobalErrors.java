package br.edu.ceulp.tccapi.error;
import java.util.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
@RestControllerAdvice
public class GlobalErrors {
 private final ApiErrors errors;
 private static final Logger log=LoggerFactory.getLogger(GlobalErrors.class);
 public GlobalErrors(ApiErrors errors){this.errors=errors;}
 @ExceptionHandler(Exception.class)
 public ResponseEntity<ApiErrors.Body> handle(Exception ex){
  int status=500;Map<String,String> fields=new TreeMap<>();
  if(ex instanceof MethodArgumentNotValidException e){
   status=400;
   e.getBindingResult().getFieldErrors().forEach(f->fields.put(f.getField(),"Valor invalido"));
  }else if(ex instanceof org.springframework.http.converter.HttpMessageNotReadableException || ex instanceof org.springframework.beans.TypeMismatchException){status=400;}
  else if(ex instanceof HandlerMethodValidationException){status=400;}
  else if(ex instanceof ResponseStatusException e){status=e.getStatusCode().value();}
  else if(ex instanceof DataIntegrityViolationException){status=409;}
  else if(ex instanceof ErrorResponse e){status=e.getStatusCode().value();}
  // Nunca registra corpo, senha, token ou mensagem de excecao do cliente.
  if(status>=500)log.error("Falha interna tratada: {}",ex.getClass().getSimpleName());
  HttpHeaders headers=new HttpHeaders();
  if(ex instanceof ErrorResponse e)headers.putAll(e.getHeaders());
  headers.setCacheControl(CacheControl.noStore());
  return ResponseEntity.status(status).headers(headers).body(errors.body(status,fields));
 }
}
