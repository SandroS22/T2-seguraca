package org.ufsc.util;

import java.util.Scanner;

public class IntegerInput {

    public static int input(){
        System.out.println("Selecione uma das opcoes:");
        Scanner scanner = new Scanner(System.in);

        int choice = scanner.nextInt();

        return choice;
    }
}
