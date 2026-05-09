# Plano de Trabalho: Atividade 2.2.1 - Verificação de Credenciais de Primeiro Fator (Senha)

## Objetivo
Implementar o primeiro estágio do processo de login. O sistema deve carregar o perfil cifrado do usuário através do modelo `UserStorage`, derivar a chave a partir da senha fornecida e decifrar o blob de dados. O sucesso na decifragem GCM confirma a validade da senha.

## Impacto da Atividade 2.1.3 (Atualização)
A base técnica para esta atividade foi antecipada e validada na auditoria de persistência (2.1.3). Já possuímos:
- O método `StorageManager.loadUserStorage(username)`.
- A lógica comprovada de que a falha no GCM indica senha incorreta ou arquivo adulterado.
- O modelo `UserStorage` que isola o salt e o IV do blob cifrado.

## Requisitos Técnicos
1.  **Entrada:** Username e Senha.
2.  **Fluxo de Verificação:** 
    1. Obter `UserStorage` via `StorageManager`.
    2. Derivar chave com `SecurityUtils.deriveKey(senha, salt)`.
    3. Tentar decifrar o blob para obter o objeto `User` em memória.
3.  **Resultado:** Objeto `User` decifrado e a Chave Mestra disponível em memória para a sessão.

## Passos de Execução

### 1. Implementação do Método de Autenticação Estágio 1
*   **Ação:** Implementar `AuthService.authenticateStep1(String username, String password)`.
*   **Diferencial:** Este método não apenas valida a senha, mas "destrava" os dados do usuário (como o segredo TOTP) que serão necessários no Estágio 2.

### 2. Gestão da Chave de Sessão
*   **Ação:** Definir uma estrutura (ex: Classe `SessionContext`) para manter o usuário logado e sua chave de cifragem em memória, garantindo que não sejam persistidas em texto claro (Requisito 6.i).

### 3. Validação Integrada
*   **Teste:** Executar `LoginStep1Test.java` para confirmar que o fluxo de "destravar" o envelope funciona de ponta a ponta com usuários reais cadastrados.


---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. AuthService** | Implementar `authenticateStep1` | Implementado com sucesso | **Igual:** Utiliza a decifragem do envelope como prova de senha. |
| **2. Sessão** | Gestão de chave em memória | `MiniBlockchainServer` Facade | **Melhoria:** A gestão de sessão foi movida para uma fachada que gerencia o estado pendente. |
| **3. Validação** | Teste de sucesso e falha | `LoginStep1Test` OK | **Igual:** Validou-se sob as novas regras de alfanuméricos. |

---

## Re-validação e Impacto da Arquitetura (1.2) e Cadastro (2.1)

A atividade 2.2.1 foi refinada para se tornar o primeiro estágio de um fluxo de estado seguro:

### Mudanças e Fortalecimento:
1.  **Isolamento via Fachada (1.2.3):** O método de login agora reside por trás da `MiniBlockchainServer`. Quando o Passo 1 é concluído, o servidor mantém o usuário decifrado em um estado "pendente" interno, nunca enviando o objeto `User` ou a `SecretKey` para o cliente. Isso atende rigorosamente ao requisito **6.i**.
2.  **Sanitização Herdada (1.2.1):** Tentativas de login com usernames malformados são bloqueadas preventivamente, protegendo o `StorageManager`.
3.  **Integridade GCM:** Confirmado que o erro de decifragem do envelope é o único sinal necessário para negar o acesso, garantindo que o sistema não processe dados adulterados.

### Resultados da Rodada de Re-validação:
*   **Sucesso:** Senha correta permite o acesso ao estado pendente do servidor.
*   **Rejeição:** Senhas incorretas geram erro de integridade GCM, bloqueando o acesso aos dados sensíveis (TOTP secret).
*   **Isolamento:** Cliente CLI recebeu apenas uma `ServerResponse` textual, sem acesso às chaves em memória.

---
**Próximo Passo Imediato:** Implementar o método `authenticateStep1` no `AuthService.java`.
