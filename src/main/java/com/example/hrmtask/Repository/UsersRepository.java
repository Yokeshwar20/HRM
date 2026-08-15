package com.example.hrmtask.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.Users;


public interface UsersRepository extends JpaRepository<Users,Long>{
    Optional<Users> findByEmail(String email);
}
