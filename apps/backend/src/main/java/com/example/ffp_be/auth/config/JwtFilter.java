package com.example.ffp_be.auth.config;

import com.example.ffp_be.auth.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);
        String uri = request.getRequestURI();

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("SecurityContext에 인증 저장: {}", uri);
        } else {
            log.debug("유효한 JWT 토큰 없음: {}", uri);
        }

        chain.doFilter(request, response);
    }
}
