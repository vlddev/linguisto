package org.linguisto.webui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectFilter implements Filter {
    @Override
    public void init(FilterConfig config) throws ServletException {
        // If you have any <init-param> in web.xml, then you could get them
        // here by config.getInitParameter("name") and assign it as field.
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String reqURI = request.getRequestURI();

        if (reqURI.startsWith("/sdictviewDe.xhtml") || reqURI.startsWith("/faces/sdictviewDe.xhtml")) {
            response.sendRedirect(request.getContextPath() + "/dict/de");
        } else if (reqURI.startsWith("/sdictviewEn.xhtml") || reqURI.startsWith("/faces/sdictviewEn.xhtml")) {
            response.sendRedirect(request.getContextPath() + "/dict/en");
        } else if (reqURI.startsWith("/sdictviewFr.xhtml") || reqURI.startsWith("/faces/sdictviewFr.xhtml")) {
            response.sendRedirect(request.getContextPath() + "/dict/fr");
        } else {
            chain.doFilter(req, res); // Logged-in user found, so just continue request.
        }
    }

    @Override
    public void destroy() {
        // If you have assigned any expensive resources as field of
        // this Filter class, then you could clean/close them here.
    }
}
