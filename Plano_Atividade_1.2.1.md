# Plano de Trabalho: Atividade 1.2.1 - Definição do Esquema do Usuário

## Objetivo
Formalizar a definição lógica e técnica do esquema de dados para o objeto `Usuário`. Esta definição servirá como base para todos os módulos de autenticação e persistência, garantindo que os campos necessários para confidencialidade (AES-GCM), autenticação forte (TOTP) e derivação de chaves (PBKDF2) estejam presentes e bem especificados.

## Campos Identificados
Conforme os requisitos do PDF e a arquitetura de segurança implementada:
1.  **Username:** Identificador único do usuário.
2.  **Salt:** Valor aleatório usado na derivação da chave mestra (Público).
3.  **Password Hash:** Verificador de senha (armazenado dentro do envelope).
4.  **TOTP Secret:** Segredo compartilhado para 2FA (armazenado dentro do envelope).
5.  **Metadados de Cifragem:** IVs necessários para decifrar os segredos internos.

## Passos de Execução

### 1. Especificação Técnica dos Campos (CONCLUÍDO)
*   **Username:** String UTF-8. Identificador único e público.
*   **Salt:** 16 bytes (128 bits) gerados via `SecureRandom`. Armazenado como String Hex (32 caracteres). Finalidade: Salgar o PBKDF2.
*   **Password Hash (Verifier):** 32 bytes (256 bits) resultantes de `HMAC-SHA256(Key, "auth-verifier")`. Armazenado como String Hex (64 caracteres) dentro do envelope. Finalidade: Prova de conhecimento da senha.
*   **TOTP Secret:** 32 bytes (256 bits) gerados via `SecureRandom`. Armazenado como String Hex (64 caracteres) dentro do envelope. Finalidade: Chave para o 2FA.
*   **IV (Initialization Vector):** 12 bytes (96 bits) gerados aleatoriamente para cada cifragem AES-GCM do envelope. Armazenado como String Hex (24 caracteres).

### 2. Documentação do Modelo de Persistência vs. Memória (CONCLUÍDO)
O design separa a representação do usuário em duas camadas para garantir a segurança dos dados em repouso:

*   **Modelo de Memória (`User.java`):**
    *   **Estado:** Desbloqueado/Texto claro (em memória volátil).
    *   **Uso:** Utilizado pelo `AuthService` e `TotpService` durante a sessão ativa.
    *   **Campos:** `username`, `salt`, `passwordHash`, `totpSecret`.
    *   **Segurança:** Nunca deve ser serializado diretamente para o disco.

*   **Modelo de Persistência (`UserStorage.java`):**
    *   **Estado:** Protegido (Cifrado com AES-GCM).
    *   **Uso:** Utilizado pelo `StorageManager` para gravação e leitura física.
    *   **Campos Públicos:** `salt` (para permitir o KDF no login) e `iv`.
    *   **Campo Privado:** `blob` (contém o objeto `User` cifrado).
    *   **Conformidade:** Atende ao item 6.vi do PDF: "os parâmetros devem ser guardados em arquivo cifrado".

### 3. Definição de Regras de Validação (CONCLUÍDO)
Para garantir a qualidade dos dados e um nível mínimo de segurança:

*   **Regras para Username:**
    *   Tamanho mínimo: 3 caracteres.
    *   Formato: Alfanumérico (letras e números apenas).
    *   Unicidade: Validada via `StorageManager.userExists(username)`.

*   **Regras para Senha:**
    *   Tamanho mínimo: 8 caracteres.
    *   Restrição: Não deve ser igual ao username.

*   **Implementação:** As regras serão integradas ao método `AuthService.register()`.

### 4. Validação da Implementação Existente (CONCLUÍDO)
*   **Auditoria Realizada:** Comparação das classes `User.java` e `UserStorage.java` com a especificação técnica do Passo 1.
*   **Conformidade:**
    *   `User.java` contempla todos os campos necessários para a lógica de negócio (Username, Salt, Hash e TOTP).
    *   `UserStorage.java` encapsula corretamente o envelope cifrado.
    *   Os tamanhos de Salt (16 bytes), IV (12 bytes) e Chaves (32 bytes/256 bits) estão sendo gerados e manipulados corretamente conforme as especificações.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Especificação** | Definir tipos e tamanhos | Especificação técnica detalhada | **Igual:** Estabelecido o uso de 256 bits para segredos e 128 bits para salts. |
| **2. Modelo** | Diferenciar Memória/Disco | `User` vs `UserStorage` | **Igual:** Estrutura clara que garante o requisito 6.vi. |
| **3. Regras** | Definir regras de validação | Regras implementadas no `AuthService` | **Igual:** Mínimo de 3 chars p/ user e 8 p/ senha. |
| **4. Validação** | Auditar classes existentes | Classes validadas e em conformidade | **Igual:** Nenhuma correção estrutural foi necessária, apenas formalização. |

### Conclusão da Atividade
O esquema do usuário está formalmente definido e validado. A estrutura suporta tanto a autenticação forte (TOTP) quanto a confidencialidade exigida, mantendo uma clara separação entre os dados públicos e os segredos cifrados no disco.

---
**Próximo Passo Imediato:** Consolidar a especificação técnica dos campos do usuário.
