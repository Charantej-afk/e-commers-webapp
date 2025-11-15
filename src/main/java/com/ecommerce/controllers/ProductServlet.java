package com.ecommerce.controllers;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h1>E-Commerce Product List</h1>");
        out.println("<ul>");
        out.println("<li>Product 1 - ₹500</li>");
        out.println("<li>Product 2 - ₹899</li>");
        out.println("<li>Product 3 - ₹1299</li>");
        out.println("</ul>");
        out.println("</body></html>");
    }
}
