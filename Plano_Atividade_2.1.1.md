# Plano de Trabalho: Atividade 2.1.1 - Implementação da Derivação de Senha com PBKDF2/Scrypt

## Objetivo
Implementar a lógica de derivação de senha de alto nível para o processo de cadastro de usuários. O objetivo é garantir que as senhas em texto puro nunca sejam armazenadas, transformando-as em hashes criptográficos robustos utilizando os parâmetros e algoritmos validados na fase de arquitetura.

## Requisitos Técnicos
1.  **Algoritmo:** PBKDF2WithHmacSHA512 (via BCFIPS).
2.  **Configuração:** 10.000 iterações, chave de 256 bits.
3.  **Salt:** 16 bytes (128 bits) gerados aleatoriamente para cada novo usuário.
4.  **Integração:** Utilizar a classe `SecurityUtils` já implementada.

## Passos de Execução

### 1. Design da Lógica de Cadastro
*   **Ação:** Definir o fluxo de transformação: `Senha -> Salt Único -> PBKDF2 -> Hash de Verificação`.
*   **Nota:** O hash gerado será usado no envelope cifrado (Atividade 1.1.3) para validar o login futuramente.

### 2. Implementação do AuthService (Cadastro)
*   **Ação:** Criar a classe `AuthService.java` que coordenará o processo de registro.
*   **Método:** `register(String username, String password)`.
*   **Responsabilidade:**
    1. Verificar se o usuário já existe.
    2. Gerar salt aleatório.
    3. Derivar a chave (KDF).
    4. Gerar o hash de verificação (HMAC da string "auth" usando a chave derivada).

### 3. Integração com UserStorage
*   **Ação:** Adaptar a lógica para criar o objeto `User` e prepará-lo para o armazenamento via `UserStorage`.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar um script `RegistrationTest.java` que cadastra um usuário e verifica se o hash armazenado é consistente (mesma senha + mesmo salt = mesmo hash).


---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Password -> Salt -> PBKDF2 -> Hash | Implementado conforme planejado. | O hash de verificação é um HMAC da string "auth-verifier" usando a chave derivada. |
| **2. AuthService** | Classe `AuthService` com `register()` | Classe criada e funcional. | Centraliza a lógica de cadastro e gestão de segurança do usuário. |
| **3. Integração** | Uso de `UserStorage` | Integrado via `UserStorage`. | Garante que dados sensíveis fiquem no blob cifrado. |
| **4. Validação** | `RegistrationTest` funcional | Sucesso na criação e ocultação da senha. | O teste confirmou que a senha não aparece no arquivo físico. |

### Justificativa das Mudanças e Detalhes Técnicos
- **Segurança Reforçada:** Em vez de armazenar o hash da senha diretamente, armazenamos um verificador derivado. Isso adiciona uma camada de separação entre a chave de cifragem (Master Key) e a credencial de autenticação.
- **Conformidade:** O resultado do teste mostra um arquivo JSON contendo apenas `salt`, `iv` e `blob`, cumprindo integralmente o requisito de que parâmetros sensíveis devem estar cifrados.
- **Parâmetros Utilizados:** 
    - Salt: 16 bytes (aleatório).
    - Iterações: 10.000 (PBKDF2-HMAC-SHA512).
    - Chave: 256 bits (AES-256).

---

## Re-validação e Impacto da Atividade 1.2.1 (Pós-Design de Esquema)

Após a formalização do Esquema do Usuário (1.2.1), a atividade 2.1.1 foi revisitada para integrar as novas regras de negócio e sanitização de dados.

### Mudanças Implementadas:
1.  **Sanitização de Username:** Introduzida restrição para permitir apenas caracteres alfanuméricos (A-Z, 0-9) e tamanho mínimo de 3 caracteres. Isso evita problemas de injeção no sistema de arquivos.
2.  **Travas de Senha:** Implementada verificação de tamanho mínimo (8 caracteres) e proibição de senhas idênticas ao username.

### Resultados da Rodada de Testes de Re-validação:
*   **Teste de Sucesso:** Usuário `bob123` cadastrado com êxito.
*   **Teste de Simbolismo:** Rejeição correta do username `user_!@#` com mensagem de erro amigável.
*   **Teste de Força:** Rejeição correta de senha curta (`123`).
*   **Conclusão:** O sistema de cadastro agora é **Schema-Compliant**, garantindo integridade estrutural além da segurança criptográfica já validada anteriormente.

---
**Próximo Passo Imediato:** Iniciar a implementação da classe `AuthService.java` com o método de registro.
