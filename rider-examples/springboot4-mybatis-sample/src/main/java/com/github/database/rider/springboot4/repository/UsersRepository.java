package com.github.database.rider.springboot4.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.github.database.rider.springboot4.entity.user.Users;
import com.github.database.rider.springboot4.entity.user.UsersExample;
import com.github.database.rider.springboot4.mapper.user.UsersMapper;

@Repository
public class UsersRepository {

	private final UsersMapper UsersMapper;

	public UsersRepository(UsersMapper UsersMapper) {
		this.UsersMapper = UsersMapper;
	}

	public List<Users> findAll() {
        return this.UsersMapper.selectByExample(null);
    }

	public int delete(List<Long> ids) {
		UsersExample UsersExample = new UsersExample();
		UsersExample.createCriteria().andIdIn(ids);
		return this.UsersMapper.deleteByExample(UsersExample);
	}

	public long count() {
		return this.UsersMapper.countByExample(null);
	}
	
	public Users findByEmail(String email) {
		UsersExample UsersExample = new UsersExample();
		UsersExample.createCriteria().andEmailEqualTo(email);
		return this.UsersMapper.selectByExample(UsersExample).stream().findFirst().orElse(null);
	}

	public Optional<Users> findById(Long id) {
		return Optional.ofNullable(this.UsersMapper.selectByPrimaryKey(id));
	}

	public int delete(Users Users) {
		return this.UsersMapper.deleteByPrimaryKey(Users.getId());
	}

	public boolean save(Users Users) {
		List<Users> Userss = this.findAll();
		long maxId = 0;
		for (Users u : Userss) {
			if (u.getId() > maxId) {
				maxId = u.getId();
			}
			if (u.getEmail().equals(Users.getEmail()) && u.getName().equals(Users.getName())) {
				Users.setId(u.getId());
			}
		}
		if (Objects.nonNull(Users.getId())) {
			return this.UsersMapper.updateByPrimaryKey(Users) > 0;
		} else {
			Users.setId(maxId + 1);
			return this.UsersMapper.insert(Users) > 0;
		}
	}
}
