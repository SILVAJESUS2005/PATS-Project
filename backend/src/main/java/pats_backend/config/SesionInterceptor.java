package pats_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Este interceptor protege las rutas privadas del sistema.
// Si el usuario no tiene una sesión activa, el servidor responde con 401 Unauthorized.

@Component
public class SesionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // Verifica si existe sesión activa
        if (session == null || session.getAttribute("usuario") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("401 Unauthorized: Debes iniciar sesión");

            return false;
        }

        return true;
    }
}