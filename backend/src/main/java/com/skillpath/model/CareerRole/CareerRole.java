package com.skillpath.model.CareerRole;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "career_roles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CareerRole {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
 private Long id;
 @Column(unique = true, nullable = false, length = 100) 
 private String name;
 @Column(columnDefinition = "TEXT") 
 private String description;
}