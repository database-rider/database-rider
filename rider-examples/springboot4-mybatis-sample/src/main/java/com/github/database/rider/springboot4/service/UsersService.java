package com.github.database.rider.springboot4.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.database.rider.springboot4.entity.user.Users;
import com.github.database.rider.springboot4.repository.UsersRepository;

@Service
public class UsersService {

	private final UsersRepository usersRepository;

	public UsersService(UsersRepository usersRepository) {
		this.usersRepository = usersRepository;
	}

	@Transactional
	public List<Users> findAll() {
		return this.usersRepository.findAll();
	}

	@Transactional
	public long count() {
		return this.usersRepository.count();
	}

	@Transactional
	public Users findByEmail(String email) {
		return this.usersRepository.findByEmail(email);
	}

	@Transactional
	public Optional<Users> findById(Long id) {
		return this.usersRepository.findById(id);
	}

	@Transactional
	public int delete(Users users) {
		return this.usersRepository.delete(users);
	}

	@Transactional
	public boolean save(Users users) {
		return this.usersRepository.save(users);
	}
}