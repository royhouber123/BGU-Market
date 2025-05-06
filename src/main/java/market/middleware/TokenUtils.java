package market.middleware;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class TokenUtils {
    public static String getToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return (String) attrs.getRequest().getAttribute(AuthTokenFilter.TOKEN_ATTR);
        }
        return null;
    }
}
