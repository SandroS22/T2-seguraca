# Plano de Trabalho: Atividade 2.2.2 - Validação de Segundo Fator (TOTP via HMAC)

## Objetivo
Implementar o segundo estágio do processo de login (2FA). O sistema deve validar um código TOTP de 6 dígitos baseado no segredo decifrado do usuário no estágio anterior. A sessão só será estabelecida se ambos os fatores forem confirmados, garantindo a autenticação forte.

## Impacto das Atividades 1.2.3 e 2.1.2 (Atualização)
O design foi refinado para uma arquitetura baseada em estados dentro da fachada:
- **Coordenação (1.2.3):** A lógica agora reside em `MiniBlockchainServer.loginStep2()`, que coordena o fluxo entre `AuthService` e `TotpService`.
- **Estado Pendente:** O TOTP só é validado se houver um usuário em estado `pendingUser` (que já passou pela senha).
- **Consistência Técnica (2.1.2):** Utiliza o `TotpService` validado que aplica HMAC-SHA256 e janelas de 30 segundos.

## Requisitos Técnicos
1.  **Entrada:** Código TOTP de 6 dígitos.
2.  **Validação de Estado:** Verificar se o Passo 1 foi concluído (existência de `pendingUser` no servidor).
3.  **Lógica de Validação:** Comparar o código fornecido com o calculado sobre o segredo decifrado.
4.  **Finalização:** Se válido, mover a chave mestra para `SessionContext` e limpar o estado temporário.

## Passos de Execução

### 1. Formalização do Login Estágio 2 na Fachada
*   **Ação:** Refinar o método `MiniBlockchainServer.loginStep2(String code)`.
*   **Segurança:** Garantir que o estado pendente seja limpo em caso de múltiplas falhas ou timeout (limpeza manual nesta fase).

### 2. Blindagem da Chave de Sessão (CONCLUÍDO)
*   **Verificação Realizada:** Auditoria no fluxo de transferência de chaves.
*   **Resultado:** A `SecretKey` derivada no Passo 1 reside na variável privada `pendingKey` do Servidor. Ao sucesso do TOTP no Passo 2, ela é injetada no `SessionContext.setSession()` e a variável local é setada como `null`.
*   **Isolamento:** Em nenhum momento a chave cruza a fronteira da Fachada para o Cliente. O objeto `ServerResponse` retornado ao final contém apenas mensagens textuais de sucesso.

### 3. Validação de Ponta a Ponta
*   **Teste:** Executar `FullLoginTest.java` cobrindo cenários de sucesso, código expirado e tentativa de pular o Passo 1.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Fachada** | Refinar `loginStep2` | Implementado com sucesso | **Melhoria:** Adicionada limpeza automática de estado em caso de erro para impedir brute force. |
| **2. Blindagem** | Transferência interna de chave | Handover 100% encapsulado | **Igual:** A chave de sessão nunca deixa o contexto do servidor. |
| **3. Validação** | Teste MFA completo | `FullLoginTest` com Sucesso | **Igual:** Validado sucesso, bypass impedido e limpeza de estado pós-erro. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Política de Tentativa Única (TOTP)** | Originalmente o plano não previa a limpeza de estado imediata. Decidiu-se zerar o `pendingUser` ao primeiro erro de TOTP. | **Crítico:** Impede ataques de força bruta contra o código de 6 dígitos. O atacante é forçado a re-enviar a senha, tornando o ataque inviável. |
| **Bloqueio de Bypass do Passo 1** | Garantir a ordem lógica do MFA. | **Alto:** Assegura que o servidor não processe validações de TOTP sem que uma identidade tenha sido pré-verificada via senha. |
| **Limpeza Interna de Referências** | Cumprir o requisito de não manter dados sensíveis expostos. | **Médio:** Garante que, ao final do login ou em caso de erro, referências temporárias à `SecretKey` sejam destruídas (nullified). |

**Conclusão Final:** A atividade 2.2.2 foi concluída com êxito. O sistema agora possui um processo de autenticação forte em dois fatores, blindado contra tentativas de subversão e em total conformidade com o protocolo de comunicação estabelecido.

---
**Próximo Passo Imediato:** Implementar o método `authenticateStep2` no `AuthService.java`.
