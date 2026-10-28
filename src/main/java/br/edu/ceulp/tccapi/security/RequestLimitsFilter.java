package br.edu.ceulp.tccapi.security;
import br.edu.ceulp.tccapi.error.ApiErrors;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/** Janela fixa por IP de transporte, limitada em memoria; instancia unica de laboratorio. */
@Component
public class RequestLimitsFilter extends OncePerRequestFilter {
 private final ApiErrors errors;private final int apiMax,authMax,maxClients,maxBody;
 private final long window;private final LongSupplier clock;
 private final Map<String,Bucket> clients=new HashMap<>();
 private static class Bucket {long until;int api,auth;Bucket(long until){this.until=until;}}
 @org.springframework.beans.factory.annotation.Autowired
 public RequestLimitsFilter(ApiErrors errors,
  @Value("${app.limits.api:120}") int api,@Value("${app.limits.auth:10}") int auth,
  @Value("${app.limits.window-seconds:60}") int seconds,@Value("${app.limits.max-clients:10000}") int clients,
  @Value("${app.limits.max-body-bytes:16384}") int body){this(errors,api,auth,seconds,clients,body,System::nanoTime);}
 public RequestLimitsFilter(ApiErrors errors,int api,int auth,int seconds,int clients,int body,LongSupplier clock){
  if(api<1||auth<1||seconds<1||clients<1||body<1)throw new IllegalArgumentException("Limites devem ser positivos");
  this.errors=errors;apiMax=api;authMax=auth;window=seconds*1_000_000_000L;maxClients=clients;maxBody=body;this.clock=clock;
 }
 private synchronized long allow(String ip,boolean auth){
  long now=clock.getAsLong();clients.entrySet().removeIf(e->e.getValue().until<=now);
  Bucket b=clients.get(ip);
  if(b==null){if(clients.size()>=maxClients)return -1;b=new Bucket(now+window);clients.put(ip,b);}
  if(b.api>=apiMax||(auth&&b.auth>=authMax))return Math.max(1,(b.until-now+999_999_999L)/1_000_000_000L);
  b.api++;if(auth)b.auth++;return 0;
 }
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  // X-Forwarded-For nao e confiavel neste modo direto, sem proxy configurado.
  String path=req.getServletPath().isEmpty()?req.getRequestURI().substring(req.getContextPath().length()):req.getServletPath();
  long retry=allow(req.getRemoteAddr(),path.startsWith("/api/auth/"));
  if(retry!=0){res.setHeader("Retry-After",Long.toString(retry<0?60:retry));errors.write(res,retry<0?503:429);return;}
  if(req.getContentLengthLong()>maxBody){errors.write(res,413);return;}
  byte[] bytes=req.getInputStream().readNBytes(maxBody+1);
  if(bytes.length>maxBody){errors.write(res,413);return;}
  HttpServletRequestWrapper wrapped=new HttpServletRequestWrapper(req){
   @Override public int getContentLength(){return bytes.length;}
   @Override public long getContentLengthLong(){return bytes.length;}
   @Override public ServletInputStream getInputStream(){
    ByteArrayInputStream in=new ByteArrayInputStream(bytes);
    return new ServletInputStream(){public int read(){return in.read();}public boolean isFinished(){return in.available()==0;}public boolean isReady(){return true;}public void setReadListener(ReadListener l){throw new UnsupportedOperationException();}};
   }
   @Override public BufferedReader getReader(){return new BufferedReader(new InputStreamReader(getInputStream(),java.nio.charset.StandardCharsets.UTF_8));}
  };
  chain.doFilter(wrapped,res);
 }
}
