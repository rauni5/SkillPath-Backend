package com.skillpath.security;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import
org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.util.List;
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response); return;
        }
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance()
                                    .verifyIdToken(header.substring(7));
            var auth = new UsernamePasswordAuthenticationToken(new FirebasePrincipal(decoded.getUid(),
                                                                decoded.getEmail()),
                                                                null,
                                                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (FirebaseAuthException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        chain.doFilter(request, response);
    }
}
