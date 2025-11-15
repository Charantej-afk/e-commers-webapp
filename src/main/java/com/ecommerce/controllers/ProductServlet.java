package com.ecommerce.controllers;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.getWriter().println("<h1>Welcome to E-commerce Application!</h1>");
    }
}
