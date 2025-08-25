package com.milsondev.servus.service.auth;

import com.milsondev.servus.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String cookieToken = null;
        if (header == null) {
            // Try cookie named Authorization or servus.auth
            if (request.getCookies() != null) {
                for (var c : request.getCookies()) {
                    if ("Authorization".equalsIgnoreCase(c.getName()) || "servus.auth".equalsIgnoreCase(c.getName())) {
                        cookieToken = c.getValue();
                        break;
                    }
                }
            }
        }

        String username = null;
        String jwt = null;

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
        } else if (cookieToken != null) {
            jwt = cookieToken.startsWith("Bearer ") ? cookieToken.substring(7) : cookieToken;
        }

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Invalid token syntax/claims: clear cookies and continue unauthenticated
                clearAuthCookies(response);
                SecurityContextHolder.clearContext();
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token invalid for this user
                    clearAuthCookies(response);
                    SecurityContextHolder.clearContext();
                }
            } catch (UsernameNotFoundException ex) {
                // User no longer exists: clear token and proceed
                clearAuthCookies(response);
                SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                // Any other auth error: clear and proceed unauthenticated
                clearAuthCookies(response);
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        deleteCookie(response, "Authorization");
        deleteCookie(response, "servus.auth");
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set true when behind HTTPS
        response.addCookie(cookie);
    }
}

