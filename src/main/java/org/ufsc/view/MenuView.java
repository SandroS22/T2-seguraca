package org.ufsc.view;

import org.ufsc.util.MenuInput;

import java.util.HashMap;
import java.util.Map;

public class MenuView {

    public static Integer ShowMenu() {
        System.out.println("1 - Login");
        System.out.println("2 - Cadatrar");
        System.out.println("0 - Encerrar");

        return MenuInput.intInput();
    }

    public static Map<String, String> ShowLogin() {
        System.out.println("=======================");
        System.out.println("||     LOGIN         ||");
        System.out.println("=======================");
        System.out.print("Nome de usuário: ");
        String username = MenuInput.stringInput();
        System.out.print("\nSenha: ");
        String password = MenuInput.passwordInput();

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        return loginData;
    }
}
