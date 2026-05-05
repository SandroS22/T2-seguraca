package org.ufsc.util;

import java.io.Console;
import java.util.Scanner;

public class MenuInput {

    private static final Scanner scan = new Scanner(System.in);

    public static int intInput(){
        scan.reset();
        System.out.print("Selecione uma das opcoes acima: ");
        return scan.nextInt();
    }

    public static String stringInput() {
        scan.reset();
        return scan.next();
    }

    public static String passwordInput() {
        Console console = System.console();
        if (console == null) {
            System.out.println("Não foi possível encontrar o console!");
            System.exit(1);
        }

        return new String(console.readPassword());
    }

    public static void clean() {
        scan.close();
    }
}
