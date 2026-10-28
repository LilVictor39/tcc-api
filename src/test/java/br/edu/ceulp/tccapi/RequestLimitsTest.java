package br.edu.ceulp.tccapi;
import br.edu.ceulp.tccapi.security.RequestLimitsFilter;
import br.edu.ceulp.tccapi.error.ApiErrors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
class RequestLimitsTest {
 AtomicLong now;RequestLimitsFilter filter;
 @BeforeEach void setup(){now=new AtomicLong();filter=new RequestLimitsFilter(new ApiErrors(new ObjectMapper().findAndRegisterModules()),3,2,60,2,32,now::get);}
 MockHttpServletResponse request(String ip,String path,String xff,byte[] body,boolean declared)throws Exception{
  MockHttpServletRequest req=new MockHttpServletRequest("POST",path);req.setRemoteAddr(ip);req.setServletPath(path);req.setContent(body);
  if(xff!=null)req.addHeader("X-Forwarded-For",xff);
  if(!declared)req=new MockHttpServletRequest("POST",path){
   {setRemoteAddr(ip);setServletPath(path);setContent(body);}
   public long getContentLengthLong(){return -1;}public int getContentLength(){return -1;}
  };
  MockHttpServletResponse res=new MockHttpServletResponse();filter.doFilter(req,res,(a,b)->((jakarta.servlet.http.HttpServletResponse)b).setStatus(204));return res;
 }
 int call(String ip,String path)throws Exception{return request(ip,path,null,new byte[0],true).getStatus();}
 @Test void limiteApi()throws Exception{for(int i=0;i<3;i++)assertEquals(204,call("a","/api/tarefas"));var r=request("a","/api/tarefas",null,new byte[0],true);assertEquals(429,r.getStatus());assertEquals("60",r.getHeader("Retry-After"));assertTrue(r.getContentAsString().contains("429"));}
 @Test void limiteAuthSeparado()throws Exception{assertEquals(204,call("a","/api/auth/login"));assertEquals(204,call("a","/api/auth/cadastro"));assertEquals(429,call("a","/api/auth/login"));assertEquals(204,call("a","/api/tarefas"));}
 @Test void janelaRecupera()throws Exception{for(int i=0;i<3;i++)call("a","/api/tarefas");now.set(59_000_000_000L);assertEquals("1",request("a","/api/tarefas",null,new byte[0],true).getHeader("Retry-After"));now.set(60_000_000_000L);assertEquals(204,call("a","/api/tarefas"));}
 @Test void ipsIndependentes()throws Exception{for(int i=0;i<3;i++)call("a","/api/tarefas");assertEquals(204,call("b","/api/tarefas"));}
 @Test void xffNaoBurla()throws Exception{for(int i=0;i<3;i++)request("a","/api/tarefas","ip"+i,new byte[0],true);assertEquals(429,request("a","/api/tarefas","novo-ip",new byte[0],true).getStatus());}
 @Test void memoriaLimitada()throws Exception{call("a","/");call("b","/");assertEquals(503,call("c","/"));now.set(60_000_000_000L);assertEquals(204,call("c","/"));}
 @Test void corpoComETamSemDeclarado()throws Exception{assertEquals(413,request("a","/",null,new byte[33],true).getStatus());assertEquals(413,request("a","/",null,new byte[33],false).getStatus());assertEquals(204,request("b","/",null,new byte[32],false).getStatus());}
 @Test void concorrenciaNaoUltrapassa()throws Exception{
  ExecutorService pool=Executors.newFixedThreadPool(8);AtomicInteger ok=new AtomicInteger();
  try{var tasks=new java.util.ArrayList<Callable<Void>>();for(int i=0;i<30;i++)tasks.add(()->{if(call("a","/api/tarefas")==204)ok.incrementAndGet();return null;});for(var result:pool.invokeAll(tasks))result.get();assertEquals(3,ok.get());}finally{pool.shutdownNow();}
 }
}
