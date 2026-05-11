package br.com.escalas.security;

import br.com.escalas.domain.auth.Role;
import br.com.escalas.domain.auth.UserAccount;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthenticatedUser implements UserDetails {

    private final Long userId;
    private final Long volunteerId;
    private final String username;
    private final String password;
    private final Role role;
    private final boolean active;

    public AuthenticatedUser(UserAccount account) {
        this.userId = account.getId();
        this.volunteerId = account.getVolunteer() != null ? account.getVolunteer().getId() : null;
        this.username = account.getUsername();
        this.password = account.getPasswordHash();
        this.role = account.getRole();
        this.active = account.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
