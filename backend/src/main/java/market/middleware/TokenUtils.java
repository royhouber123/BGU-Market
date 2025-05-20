package market.middleware;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



public class TokenUtils {


    //added only to local acceptence Tests
    private static String mockToken = null;
    //added only to local acceptence Tests
    public static void setMockToken(String token) {
    mockToken = token;
    }
    //added only to local acceptence Tests
    public static void clearMockToken() {
        mockToken = null;
    }


    public static String getToken() {
        if (mockToken != null) return mockToken;//added only to local acceptence Tests
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return (String) attrs.getRequest().getAttribute(AuthTokenFilter.TOKEN_ATTR);
        }
        return null;
    }
}
