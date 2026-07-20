package com.skillpath.security;

import com.skillpath.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class AdminAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepo;

    private static final String ADMIN_PREFIX = "/api/v1/admin/";
    private static final String SETUP_PATH   = "/api/v1/admin/setup";

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         chain) throws ServletException, IOException {

        final String uri = request.getRequestURI();

        // Only intercept admin routes
        if (!uri.startsWith(ADMIN_PREFIX) || uri.equals(SETUP_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof FirebasePrincipal principal)) {
            write401(response);
            return;
        }

        // Look up the user and check the is_admin flag
        boolean isAdmin = userRepo
                .findByFirebaseUid(principal.getUid())
                .map(u -> u.isAdmin())
                .orElse(false);

        if (!isAdmin) {
            write403(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void write401(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        try (PrintWriter w = response.getWriter()) {
            w.write("{\"success\":false,\"message\":\"Authentication required.\"}");
        }
    }

    private void write403(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        try (PrintWriter w = response.getWriter()) {
            w.write("{\"success\":false,\"message\":\"Admin access required.\"}");
        }
    }
}