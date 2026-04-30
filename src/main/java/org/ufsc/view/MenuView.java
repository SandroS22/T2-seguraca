package org.ufsc.view;

import org.ufsc.util.MenuInput;

public class MenuView {

    public static Integer ShowMenu() {
        System.out.println("1 - Login");
        System.out.println("2 - Cadatrar");
        System.out.println("0 - Encerrar");

        return MenuInput.input();
    }

    public static void ShowLogin() {
        System.out.println("Insira suas informações");
        System.out.print("Nome de usuário: ");
        System.out.print("\nSenha: ");

    }
}
