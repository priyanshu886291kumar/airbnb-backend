package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
   Optional< User> findByEmail(String email);
}
