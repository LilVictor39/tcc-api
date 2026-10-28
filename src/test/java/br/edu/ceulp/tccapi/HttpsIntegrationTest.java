package br.edu.ceulp.tccapi;
import java.nio.file.*;
import java.net.URI;
import java.net.http.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.profiles.active=test",
 "server.ssl.enabled=true","server.ssl.key-store-type=PKCS12","server.ssl.key-alias=tcc-local","server.ssl.enabled-protocols=TLSv1.2,TLSv1.3",
 "app.limits.api=10000","app.limits.auth=10000",
 "spring.datasource.url=jdbc:h2:mem:https;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="})
class HttpsIntegrationTest {
 static final String PASSWORD="SomenteTesteAutomatico123";
 static Path keystore;
 @DynamicPropertySource static void tls(DynamicPropertyRegistry r)throws Exception{
  keystore=Files.createTempDirectory("tcc-tls-test-").resolve("test.p12");
  Process process=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","keytool").toString(),"-genkeypair","-alias","tcc-local","-keyalg","RSA","-keysize","2048","-validity","2","-storetype","PKCS12","-keystore",keystore.toString(),"-storepass",PASSWORD,"-dname","CN=localhost","-ext","SAN=dns:localhost,ip:127.0.0.1","-noprompt").redirectErrorStream(true).start();
  String output=new String(process.getInputStream().readAllBytes());if(process.waitFor()!=0)throw new IllegalStateException(output);
  keystore.toFile().deleteOnExit();
  r.add("server.ssl.key-store",()->keystore.toUri().toString());r.add("server.ssl.key-store-password",()->PASSWORD);
 }
 @LocalServerPort int port;
 HttpClient client(String protocol)throws Exception{
  KeyStore ks=KeyStore.getInstance("PKCS12");try(var in=Files.newInputStream(keystore)){ks.load(in,PASSWORD.toCharArray());}
  TrustManagerFactory tm=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());tm.init(ks);
  SSLContext context=SSLContext.getInstance("TLS");context.init(null,tm.getTrustManagers(),null);
  SSLParameters params=new SSLParameters();params.setProtocols(new String[]{protocol});
  return HttpClient.newBuilder().sslContext(context).sslParameters(params).build();
 }
 URI uri(String path){return URI.create("https://localhost:"+port+path);}
 void secure(String protocol)throws Exception{
  var response=client(protocol).send(HttpRequest.newBuilder(uri("/api/tarefas")).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(401,response.statusCode());assertEquals(protocol,response.sslSession().orElseThrow().getProtocol());
  assertTrue(response.headers().firstValue("Strict-Transport-Security").orElseThrow().contains("max-age="));
  assertTrue(response.body().contains("Autenticacao necessaria"));assertFalse(response.body().contains("trace"));
 }
 @Test void tls12CertificadoConfiavel()throws Exception{secure("TLSv1.2");}
 @Test void tls13CertificadoConfiavel()throws Exception{secure("TLSv1.3");}
 @Test void fluxoAutenticadoEmHttps()throws Exception{
  var c=client("TLSv1.3");var json=new com.fasterxml.jackson.databind.ObjectMapper();
  String credentials="{\"email\":\"https@example.test\",\"senha\":\"TesteHttps123!\"}";
  var registered=c.send(HttpRequest.newBuilder(uri("/api/auth/cadastro")).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(credentials)).build(),HttpResponse.BodyHandlers.ofString());assertEquals(201,registered.statusCode());
  var logged=c.send(HttpRequest.newBuilder(uri("/api/auth/login")).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(credentials)).build(),HttpResponse.BodyHandlers.ofString());assertEquals(200,logged.statusCode());String token=json.readTree(logged.body()).get("accessToken").asText();
  var task=c.send(HttpRequest.newBuilder(uri("/api/tarefas")).header("Content-Type","application/json").header("Authorization","Bearer "+token).POST(HttpRequest.BodyPublishers.ofString("{\"titulo\":\"Teste TLS\",\"status\":\"PENDENTE\"}")).build(),HttpResponse.BodyHandlers.ofString());assertEquals(201,task.statusCode());
 }
 @Test void httpSimplesNaoExecutaApi()throws Exception{
  var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/cadastro")).POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());assertEquals(400,response.statusCode());
 }
}
