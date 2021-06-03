package com.ingsoft2021.SupermarketApp.auth.login;

import com.ingsoft2021.SupermarketApp.appadmin.AppAdmin;
import com.ingsoft2021.SupermarketApp.appadmin.AppAdminRepository;
import com.ingsoft2021.SupermarketApp.appuser.AppUser;
import com.ingsoft2021.SupermarketApp.appuser.AppUserRepository;
import com.ingsoft2021.SupermarketApp.util.Request.AuthResponse;
import com.ingsoft2021.SupermarketApp.util.Request.LoginRequest;
import com.ingsoft2021.SupermarketApp.util.Checker;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ingsoft2021.SupermarketApp.util.AppUserRole.*;

@AllArgsConstructor
@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final AppUserRepository appUserRepository;
    private final AppAdminRepository appAdminRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthResponse login(LoginRequest loginRequest) throws NoSuchFieldException {
        AuthResponse response = new AuthResponse(null, null, null);
        Checker.check(loginRequest);
        switch (loginRequest.getAppUserRole()){
            case "ADMIN":
                Optional<AppAdmin> appAdmin = appAdminRepository.findByEmail(loginRequest.getEmail());
                if(!appAdmin.isPresent()) throw new IllegalStateException("EMAIL_NOT_FOUND");
                if(bCryptPasswordEncoder.matches(loginRequest.getPassword(), appAdmin.get().getPassword())) {
                    String token = UUID.randomUUID().toString();
                    String cryptedToken = bCryptPasswordEncoder.encode(token);
                    LocalDateTime createdAt = LocalDateTime.now();
                    LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
                    loginRepository.save(new Login(appAdmin.get().getEmail(), ADMIN, cryptedToken, createdAt, expiresAt));
                    response = new AuthResponse(token, appAdmin.get().getEmail(), expiresAt);
                }else throw new IllegalStateException("INVALID_PASSWORD");
                break;
            case "USER":
                Optional<AppUser> appUser = appUserRepository.findByEmail(loginRequest.getEmail());
                if(!appUser.isPresent()) throw new IllegalStateException("EMAIL_NOT_FOUND");
                if(bCryptPasswordEncoder.matches(loginRequest.getPassword(), appUser.get().getPassword())) {
                    String token = UUID.randomUUID().toString();
                    String cryptedToken = bCryptPasswordEncoder.encode(token);
                    LocalDateTime createdAt = LocalDateTime.now();
                    LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
                    loginRepository.save(new Login(appUser.get().getEmail(), USER, cryptedToken, createdAt, expiresAt));
                    response = new AuthResponse(token, appUser.get().getEmail(), expiresAt);
                }else throw new IllegalStateException("INVALID_PASSWORD");
                break;
            default:
                throw new IllegalStateException("Wrong username or/and password");
        }
        return response;
    }



    public void logout(String token) {
        Login existingUser = findByToken(token);
        loginRepository.delete(loginRepository.findByEmail(existingUser.getEmail()).get());
    }


    public Login findByToken(String token) throws IllegalStateException{
        List<Login> logins = loginRepository.findAll();
        for(Login login : logins){
            if(bCryptPasswordEncoder.matches(token, login.getToken())){
                return login;
            }
        }
        throw new IllegalStateException("TOKEN_NOT_FOUND");
    }

    public AuthResponse loginAsGuest() {
        String token = UUID.randomUUID().toString();
        String encrypted_token = bCryptPasswordEncoder.encode(token);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus2 = LocalDateTime.now().plusHours(2);
        String anonymousEmail = "anonymous@"+UUID.randomUUID().toString().substring(0,7)+".guest";
        loginRepository.save(new Login(anonymousEmail,GUEST,encrypted_token,now,nowPlus2));
        return new AuthResponse(token,anonymousEmail,nowPlus2);
    }

    public void updateLogGuest(Login login){
        loginRepository.save(login);
    }

    public void deleteByToken(String token){
        List<Login> logins = loginRepository.findAll();
        for(Login login : logins){
            if(bCryptPasswordEncoder.matches(token, login.getToken())){
                loginRepository.delete(login);
                return;
            }
        }
        throw new IllegalStateException("TOKEN_NOT_FOUND");
    }
}