package interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import pojo.User;
import pojo.UserStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AccessInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        System.out.println("preHandle");
        if(session == null || session.getAttribute("user") == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }else{
            User user = (User)session.getAttribute("user");
            if(user.getUserStatus() == UserStatus.BLOCKED)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return user.getUserStatus() != UserStatus.BLOCKED;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
