package com.manolito.dashflow.filter;

import com.manolito.dashflow.entity.application.ApplicationUser;
import com.manolito.dashflow.entity.application.AuditLog;
import com.manolito.dashflow.repository.application.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);

            logRequest(request, responseWrapper, null);

        } catch (Exception e) {
            if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
                responseWrapper.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

            logRequest(request, responseWrapper, e);
            throw e;
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request,
                            HttpServletResponse response,
                            Exception exception) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .requestMethod(request.getMethod())
                    .requestUri(request.getRequestURI())
                    .responseStatus(response.getStatus())
                    .userId(getAuthenticatedUserId())
                    .errorMessage(exception != null ? exception.getMessage() : null)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to save audit log", e);
        }
    }

    private Integer getAuthenticatedUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof ApplicationUser) {
                return ((ApplicationUser) authentication.getPrincipal()).getId();
            }
        } catch (Exception e) {
            logger.error("Error getting user ID", e);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }
}
