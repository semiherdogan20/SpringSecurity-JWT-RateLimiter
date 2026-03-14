package com.security.project.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimitFilter implements Filter {
    private static final int LIMIT = 5;
    private static final long WINDOW_MS = 60_000;
    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getServletPath();
        if (!"/api/login".equals(path) && !"/api/verify-login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(req);
        String email = req.getHeader("X-User-Email") != null ? req.getHeader("X-User-Email") : "unknown-email";
        String key = ip + "-" + email;
        long now = System.currentTimeMillis();

        Deque<Long> q = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        while (true) {
            Long first = q.peekFirst();
            if (first == null || now - first <= WINDOW_MS) break;
            q.pollFirst();
        }

        if (q.size() >= LIMIT) {
            res.setStatus(429);
            res.getWriter().write("Too many login attempts. Try again later.");
            return;
        }

        q.addLast(now);
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}