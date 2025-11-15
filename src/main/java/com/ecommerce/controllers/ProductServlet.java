package com.ecommerce.controllers;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.getWriter().println("<h1>Welcome to Ecommerce App!</h1>");
        resp.getWriter().println("<p>Product list will appear here.</p>");
    }
}
