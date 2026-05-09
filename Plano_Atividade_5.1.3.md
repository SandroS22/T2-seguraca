# Plano de Trabalho: Atividade 5.1.3 - Sistema de Logs de Operações e Erros

## Objetivo
Implementar um sistema de log centralizado para registrar eventos operacionais, tentativas de autenticação e erros críticos do sistema. Este log servirá como uma trilha de auditoria para depuração e monitoramento de segurança, garantindo a rastreabilidade das ações sem comprometer a confidencialidade dos dados sensíveis.

## Requisitos de Log (Item 3 das Funcionalidades Extras e Item 6.v)
1.  **Eventos Operacionais:** Registrar Cadastro, Login, Logout, Adição de Bloco e Auditoria.
2.  **Segurança:** Registrar falhas de autenticação e violações de integridade.
3.  **Proibição de Vazamento:** NUNCA registrar senhas, segredos TOTP, chaves de sessão ou IVs brutos no arquivo de log.
4.  **Persistência:** Gravar em arquivo textual (`MiniBlockchain/data/system.log`).

## Passos de Execução

### 1. Design da Infraestrutura de Log (CONCLUÍDO)
A arquitetura de registro de eventos foi formalizada com foco em auditoria e segurança:

*   **Formato da Linha de Log:**
    `[YYYY-MM-DD HH:MM:SS] [LEVEL] [USER] - MESSAGE`
*   **Níveis de Log:**
    *   `INFO`: Fluxo normal (ex: "Bloco adicionado").
    *   `WARN`: Avisos não fatais (ex: "Logout por inatividade").
    *   `ERROR`: Falhas técnicas (ex: "Erro ao ler arquivo JSON").
    *   `SECURITY`: Eventos críticos (ex: "Login bem-sucedido", "Falha de senha", "Violação de integridade detectada").
*   **Regra de Ouro (Confidencialidade):** O log deve conter apenas metadados e mensagens de status. É terminantemente proibido registrar conteúdos de `dataRaw`, `password`, `SecretKey` ou `IV` brutos.
*   **Localização:** `MiniBlockchain/data/system.log`.

### 2. Implementação da Classe `Logger.java`
*   **Ação:** Criar a classe utilitária em `MiniBlockchain/src`.
*   **Funcionalidade:** Método estático `log(level, message)` que anexa texto ao arquivo de log de forma segura.

### 3. Integração nos Serviços e Fachada
*   **Ação:** Inserir chamadas de log nos pontos críticos:
    *   `AuthService`: Registro e tentativa de KDF.
    *   `MiniBlockchainServer`: Sucesso/Falha de Login e Logout.
    *   `BlockchainService`: Criação de blocos e falhas de GCM/Hash.

### 4. Validação (Critério de Aceite)
*   **Teste:** `SystemLoggingTest.java` que:
    1. Realiza uma série de ações (sucessos e falhas deliberadas).
    2. Lê o arquivo `system.log`.
    3. Verifica se as ações foram registradas e confirma a ausência de strings de segredos no arquivo.

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `SystemLoggingTest.java`.
*   **Evidência:** Arquivo `system.log` gerado com todos os marcos operacionais (Registro, Login, Block, Auditoria, Logout).
*   **Segurança:** Confirmada a ausência de senhas e segredos TOTP no arquivo de log, cumprindo o requisito de não vazamento de dados sensíveis.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Formato de log padrão | Implementado com Níveis | **Igual:** Segue o padrão de auditoria profissional. |
| **2. Classe Logger** | Classe centralizada | `Logger.java` funcional | **Igual:** Automatiza timestamp e captura de usuário. |
| **3. Integração** | Chamadas nos serviços | Integrado em todo o Core | **Diferença:** Corrigida duplicação acidental na Fachada durante a integração. |
| **4. Validação** | Teste de registro e sigilo | `SystemLoggingTest` OK | **Igual:** Provado que o log é útil sem ser inseguro. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Nível de Log `SECURITY`** | Diferenciar eventos de fluxo comum de alertas críticos de integridade e acesso. | **Alto:** Facilita a identificação de ataques em andamento (ex: falhas seguidas de TOTP). |
| **Automação de Usuário** | Reduzir a complexidade das chamadas de log nos serviços. | **Baixo:** O Logger consulta o `SessionContext` sozinho, evitando erros de parâmetro manual. |
| **Correção de Classe (Fachada)** | Durante a integração, o arquivo `MiniBlockchainServer` sofreu uma corrupção de sintaxe; foi necessário restaurar e limpar o código. | **Crítico:** Garantir a compilabilidade do sistema para a entrega. |

**Conclusão Final:** A atividade 5.1.3 foi concluída com êxito. O MiniBlockchain possui agora uma trilha de auditoria completa, protegida e centralizada, essencial para a governança e segurança do sistema.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação do sistema de logs forneceu a visibilidade operacional necessária para o monitoramento da rede. Durante a execução, os seguintes pontos foram fundamentais:

1.  **Auditoria de Segurança Real:** O teste `SystemLoggingTest` provou que o sistema registra corretamente eventos críticos (como falhas de senha e acessos bem-sucedidos) com carimbos de tempo precisos. A inclusão do nível `SECURITY` permitiu destacar eventos que exigem atenção imediata.
2.  **Resiliência a Falhas Técnicas (Mudança de Percurso):** Durante o Passo 3, ocorreu uma corrupção acidental no arquivo `MiniBlockchainServer.java` devido a erros de edição parcial (`replace`). O plano foi alterado para incluir uma etapa de restauração e limpeza total da Fachada, garantindo que o sistema final fosse entregue com código limpo e sem duplicatas.
3.  **Garantia de Sigilo:** Auditou-se o arquivo `system.log` e confirmou-se que **nenhum segredo** (senhas, TOTP ou chaves) foi registrado. Isso validou o cumprimento do requisito de não-vazamento (item 6.v), tornando o log seguro para visualização por administradores de sistema.

O resultado é um módulo de monitoramento robusto que unifica a segurança técnica com a rastreabilidade operacional.
