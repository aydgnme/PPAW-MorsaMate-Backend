package me.aydgn.MorseMate.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ADMIN_LOGIN_PATH = "/admin/login";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");

        if (uri != null && uri.startsWith("/admin") && acceptsHtml(accept)) {
            response.sendRedirect(ADMIN_LOGIN_PATH);
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Authentication required\"}");
    }

    private boolean acceptsHtml(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        }
        return acceptHeader.contains("text/html") || acceptHeader.contains("application/xhtml+xml");
    }
}
