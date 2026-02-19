package com.progresssoft.docaccess.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserHeaderFilter extends OncePerRequestFilter {

    private static final String USER_HEADER = "X-User";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader(USER_HEADER);

        if (username == null || username.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing X-User header");
            return;
        }

        try {
            UserContextHolder.setCurrentUser(username);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }

    }
}