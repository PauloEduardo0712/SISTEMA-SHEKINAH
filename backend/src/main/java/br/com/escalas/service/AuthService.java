package br.com.escalas.service;

import br.com.escalas.api.auth.AuthRequest;
import br.com.escalas.api.auth.AuthResponse;
import br.com.escalas.api.auth.CurrentUserResponse;
import br.com.escalas.api.auth.RegisterRequest;
import br.com.escalas.api.volunteer.VolunteerRequest;
import br.com.escalas.security.AuthenticatedUser;
import br.com.escalas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VolunteerService volunteerService;

    public AuthResponse login(AuthRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        ).getPrincipal();

        return new AuthResponse(
            jwtService.generateToken(user),
            user.getUserId(),
            user.getVolunteerId(),
            user.getUsername(),
            user.getRole()
        );
    }

    public AuthResponse register(RegisterRequest request) {
        volunteerService.create(new VolunteerRequest(
            request.fullName(),
            request.username(),
            request.email(),
            request.phone(),
            request.notes(),
            request.ministryIds(),
            true,
            null,
            request.password()
        ));

        return login(new AuthRequest(request.username(), request.password()));
    }

    public CurrentUserResponse currentUser(AuthenticatedUser user) {
        if (user == null) {
            throw new InsufficientAuthenticationException("Sessao invalida ou expirada.");
        }

        return new CurrentUserResponse(
            user.getUserId(),
            user.getVolunteerId(),
            user.getUsername(),
            user.getRole()
        );
    }
}
