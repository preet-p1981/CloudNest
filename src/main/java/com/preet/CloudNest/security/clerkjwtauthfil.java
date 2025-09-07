package com.preet.CloudNest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class clerkjwtauthfil extends OncePerRequestFilter {

    @Value("${clerk.issuer}")
    private String clerkIssuer;
    private final clerkjsonkeypro jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // ✅ Skip JWT check for webhooks and files endpoints
        if (path.contains("/webhooks")  ||(path.contains("/download"))){
            filterChain.doFilter(request, response);
            return;
        }

        String authheader = request.getHeader("Authorization");
        if (authheader == null || !authheader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authheader.substring(7);
            String[] chunks = token.split("\\.");
            if (chunks.length < 3) {
                filterChain.doFilter(request, response);
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headernode = mapper.readTree(headerJson);

            if (!headernode.has("kid")) {
                filterChain.doFilter(request, response);
                return;
            }

            String kid = headernode.get("kid").asText();
            PublicKey publicKey = jwksProvider.getpublickey(kid);

            // verify token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String clerkid = claims.getSubject();
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(clerkid, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            filterChain.doFilter(request, response);
        }
    }
}