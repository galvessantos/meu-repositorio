package com.montreal.oauth.config;

import static com.montreal.core.exception_handler.ApiExceptionHandler.MSG_ERROR_GENERIC;
import static com.montreal.core.exception_handler.ApiExceptionHandler.MSG_ERROR_TOKEN_EXPIRED;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.montreal.core.exception_handler.Problem;
import com.montreal.core.exception_handler.ProblemType;
import com.montreal.oauth.domain.service.JwtService;
import com.montreal.oauth.domain.service.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String PREFIX_TOKEN = "Bearer ";
    @Value("${montreal.oauth.encryptSecretKey}")
    private  String encryptSecretKey;

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/password-reset/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;
            if(authHeader != null && authHeader.startsWith(PREFIX_TOKEN)){
                token = authHeader.split(PREFIX_TOKEN)[1];
                username = jwtService.extractUsername(token);
            }

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
                if(jwtService.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            Problem problem = buildProblem(ex, ProblemType.TOKEN_INVALIDOS,
                    HttpStatus.UNAUTHORIZED.value(),
                    MSG_ERROR_TOKEN_EXPIRED);
            handler(response, problem);
        } catch (Exception ex) {
            Problem problem = buildProblem(ex, ProblemType.ERRO_DE_SISTEMA,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    MSG_ERROR_GENERIC);
            handler(response, problem);
        }
    }

    private static Problem buildProblem(Exception ex, ProblemType problemType, Integer status, String userMessage) {
        return Problem.builder()
                .status(status)
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(ex.getMessage())
                .userMessage(userMessage)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private void handler(HttpServletResponse response, Problem problem) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }

}
