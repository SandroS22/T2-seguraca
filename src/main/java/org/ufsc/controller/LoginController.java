package org.ufsc.controller;

import org.ufsc.model.User;
import org.ufsc.repository.UserRepository;
import org.ufsc.service.CryptoService;
import org.ufsc.util.MenuInput;
import org.ufsc.view.MenuView;

import java.util.Map;

public class LoginController {

    public void Login() {
        Map<String, String> data = MenuView.showLogin();
        String username = data.get("username");
        String passwordAttempt = data.get("password");

        UserRepository repo = new UserRepository();
        User user = repo.getUserByUsername(username);

        if (user == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }

        byte[] attemptHash = CryptoService.hashPassword(passwordAttempt.toCharArray(), user.getSalt());
        if (!java.util.Arrays.equals(attemptHash, user.getPasswordHash())) {
            System.out.println("Senha incorreta.");
            return;
        }

        System.out.print("Digite o código TOTP (6 dígitos): ");
        String totpCode = MenuInput.stringInput();

        if (CryptoService.verifyTOTP(user.getTotpSecret(), totpCode)) {
            System.out.println("Login realizado com sucesso!");
        } else {
            System.out.println("Código TOTP inválido.");
        }
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
        
        System.out.println("\n*** ATENÇÃO: Configure seu TOTP ***");
        System.out.println("Sua chave secreta: " + totpSecret);
        System.out.println("************************************\n");
    }
}
