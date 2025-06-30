package market.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AuthTokenFilter implements Filter {
    public static final String TOKEN_ATTR = "AUTH_TOKEN";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        // Debug logging
        System.out.println("[AuthTokenFilter] Processing request:");
        System.out.println("  Method: " + req.getMethod());
        System.out.println("  URL: " + req.getRequestURL());
        System.out.println("  URI: " + req.getRequestURI());
        System.out.println("  Query: " + req.getQueryString());
        
        String authHeader = req.getHeader("Authorization");
        System.out.println("  Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "NULL"));
        
        // Log other important headers
        System.out.println("  Content-Type: " + req.getHeader("Content-Type"));
        System.out.println("  Origin: " + req.getHeader("Origin"));
        System.out.println("  Access-Control-Request-Method: " + req.getHeader("Access-Control-Request-Method"));
        
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("  Extracted token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));
        } else {
            System.out.println("  No valid Bearer token found");
        }
        
        req.setAttribute(TOKEN_ATTR, token);
        
        // Handle CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("  [AuthTokenFilter] Handling OPTIONS preflight request");
            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            res.setHeader("Access-Control-Max-Age", "3600");
            res.setStatus(HttpServletResponse.SC_OK);
            System.out.println("  [AuthTokenFilter] OPTIONS request handled, returning 200");
            return;
        }
        
        System.out.println("  [AuthTokenFilter] Continuing filter chain...");
        chain.doFilter(request, response);
        System.out.println("  [AuthTokenFilter] Request completed");
    }
}
