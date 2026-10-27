package br.edu.ceulp.tccapi.model;
import jakarta.persistence.*;
@Entity @Table(name="usuarios")
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String email;
 @Column(nullable=false) private String senhaHash;
 protected Usuario() {}
 public Usuario(String email,String senhaHash){this.email=email;this.senhaHash=senhaHash;}
 public Long getId(){return id;} public String getEmail(){return email;} public String getSenhaHash(){return senhaHash;}
}
