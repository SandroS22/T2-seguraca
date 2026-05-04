package org.ufsc.util;

import java.io.Console;
import java.util.Scanner;

public class MenuInput {

    public static int intInput(){
        System.out.print("Selecione uma das opcoes acima: ");
        Scanner scanner = new Scanner(System.in);
        scanner.close();

        return scanner.nextInt();
    }

    public static String stringInput() {
        System.out.print("Inserir informação: ");
        Scanner scanner = new Scanner(System.in);
        scanner.close();

        return scanner.nextLine();
    }

    public static String passwordInput() {
        Console console = System.console();
        if (console == null) {
            System.out.println("Não foi possível encontrar o console!");
            System.exit(1);
        }

        return new String(console.readPassword("Insira sua senha: "));
    }
}
