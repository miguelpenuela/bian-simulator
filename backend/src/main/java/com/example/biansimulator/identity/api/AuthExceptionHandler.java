package com.example.biansimulator.identity.api;

import com.example.biansimulator.identity.application.InvalidCredentialsException;
import com.example.biansimulator.identity.application.LoginBlockedException;
import com.example.biansimulator.shared.error.ProblemTypes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials() {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos.");
        pd.setType(ProblemTypes.INVALID_CREDENTIALS);
        pd.setTitle("Credenciales inválidas");
        return pd;
    }

    @ExceptionHandler(LoginBlockedException.class)
    ResponseEntity<ProblemDetail> blocked(LoginBlockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos fallidos. Vuelva a intentarlo más tarde.");
        pd.setType(ProblemTypes.LOGIN_BLOCKED);
        pd.setTitle("Acceso bloqueado temporalmente");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.retryAfterSeconds()))
                .body(pd);
    }
}
