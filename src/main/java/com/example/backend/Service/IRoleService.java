package com.example.backend.Service;

import com.example.backend.Entity.Role;

import java.util.List;
import java.util.Optional;

public interface IRoleService {
    public Role createRole(Role role);
    public List<Role> getAllRoles();
    public Optional<Role> getRoleById(Long id);
    public Role updateRole(Long id, Role newRole);
    public void deleteRole(Long id);

}
