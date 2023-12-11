package com.capita.securecapita.repository;


import com.capita.securecapita.model.Role;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface RoleRepository<T extends Role> {
    /*Basic Crud operation*/
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);
    void addRoleToUser(Long userId, String roleName);
}
