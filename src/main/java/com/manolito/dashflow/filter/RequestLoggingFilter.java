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

/**
 * A filter that logs all incoming HTTP requests and responses to an audit log.
 * <p>
 * This filter intercepts all requests (except Swagger documentation endpoints) and logs:
 * <ul>
 *   <li>Request method and URI</li>
 *   <li>Response status code</li>
 *   <li>Authenticated user ID (if available)</li>
 *   <li>Error messages (if request processing fails)</li>
 *   <li>Timestamp of the request</li>
 * </ul>
 * The logs are persisted to the database via {@link AuditLogRepository}.
 * </p>
 *
 * <p>
 * Security exceptions (authentication/authorization failures) are automatically detected
 * and logged with a 403 (Forbidden) status code.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;

    /**
     * Processes each HTTP request, wrapping the response for auditing purposes.
     * <p>
     * This implementation:
     * <ol>
     *   <li>Wraps the response to enable content caching</li>
     *   <li>Proceeds with the filter chain</li>
     *   <li>Logs successful requests</li>
     *   <li>Handles and logs exceptions</li>
     *   <li>Ensures the response is properly copied back</li>
     * </ol>
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to proceed with
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Logs the request details to the audit log.
     * <p>
     * Captures key information about the request/response cycle and persists it.
     * Silently handles any logging errors to avoid interrupting request processing.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param exception the exception that occurred (null if request succeeded)
     */
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

    /**
     * Retrieves the authenticated user's ID from the security context.
     * <p>
     * Safely extracts the user ID if:
     * <ul>
     *   <li>Authentication exists in the security context</li>
     *   <li>The principal is an {@link ApplicationUser}</li>
     * </ul>
     * </p>
     *
     * @return the user ID if available, null otherwise
     */
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

    /**
     * Determines which requests should bypass logging.
     * <p>
     * Excludes Swagger documentation endpoints from being logged to reduce noise.
     * </p>
     *
     * @param request the HTTP request
     * @return true if the request should not be filtered, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }
}
