package com.example.democart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.democart.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
