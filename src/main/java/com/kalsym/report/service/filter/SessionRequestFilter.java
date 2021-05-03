package com.kalsym.report.service.filter;

import com.kalsym.report.service.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.report.service.ReportServiceApplication;
import com.kalsym.report.service.service.MySQLUserDetailsService;
import com.kalsym.report.service.utils.Logger;
import com.kalsym.report.service.utils.DateTimeUtil;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author Sarosh
 */
@Component
public class SessionRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MySQLUserDetailsService jwtUserDetailsService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${services.user-service.session_details:not-known}")
    String userServiceSessionDetailsUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String logprefix = request.getRequestURI();

        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, "------------- " + request.getMethod() + " " + logprefix + "-------------", "", "");

        final String authHeader = request.getHeader("Authorization");
        Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "Authorization: " + authHeader, "");

        String accessToken = null;

        boolean tokenPresent = false;

        // Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (null != authHeader && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.replace("Bearer ", "");
            Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "token: " + accessToken, "");
            Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "token length: " + accessToken.length(), "");
            tokenPresent = true;
        } else {
            Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "token does not begin with Bearer String", "");
        }

        boolean authorized = false;
        if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "sessionId: " + sessionId, "");

            try {
                ResponseEntity<HttpReponse> authResponse = restTemplate.postForEntity(userServiceSessionDetailsUrl, accessToken, HttpReponse.class);

                Date expiryTime = null;

                Auth auth = null;
                String username = null;

                if (authResponse.getStatusCode() == HttpStatus.ACCEPTED) {
                    ObjectMapper mapper = new ObjectMapper();
                    Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "data: " + authResponse.getBody().getData(), "");

                    auth = mapper.convertValue(authResponse.getBody().getData(), Auth.class);
                    username = auth.getSession().getUsername();
                    expiryTime = auth.getSession().getExpiry();
                }

                if (null != expiryTime && null != username) {
                    long diff = 0;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date currentTime = sdf.parse(DateTimeUtil.currentTimestamp());
                        diff = expiryTime.getTime() - currentTime.getTime();
                    } catch (Exception e) {
                        Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "error calculating time to session expiry", "");
                    }
                    Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "time to session expiry: " + diff + "ms", "");
                    if (0 < diff) {
                        authorized = true;
                        MySQLUserDetails userDetails = new MySQLUserDetails(auth, auth.getAuthorities());

                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    } else {
                        Logger.application.warn(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "session expired", "");
                        //response.setStatus(HttpStatus.UNAUTHORIZED);
                        response.getWriter().append("Session expired");
                    }
                }
            } catch (IOException | IllegalArgumentException | RestClientException e) {
                Logger.application.error(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "Exception processing session ", "", e);

            }

        }

        Logger.cdr.info(request.getRemoteAddr() + "," + request.getMethod() + "," + request.getRequestURI() + "," + tokenPresent + "," + authorized);

        chain.doFilter(request, response);
    }
}
