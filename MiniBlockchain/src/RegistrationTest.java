import java.io.File;

public class RegistrationTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Cadastro (Atividade 2.1.1 - Re-validado com 1.2.1) ---");

            
            String user = "bob123";
            String pass = "bob-strong-pass";

            System.out.println("Testando Cadastro VALIDO para: " + user);
            AuthService.register(user, pass);

            java.io.File userFile = new java.io.File("MiniBlockchain/data/users/user_" + user + ".json");
            if (userFile.exists()) {
                System.out.println("[SUCESSO] Arquivo de usuario criado.");
                String content = new String(java.nio.file.Files.readAllBytes(userFile.toPath()));
                if (!content.contains(pass)) {
                    System.out.println("[SUCESSO] Senha protegida no envelope.");
                }
            }

            
            System.out.println("\nTestando Cadastro INVALIDO (Username com simbolo): 'user_!@#'");
            try {
                AuthService.register("user_!@#", "senha-valida-123");
                System.out.println("[FALHA] Aceitou username invalido!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Rejeitou username invalido: " + e.getMessage());
            }

            
            System.out.println("\nTestando Cadastro INVALIDO (Senha curta): '123'");
            try {
                AuthService.register("alice99", "123");
                System.out.println("[FALHA] Aceitou senha curta!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Rejeitou senha curta: " + e.getMessage());
            }

            System.out.println("\n--- Fim da Re-validacao de Cadastro ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
