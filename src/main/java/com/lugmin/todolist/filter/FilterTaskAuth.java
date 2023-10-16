package com.lugmin.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lugmin.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.equals("/tasks/")) {
            var auth = request.getHeader("Authorization");
            var authEncoded = auth.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecoded);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String passwd = credentials[1];

            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Wrong login credentials.");
            } else {
                var passwdChecker = BCrypt.verifyer().verify(passwd.toCharArray(), user.getPassword());
                if (passwdChecker.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Wrong login credentials.");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
