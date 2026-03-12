package com.pm.filter;

import com.pm.model.User;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override public void init(FilterConfig c) {}
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  rq = (HttpServletRequest) req;
        HttpServletResponse rs = (HttpServletResponse) res;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        String p = rq.getServletPath();
        if (p.equals("/login") || p.equals("/register") || p.equals("/confirm")
            || p.startsWith("/css/") || p.startsWith("/js/")) {
            chain.doFilter(req, res); return;
        }
        HttpSession s = rq.getSession(false);
        User u = s != null ? (User) s.getAttribute("user") : null;
        if (u == null) { rs.sendRedirect(rq.getContextPath()+"/login"); return; }
        if (p.startsWith("/admin") && !u.isAdmin()) {
            rs.sendRedirect(rq.getContextPath()+"/dashboard"); return;
        }
        chain.doFilter(req, res);
    }
    @Override public void destroy() {}
}
