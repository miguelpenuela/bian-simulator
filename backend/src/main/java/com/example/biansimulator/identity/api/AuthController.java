package com.example.biansimulator.identity.api;

import com.example.biansimulator.identity.api.AuthDtos.CsrfTokenResponse;
import com.example.biansimulator.identity.api.AuthDtos.CurrentUserResponse;
import com.example.biansimulator.identity.api.AuthDtos.LoginRequest;
import com.example.biansimulator.identity.application.AuthenticatedUser;
import com.example.biansimulator.identity.application.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final LoginService loginService;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public AuthController(LoginService loginService, SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) {
        this.loginService = loginService;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    @Operation(summary = "Obtiene (y emite en cookie XSRF-TOKEN) el token CSRF")
    public CsrfTokenResponse csrf(CsrfToken token) {
        return new CsrfTokenResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y crea la sesión de servidor")
    public CurrentUserResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = loginService.authenticate(body.username(), body.password(),
                request.getRemoteAddr());

        // Protección contra fijación de sesión: id nuevo al autenticarse.
        request.getSession(true);
        request.changeSessionId();

        SecurityContext context = contextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        // Token CSRF nuevo tras el cambio de privilegios; el cliente lo vuelve a pedir.
        csrfTokenRepository.saveToken(null, request, response);

        return CurrentUserResponse.from((AuthenticatedUser) authentication.getPrincipal());
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario de la sesión actual")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return CurrentUserResponse.from(user);
    }
}
