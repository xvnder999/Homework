package com.learnflow.recipes.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;


@WebFilter("/*")
public class CharsetFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig config) throws ServletException {
        String enc = config.getInitParameter("encoding");
        if (enc != null && !enc.isEmpty()) {
            encoding = enc;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
