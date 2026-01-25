package com.github.quarkus.postgres;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "DUMMY")
@Entity
public class DummyEntity {
  @Id
  @Column(name = "ID")
  private int id;
}
