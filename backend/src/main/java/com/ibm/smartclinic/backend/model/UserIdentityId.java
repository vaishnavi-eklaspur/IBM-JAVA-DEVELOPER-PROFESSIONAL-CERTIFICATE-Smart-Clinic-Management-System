package com.ibm.smartclinic.backend.model;

import com.ibm.smartclinic.backend.security.Role;
import java.io.Serializable;
import java.util.Objects;

public class UserIdentityId implements Serializable {

    private String email;
    private Role role;

    public UserIdentityId() {
    }

    public UserIdentityId(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserIdentityId that = (UserIdentityId) o;
        return Objects.equals(email, that.email) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, role);
    }
}
