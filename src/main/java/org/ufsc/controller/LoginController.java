package org.ufsc.controller;

import org.ufsc.repository.UserRepository;
import org.ufsc.service.CryptoService;
import org.ufsc.view.MenuView;

import java.util.Map;

public class LoginController {

    public void Login() {
        Map<String, String> loginData = MenuView.showLogin();
        System.out.println(loginData);
    }

    public void Register() {
        Map<String, String> data = MenuView.showRegister();
        String username = data.get("username");
        char[] password = data.get("password").toCharArray();

        // 1. Gerar parâmetros criptográficos[cite: 1]
        byte[] salt = CryptoService.generateSalt();
        byte[] hash = CryptoService.hashPassword(password, salt);
        String totpSecret = CryptoService.generateTOTPSecret();

        // 2. Salvar no banco
        UserRepository repo = new UserRepository();
        repo.saveUser(username, hash, salt, totpSecret);

        // 3. Exibir a Secret para o usuário configurar o App (Google Authenticator)[cite: 1]
        System.out.println("\n*** ATENÇÃO: Configure seu TOTP ***");
        System.out.println("Sua chave secreta: " + totpSecret);
        System.out.println("************************************\n");
    }
}
