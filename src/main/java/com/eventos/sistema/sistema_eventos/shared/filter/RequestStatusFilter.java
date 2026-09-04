package com.eventos.sistema.sistema_eventos.shared.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestStatusFilter extends OncePerRequestFilter {

    public static final String REQUEST_STATUS_ID = "requestStatusId";
    public static final String REQUEST_STATUS_HEADER = "X-Request-Status-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestStatusId = UUID.randomUUID().toString();

        request.setAttribute(REQUEST_STATUS_ID, requestStatusId);
        response.setHeader(REQUEST_STATUS_HEADER, requestStatusId);

        filterChain.doFilter(request, response);
    }
}
