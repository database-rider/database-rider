package com.github.database.rider.springboot.models;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface UserRepository extends CrudRepository<User, Long> {

  /**
   *
   * @param email the user email.
   */
  public User findByEmail(String email);

}
