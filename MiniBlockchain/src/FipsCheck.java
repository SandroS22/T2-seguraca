import java.security.Security;
import java.security.Provider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

public class FipsCheck {
    public static void main(String[] args) {
        System.out.println("--- Verificando Provedor Bouncy Castle FIPS ---");
        
        
        Security.addProvider(new BouncyCastleFipsProvider());
        
        
        Provider provider = Security.getProvider("BCFIPS");
        
        if (provider != null) {
            System.out.println("Status: BCFIPS registrado com sucesso!");
            System.out.println("Informacoes do Provedor: " + provider.getInfo());
            System.out.println("Versao: " + provider.getVersionStr());
            
            
            boolean isFipsMode = org.bouncycastle.crypto.CryptoServicesRegistrar.isInApprovedOnlyMode();
            System.out.println("Modo FIPS (Approved Only): " + isFipsMode);
        } else {
            System.out.println("Erro: Provedor BCFIPS NAO encontrado no sistema.");
            System.exit(1);
        }
    }
}
