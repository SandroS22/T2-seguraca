# Plano de Trabalho: Atividade 5.1.1 - Desenvolvimento do Menu de Operações (CLI)

## Objetivo
Desenvolver a Interface de Usuário (CLI) do MiniBlockchain. Esta interface atuará como o "Cliente" do sistema, fornecendo um menu textual que permite ao usuário realizar todas as operações de cadastro, autenticação forte (2FA) e gerenciamento de blocos, interagindo exclusivamente com a fachada `MiniBlockchainServer`.

## Requisitos de Interface (Conforme PDF Item 4)
1.  **Menu Principal:** Opções numeradas para Cadastro, Login, Logout e Sair.
2.  **Menu Logado:** Opções para Adicionar Bloco, Listar Blockchain e Auditoria.
3.  **Fluxo de Login:** Capturar a senha e, se correta, solicitar o código TOTP de 6 dígitos.
4.  **Feedback:** Exibir mensagens de sucesso ou erro retornadas pelo "Servidor".

## Passos de Execução

### 1. Design do Loop Principal
*   **Ação:** Implementar a classe `Main.java` com um loop `while(true)` e o objeto `Scanner` para entrada de dados.
*   **Estado:** O menu deve alternar automaticamente entre "Modo Visitante" e "Modo Autenticado" baseando-se no `MiniBlockchainServer.isAuthenticated()`.

### 2. Implementação das Telas de Identidade
*   **Cadastro:** Solicitar username/senha e exibir o segredo TOTP gerado.
*   **Login Passo 1:** Solicitar username/senha. Chamar `loginStep1`.
*   **Login Passo 2 (2FA):** Se o Passo 1 for OK, solicitar o código de 6 dígitos. Chamar `loginStep2`.

### 3. Implementação das Operações de Blockchain
*   **Adição:** Solicitar string de conteúdo e chamar `addBlock`.
*   **Listagem:** Chamar `getBlockchain` e exibir a lista formatada (Atividade 5.1.2 será focada na formatação rica, aqui faremos a exibição básica).

### 4. Validação (Critério de Aceite)
*   **Teste:** Walkthrough manual cobrindo o ciclo completo:
    1. Cadastro de novo usuário.
    2. Login com erro proposital (senha errada).
    3. Login com sucesso (Senha + TOTP).
    4. Adição de 2 blocos.
    5. Logout.

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `SystemWalkthroughTest.java`.
*   **Evidência:** Fluxo completo (Cadastro -> Login -> Adição -> Listagem -> Auditoria -> Logout) validado com sucesso.
*   **Destaque:** Confirmada a detecção de blocos de terceiros e de adulterações históricas durante a navegação.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Loop** | Loop `while(true)` | Classe `Main` estável | **Melhoria:** Implementada troca dinâmica de menus baseada no estado real do servidor. |
| **2. Identidade** | Cadastro e Login MFA | Fluxo 2FA Interativo | **Correção:** Ajustada inicialização do `Scanner` (de erro de sintaxe para `System.in`). |
| **3. Blockchain** | Add e Listagem | Operações Integradas | **Igual:** Cliente recebe apenas dados processados, respeitando o isolamento. |
| **4. Validação** | Walkthrough manual | `SystemWalkthroughTest` | **Melhoria:** Teste automatizado que prova a integração de todas as fases do projeto. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Menus Dinâmicos (Visitante vs Logado)** | Impedir que o usuário tente acessar funções de blockchain sem ter passado pelo MFA de forma visual. | **Baixo:** Melhora a usabilidade, embora a segurança real esteja garantida pelos guardas da Fachada. |
| **Tratamento de Exceções Global** | Evitar que erros do servidor (como falha de integridade) derrubem a aplicação cliente. | **Médio:** Mantém o terminal operante mesmo após a detecção de adulterações críticas. |
| **Deduplicação da Classe Main** | Durante as edições, ocorreram inserções duplicadas de código; foi necessária uma limpeza total do arquivo. | **Baixo:** Essencial para a compilabilidade e clareza do código entregue. |

**Conclusão Final:** A atividade 5.1.1 foi concluída com êxito. A CLI é funcional, segura e atua como um verdadeiro cliente agnóstico, provando que a arquitetura "Cliente-Servidor" simulada foi bem-sucedida.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da interface marcou a unificação de todos os esforços do projeto. Durante a execução, o maior desafio foi garantir que o estado da sessão fosse respeitado em todos os submenus. A solução de menus dinâmicos provou ser eficaz, apresentando ao usuário apenas o que ele pode de fato realizar em cada momento. 

O teste `SystemWalkthroughTest` serviu como o "batismo de fogo" da aplicação, validando que um novo usuário pode entrar na rede, configurar seu TOTP e começar a registrar dados cifrados que nem mesmo administradores (sem a chave) podem ler. A correção do `Scanner` e a proteção contra falhas inesperadas garantiram que a aplicação seja resiliente para a entrega final.

---
**Próximo Passo Imediato:** Iniciar a estrutura básica da classe `Main.java`.
