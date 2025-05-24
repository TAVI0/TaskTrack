package com.tavio.lemon.domain.service;

import com.tavio.lemon.domain.repository.UserRepository;
import com.tavio.lemon.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repo;

    public Optional<UserEntity> getByUsername(String username){
        return repo.findByUsername(username);
    }
    public Optional<UserEntity> getById(Long id){
        return repo.findById(id);
    }
    public List<UserEntity> getAll(){
        return (List<UserEntity>) repo.findAll();
    }

    public UserEntity save(UserEntity userEntity){
        return repo.save(userEntity);
    }

    public void delete(Long id){
        repo.deleteById(id);
    }
}