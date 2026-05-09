import java.util.Scanner;
import java.util.List;

/**
 * Ponto de entrada da aplicação MiniBlockchain (Cliente CLI).
 * Atua como o "Cliente" que interage com o "Servidor" via Fachada.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Verificar flag de debug
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--debug")) {
                DebugConfig.isEnabled = true;
                System.out.println("[SISTEMA] Modo Debug ATIVADO.");
            }
        }

        System.out.println("========================================");
        System.out.println("       MINI-BLOCKCHAIN CLI v1.0         ");
        System.out.println("========================================");

        while (true) {
            try {
                if (!MiniBlockchainServer.isAuthenticated()) {
                    showVisitorMenu();
                } else {
                    showAuthenticatedMenu();
                }
            } catch (Exception e) {
                System.out.println("\n[!] Ocorreu um erro inesperado: " + e.getMessage());
            }
        }
    }

    private static void showVisitorMenu() {
        System.out.println("\n--- MENU PRINCIPAL (Visitante) ---");
        System.out.println("1. Cadastrar Novo Usuário");
        System.out.println("2. Realizar Login");
        System.out.println("3. Sair");
        System.out.print("Escolha uma opção: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                handleRegister();
                break;
            case "2":
                handleLogin();
                break;
            case "3":
                System.out.println("Encerrando aplicação...");
                System.exit(0);
            default:
                System.out.println("[!] Opção inválida.");
        }
    }

    private static void showAuthenticatedMenu() {
        System.out.println("\n--- MENU BLOCKCHAIN (Autenticado: " + SessionContext.getCurrentUser().getUsername() + ") ---");
        System.out.println("1. Adicionar Novo Bloco");
        System.out.println("2. Listar Blockchain (Ver Histórico)");
        System.out.println("3. Realizar Auditoria de Integridade");
        System.out.println("4. Realizar Logout");
        System.out.println("5. Sair da Aplicação");
        System.out.print("Escolha uma opção: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                handleAddBlock();
                break;
            case "2":
                handleListBlockchain();
                break;
            case "3":
                handleAudit();
                break;
            case "4":
                MiniBlockchainServer.logout();
                System.out.println("[OK] Logout realizado com sucesso.");
                break;
            case "5":
                System.out.println("Encerrando aplicação...");
                System.exit(0);
            default:
                System.out.println("[!] Opção inválida.");
        }
    }

    private static void handleRegister() {
        System.out.println("\n--- CADASTRO DE USUÁRIO ---");
        System.out.print("Digite o username desejado: ");
        String user = scanner.nextLine();
        System.out.print("Digite a senha desejada: ");
        String pass = scanner.nextLine();

        ServerResponse res = MiniBlockchainServer.register(user, pass);
        if (res.isSuccess()) {
            System.out.println("\n[SUCESSO] " + res.getMessage());
            System.out.println("IMPORTANTE: Seu segredo TOTP (2FA) eh: " + res.getData());
            System.out.println("Configure seu aplicativo de autenticacao agora.");
        } else {
            System.out.println("\n[ERRO] " + res.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- LOGIN (PASSO 1: SENHA) ---");
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Senha: ");
        String pass = scanner.nextLine();

        ServerResponse res1 = MiniBlockchainServer.loginStep1(user, pass);
        if (!res1.isSuccess()) {
            System.out.println("\n[ERRO] " + res1.getMessage());
            return;
        }

        System.out.println("\n" + res1.getMessage());
        System.out.print("Digite o codigo TOTP de 6 digitos: ");
        String code = scanner.nextLine();

        ServerResponse res2 = MiniBlockchainServer.loginStep2(code);
        if (res2.isSuccess()) {
            System.out.println("\n[SUCESSO] " + res2.getMessage());
        } else {
            System.out.println("\n[ERRO] " + res2.getMessage());
        }
    }

    private static void handleAddBlock() {
        System.out.println("\n--- REGISTRAR NOVO BLOCO ---");
        System.out.print("Digite o conteudo do bloco: ");
        String content = scanner.nextLine();

        ServerResponse res = MiniBlockchainServer.addBlock(content);
        if (res.isSuccess()) {
            System.out.println("\n[SUCESSO] Bloco selado e salvo!");
            System.out.println("Hash do Bloco: " + res.getData());
        } else {
            System.out.println("\n[ERRO] " + res.getMessage());
        }
    }

    private static void handleListBlockchain() {
        System.out.println("\n--- HISTÓRICO DA BLOCKCHAIN ---");
        ServerResponse res = MiniBlockchainServer.getBlockchain();
        
        if (res.isSuccess()) {
            List<Block> chain = (List<Block>) res.getData();
            if (chain.isEmpty()) {
                System.out.println("A blockchain esta vazia.");
                return;
            }

            // Cabeçalho da Tabela
            String header = String.format("| %-3s | %-19s | %-12s | %-30s | %-12s |", 
                                          "ID", "DATA/HORA", "DONO", "CONTEUDO", "HASH (RES)");
            String divider = "+-----+---------------------+--------------+--------------------------------+--------------+";

            System.out.println(divider);
            System.out.println(header);
            System.out.println(divider);

            for (Block b : chain) {
                String date = BlockchainUtils.formatTimestamp(b.getTimestamp());
                String content = b.getDataRaw();
                if (content.length() > 30) content = content.substring(0, 27) + "...";
                
                String hashRes = b.getHash().substring(0, 12);

                System.out.println(String.format("| %-3s | %-19s | %-12s | %-30s | %-12s |", 
                                   b.getIndex(), date, b.getOwner(), content, hashRes));
            }
            System.out.println(divider);
            System.out.println("Nota: Conteudos de terceiros permanecem cifrados.");
        } else {
            System.out.println("\n[ERRO] " + res.getMessage());
        }
    }

    private static void handleAudit() {
        System.out.println("\n--- EXECUTANDO AUDITORIA ESTRUTURAL ---");
        ServerResponse res = MiniBlockchainServer.audit();
        if (res.isSuccess()) {
            System.out.println("[OK] " + res.getMessage());
        } else {
            System.out.println("[PERIGO] " + res.getMessage());
        }
    }
}
