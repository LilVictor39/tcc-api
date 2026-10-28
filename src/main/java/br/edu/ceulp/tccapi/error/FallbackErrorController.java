package br.edu.ceulp.tccapi.error;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class FallbackErrorController implements ErrorController {
 private final ApiErrors errors;
 public FallbackErrorController(ApiErrors errors){this.errors=errors;}
 @RequestMapping("/error") public ResponseEntity<ApiErrors.Body> error(HttpServletRequest request){
  Object code=request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
  int status=code instanceof Integer?(Integer)code:500;
  return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(errors.body(status,Map.of()));
 }
}
