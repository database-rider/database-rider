package com.github.database.rider.springboot.model.user;

import jakarta.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface UserRepository extends CrudRepository<User, Long> {

  /**
   *
   * @param email the user email.
   */
   User findByEmail(String email);

}
