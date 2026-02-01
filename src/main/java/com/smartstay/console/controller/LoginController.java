package com.smartstay.console.controller;

import com.smartstay.console.services.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v2/agents")
@CrossOrigin("*")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {


        String authUrl =
                "https://accounts.zoho.com/oauth/v2/auth" +
                        "?prompt=consent" +
                        "&response_type=code" +
                        "&client_id=1000.YLXF17CNZ2C016LL4WVTAQ8FRC6VWB" +
                        "&scope=email profile" +
                        "&redirect_uri=http://localhost:5173/verify" +
                        "&access_type=offline";

        response.sendRedirect(authUrl);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("code") String code, @RequestParam("location") String location, @RequestParam("accountsServer") String authorizeUrl)  {
        return loginService.verifyAuthToken(code, location, authorizeUrl);
    }

}
