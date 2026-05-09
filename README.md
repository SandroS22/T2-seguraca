# MiniBlockchain - Sistema Simétrico com Autenticação Forte

Este projeto implementa uma mini-blockchain multiusuário com foco em segurança criptográfica, utilizando o provedor **Bouncy Castle FIPS (BCFIPS)**. O sistema garante confidencialidade (AES-GCM), integridade (SHA-256 Chaining) e autenticação de dois fatores (TOTP).

## 🚀 Requisitos de Ambiente

1.  **Java JDK 8 ou superior:** Certifique-se de que o comando `java` e `javac` estão disponíveis no seu terminal.
2.  **Bibliotecas FIPS e Utilitários:** Os arquivos JAR necessários já estão incluídos na pasta `MiniBlockchain/lib/`:
    *   `bc-fips-1.0.2.jar` (Provedor principal)
    *   `bcpkix-fips-1.0.4.jar` (Extensões PKIX)
    *   `commons-codec-1.10.jar` (Utilitários Base32 para TOTP)

## 🛠️ Compilação

Para compilar todo o projeto, navegue até a raiz do diretório onde a pasta `MiniBlockchain` está localizada e execute o seguinte comando:

```bash
javac -cp "MiniBlockchain/lib/bc-fips-1.0.2.jar;MiniBlockchain/lib/bcpkix-fips-1.0.4.jar;MiniBlockchain/lib/commons-codec-1.10.jar" MiniBlockchain/src/*.java
```

*Nota: No Linux/macOS, utilize `:` em vez de `;` para separar os caminhos do classpath.*

## 🏃 Execução da Aplicação (CLI)

Após a compilação, você pode iniciar a interface interativa com o comando abaixo. 

> **Dica:** Adicione a flag `--debug` ao final para ver os segredos e cálculos internos em tempo real.

### Execução Normal:
```powershell
java -cp "MiniBlockchain/src;MiniBlockchain/lib/bc-fips-1.0.2.jar;MiniBlockchain/lib/bcpkix-fips-1.0.4.jar;MiniBlockchain/lib/commons-codec-1.10.jar" Main
```

### Execução em Modo Debug (Recomendado para Testes):
```powershell
java -cp "MiniBlockchain/src;MiniBlockchain/lib/bc-fips-1.0.2.jar;MiniBlockchain/lib/bcpkix-fips-1.0.4.jar;MiniBlockchain/lib/commons-codec-1.10.jar" Main --debug
```

### Fluxo Sugerido de Uso:
1.  **Cadastrar:** Crie um usuário e **anote o Segredo TOTP** exibido.
2.  **Configurar 2FA:** Utilize o segredo em um app como *Google Authenticator* ou *2FAS*.
3.  **Login:** Forneça a senha e o código de 6 dígitos gerado no app.
4.  **Operar:** Adicione blocos, visualize o histórico e execute auditorias.

## 🧪 Execução de Testes de Validação

O projeto inclui diversas suites de testes automatizados que validam cada camada do sistema:

| Teste | Comando de Execução (Copia e Cola) |
| :--- | :--- |
| **Geral** | `java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" SystemWalkthroughTest` |
| **Autenticação** | `java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" AuthFlowTest` |
| **Integridade** | `java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" IntegrityStressTest` |
| **Persistência** | `java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" FullBlockPersistenceTest` |

## 📁 Estrutura de Pastas

*   `src/`: Código-fonte Java.
*   `lib/`: Bibliotecas BCFIPS e utilitários.
*   `data/`: Diretório gerado automaticamente para persistência (JSONs e Logs).

---
**Desenvolvido por:** Gemini CLI
**Versão:** 1.1.0 (Com suporte Base32)
