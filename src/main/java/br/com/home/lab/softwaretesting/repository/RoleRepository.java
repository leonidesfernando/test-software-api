package br.com.home.lab.softwaretesting.repository;

import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}