# Plano de Trabalho: Atividade 5.2.1 - Testes de Fluxo de Autenticação

## Objetivo
Realizar uma bateria de testes exaustiva no Módulo de Autenticação para garantir que o sistema de dois fatores (MFA) seja impenetrável e resiliente a erros de entrada. O foco é validar que o acesso só é concedido sob condições perfeitas e que falhas são tratadas com segurança (limpeza de estado).

## Cenários de Teste (Requisito 4)
1.  **Sucesso Total:** Registro -> Login Passo 1 (OK) -> Login Passo 2 (OK) -> Sessão Ativa.
2.  **Usuário Inexistente:** Tentativa de login com username não cadastrado.
3.  **Senha Incorreta:** Username válido com senha errada (Deve barrar no Passo 1).
4.  **TOTP Incorreto:** Senha correta com código de 6 dígitos errado (Deve barrar no Passo 2 e limpar estado).
5.  **Bypass de Estágio:** Tentar chamar o Passo 2 sem ter executado o Passo 1.
6.  **Concorrência de Sessão:** Tentar logar com um usuário enquanto outro já possui sessão ativa.

## Passos de Execução

### 1. Implementação do Suite de Testes `AuthFlowTest.java`
*   **Ação:** Criar uma classe que automatize os 6 cenários descritos acima.
*   **Detalhe:** Usar asserts para validar cada `ServerResponse`.

### 2. Verificação de Limpeza de Estado
*   **Ação:** Garantir que, após cada falha, o servidor não mantenha rastros do usuário anterior em `pendingUser`.

### 3. Auditoria de Logs de Segurança
*   **Ação:** Verificar se o `system.log` registrou corretamente os eventos de `SECURITY` para as falhas induzidas.

### 4. Validação Final (Critério de Aceite)
*   **Resultado:** 100% de sucesso na execução dos cenários (sucessos permitidos, falhas bloqueadas).

### 4. Validação Final (CONCLUÍDO)
*   **Ação Realizada:** Execução final da suite `AuthFlowTest.java` após as correções.
*   **Evidência:** 6/6 cenários aprovados (PASS).
*   **Conclusão:** O módulo de autenticação é resiliente e garante que o acesso seja concedido apenas sob condições estritas, com proteção contra brute-force e concorrência de sessão.

---
## Resultados Obtidos vs. Planejado

| Cenário | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Sucesso** | Login OK | PASS | **Igual:** Fluxo Senha -> TOTP validado. |
| **2. Inexistente** | Erro 404 | PASS | **Igual:** Bloqueio imediato para usuários fantasmas. |
| **3. Senha Errada** | Bloqueio GCM | PASS | **Igual:** Falha de integridade do envelope impede o login. |
| **4. TOTP Errado** | Bloqueio HMAC | PASS | **Melhoria:** Confirmada a limpeza total do estado pendente pós-erro. |
| **5. Bypass** | Bloqueio Estado | PASS | **Igual:** Impossível enviar TOTP sem ter passado pela senha. |
| **6. Sobrescrita** | Bloqueio Sessão | PASS | **Diferença:** Detectada falha inicial; corrigido para barrar `loginStep1` se logado. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Bloqueio Ativo de Login Concorrente** | O teste inicial mostrou que o sistema permitia iniciar um segundo login enquanto um primeiro estava ativo. | **Alto:** Impede ataques de sequestro de sessão e garante que o contexto de memória pertença a um único usuário. |
| **Auditoria Integrada de `SECURITY`** | Validar que as falhas induzidas nos testes deixam rastros para o administrador. | **Médio:** Garante que o sistema de logs é confiável para perícia forense. |

**Conclusão Final:** A atividade 5.2.1 foi concluída com êxito. O sistema de autenticação forte está blindado e pronto para operação multiusuária segura.

---
## Resumo Narrativo de Resultados e Mudanças

A execução da bateria de testes de autenticação provou ser um marco crucial para a segurança do projeto. Durante o processo, os seguintes pontos foram fundamentais:

1.  **Detecção de Vulnerabilidade (Mudança de Plano):** O Cenário 6 revelou uma falha de design onde o sistema permitia o início de um novo fluxo de login mesmo com uma sessão já ativa. O plano original foi alterado para incluir uma correção imediata na fachada `MiniBlockchainServer`, que agora barra preventivamente qualquer tentativa de re-autenticação sem logout prévio. Isso garante a integridade do contexto de memória do usuário.
2.  **Eficiência da Limpeza de Estado:** Validou-se que qualquer erro no segundo fator (TOTP) dispara um "Secure Wipe" do estado pendente. O teste confirmou que um atacante que erre o código de 6 dígitos é forçado a recomeçar todo o processo, incluindo o fornecimento da senha, o que torna ataques de força bruta inviáveis.
3.  **Rastreabilidade:** A auditoria automática do arquivo `system.log` durante os testes confirmou que todas as violações induzidas (senhas erradas, códigos inválidos) geraram eventos de `SECURITY`, garantindo que o sistema é autovigilante.

O resultado final é um gateway de acesso que atende rigorosamente aos requisitos de autenticação forte e isolamento do PDF.
