package com.github.quarkus.postgres;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
