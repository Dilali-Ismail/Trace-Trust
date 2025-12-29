package org.usermanagement.traceandtrust.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MdcLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Extraire les informations de sécurité si elles existent
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                MDC.put("user", auth.getName());
                MDC.put("role", auth.getAuthorities().iterator().next().getAuthority());
            } else {
                MDC.put("user", "anonymous");
                MDC.put("role", "none");
            }

            // Informations sur l'endpoint
            MDC.put("endpoint", request.getMethod() + " " + request.getRequestURI());

            filterChain.doFilter(request, response);

            // Ajouter le statut HTTP après l'exécution
            MDC.put("status", String.valueOf(response.getStatus()));
            
        } finally {
            // Nettoyage impératif pour éviter les fuites de contexte entre les threads
            MDC.clear();
        }
    }
}
