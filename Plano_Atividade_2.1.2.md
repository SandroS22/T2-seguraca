# Plano de Trabalho: Atividade 2.1.2 - Geração e Armazenamento do Segredo TOTP

## Objetivo
Implementar a geração segura do segredo TOTP (Time-based One-Time Password) para cada usuário e definir a lógica de cálculo do código de autenticação baseada em HMAC e janelas de tempo. Este segredo deve ser armazenado exclusivamente de forma cifrada dentro do envelope do usuário.

## Requisitos Técnicos
1.  **Algoritmo de Base:** HMAC-SHA256 (conforme mapeado no Exemplo 4).
2.  **Segredo:** 256 bits gerados via `SecureRandom`.
3.  **Janela de Tempo (Step):** 30 segundos (padrão de mercado para TOTP).
4.  **Cálculo:** `Code = HMAC(Secret, CurrentUnixTime / 30)`.
5.  **Segurança:** Armazenamento cifrado via AES-GCM (cumprindo item 6.vi do PDF).

## Passos de Execução

### 1. Refinamento da Geração do Segredo
*   **Ação:** Garantir que o método `SecurityUtils.generateTotpSecret()` produz segredos com entropia suficiente.
*   **Implementação:** Já iniciada na 2.1.1, será validada aqui.

### 2. Implementação da Lógica de Cálculo TOTP
*   **Ação:** Criar a classe `TotpService.java` ou adicionar métodos em `SecurityUtils.java`.
*   **Lógica:** 
    1. Obter o tempo atual em segundos.
    2. Dividir por 30 para obter o "contador" de tempo.
    3. Calcular o HMAC-SHA256 do contador usando o segredo do usuário.
    4. Truncar/formatar o resultado para um código numérico de 6 dígitos.

### 3. Integração no Fluxo de Cadastro
*   **Ação:** Garantir que no momento do `AuthService.register()`, o segredo seja gerado e exibido ao usuário (simulando a leitura de um QR Code) antes de ser cifrado e salvo.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar `TotpTest.java` que:
    1. Gera um segredo.
    2. Calcula o código para o tempo T.
    3. Verifica se o código muda após 30 segundos.
    4. Verifica se o mesmo código é gerado para o mesmo segredo dentro da mesma janela de tempo.


---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Geração** | Segredo com alta entropia | 256 bits via `SecureRandom` | **Igual:** Implementado com 32 bytes, garantindo segurança robusta. |
| **2. Lógica** | Cálculo TOTP (HMAC + Time) | `TotpService` implementado | **Igual:** Segue RFC 6238 com truncamento dinâmico. |
| **3. Integração** | Retorno no Cadastro | `AuthService.register()` | **Melhoria:** O segredo é entregue via `ServerResponse` pela fachada (1.2.3). |
| **4. Validação** | `TotpTest` funcional | Sucesso em janelas e fachada | **Igual:** Validado tanto isoladamente quanto via protocolo. |

---

## Re-validação e Impacto da Arquitetura (1.2) e Cadastro (2.1.1)

A atividade 2.1.2 foi impactada positivamente pela formalização da infraestrutura do sistema:

### Impactos e Mudanças:
1.  **Formalização do Segredo (1.2.1):** O tamanho de 256 bits para o segredo TOTP deixou de ser uma decisão isolada e tornou-se parte do **Esquema Oficial do Usuário**, garantindo consistência em todo o projeto.
2.  **Isolamento via Fachada (1.2.3):** O fluxo de entrega do segredo agora ocorre estritamente via `MiniBlockchainServer`. Isso garante que o segredo decifrado nunca "vaze" para variáveis globais do cliente, cumprindo o requisito **6.i**.
3.  **Sanitização (2.1.1):** A geração do segredo TOTP agora está atrelada a um processo de cadastro sanitizado, impedindo a criação de credenciais 2FA para identidades malformadas.

### Resultados da Rodada de Re-validação:
*   **Consistência Temporal:** `TotpTest` validou que os códigos são idênticos na mesma janela e mudam após 30s.
*   **Fluxo de 2FA Seguro:** `MiniBlockchainServerTest` confirmou que um cliente CLI recebe o segredo hex, gera o código externamente e consegue se autenticar com sucesso sem nunca tocar na chave de cifragem real.
*   **Conclusão:** A funcionalidade de TOTP está plenamente integrada e protegida pela arquitetura de camadas do sistema.

---
**Próximo Passo Imediato:** Implementar a lógica de cálculo do código TOTP baseada em tempo.
