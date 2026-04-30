package org.ufsc.util;

import java.util.Scanner;

public class MenuInput {

    public static int input(){
        System.out.println("Selecione uma das opcoes:");
        Scanner scanner = new Scanner(System.in);

        return scanner.nextInt();
    }
}
