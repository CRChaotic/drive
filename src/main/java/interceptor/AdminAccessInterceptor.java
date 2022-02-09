package interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import pojo.Role;
import pojo.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminAccessInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if(session == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        User admin = (User)session.getAttribute("user");
        if(admin == null || admin.getRole() != Role.ADMIN){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
