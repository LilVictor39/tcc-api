package br.edu.ceulp.tccapi.security;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<RequestLimitsFilter> filterRegistration(RequestLimitsFilter filter){
  var bean=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);bean.setEnabled(false);return bean;
 }
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecretKeySpec signingKey(@Value("${app.jwt.secret}") String secret){
  byte[] bytes=Base64.getDecoder().decode(secret);
  if(bytes.length<32)throw new IllegalArgumentException("JWT_SECRET deve conter ao menos 32 bytes em Base64");
  return new SecretKeySpec(bytes,"HmacSHA256");
 }
 @Bean JwtEncoder jwtEncoder(SecretKeySpec key){return new NimbusJwtEncoder(new ImmutableSecret<>(key));}
 @Bean JwtDecoder jwtDecoder(SecretKeySpec key){
  var decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("tcc-api"));return decoder;
 }
 @Bean SecurityFilterChain security(HttpSecurity http,RequestLimitsFilter limits,br.edu.ceulp.tccapi.error.ApiErrors errors) throws Exception {
  return http.csrf(c->c.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .addFilterBefore(limits,org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setHeader("WWW-Authenticate","Bearer");errors.write(res,401);}).accessDeniedHandler((req,res,ex)->errors.write(res,403)))
   .authorizeHttpRequests(a->a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll().requestMatchers(HttpMethod.POST,"/api/auth/cadastro","/api/auth/login").permitAll().anyRequest().authenticated())
   .oauth2ResourceServer(o->o.jwt(j->{}).authenticationEntryPoint((req,res,ex)->{res.setHeader("WWW-Authenticate","Bearer");errors.write(res,401);}).accessDeniedHandler((req,res,ex)->errors.write(res,403))).build();
 }
}
