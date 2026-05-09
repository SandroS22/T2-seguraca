# Plano de Trabalho: Atividade 4.1.1 - Coleta de Dados e Timestamping

## Objetivo
Implementar a etapa inicial do registro de blocos, focada na captura segura de dados do usuário e na atribuição de uma marca temporal (timestamp) precisa. Esta fase prepara o conteúdo bruto para ser processado pelas camadas de criptografia e integridade desenvolvidas anteriormente.

## Requisitos Funcionais (Requisito 2.2)
1.  **Dados Arbitrários:** O sistema deve aceitar qualquer string como conteúdo (ex: transação, mensagem).
2.  **Timestamp:** Cada bloco deve conter o momento exato de sua criação (milissegundos desde a época Unix).
3.  **Owner:** O sistema deve atribuir automaticamente o ID do usuário logado como dono do conteúdo.

## Passos de Execução

### 1. Definição do Método de Preparação Bruta
*   **Ação:** Criar no `BlockchainService` um método para iniciar a montagem de um bloco.
*   **Lógica:** Receber a String de dados e capturar `System.currentTimeMillis()`.

### 2. Integração com Contexto de Sessão (CONCLUÍDO)
*   **Ação Realizada:** Inclusão de trava de segurança no `prepareNewBlock`.
*   **Segurança:** O sistema utiliza `SessionContext.isLoggedIn()` para impedir coletas anônimas.
*   **Automatização:** O campo `owner` é preenchido obrigatoriamente com o username da sessão ativa, impedindo que um usuário registre blocos em nome de outro.

### 3. Implementação do Fluxo de Pré-Bloco (CONCLUÍDO)
*   **Ação Realizada:** Consolidação do método `prepareNewBlock()` no `BlockchainService`.
*   **Resultado:** O método agora retorna um objeto `Block` pronto para receber os dados cifrados, já possuindo `Index`, `Timestamp`, `Owner` e `HashPrev` devidamente configurados.

### 4. Validação (Critério de Aceite)
*   **Teste:** `DataCollectionTest.java` que verifica:
    1. Se o timestamp gerado é coerente com o horário atual.
    2. Se o owner corresponde ao usuário logado.
    3. Se o índice sugerido segue a sequência da cadeia existente.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Coleta** | Método de montagem bruta | Implementado com `dataRaw` | **Melhoria:** Adicionado campo temporário para segurar o dado antes da cifragem. |
| **2. Sessão** | Extração de `owner` | Automático via `SessionContext` | **Igual:** Bloqueio de coletas anônimas validado. |
| **3. Fluxo** | Objeto `Block` populado | `prepareNewBlock` funcional | **Igual:** Integração com Gênese e Continuidade OK. |
| **4. Validação** | Teste de metadados | `DataCollectionTest` com Sucesso | **Igual:** Confirmado sequenciamento de índices e timestamps. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Inclusão do campo `dataRaw`** | Necessidade de transportar o conteúdo em texto claro do momento da coleta até o momento da cifragem dentro do mesmo objeto. | **Baixo:** O campo não é incluído na serialização JSON e serve apenas como buffer de memória temporário. |
| **Integração Prematura do Encadeamento** | Decidiu-se chamar `linkNewBlock` já no preparo, em vez de esperar a fase de selo. | **Médio:** Garante que o bloco "nasça" sabendo seu lugar na fila, evitando conflitos de concorrência simples. |
| **Uso de Millis como String** | Padronizar todos os metadados do bloco como Strings para o JSON. | **Baixo:** Facilita o determinismo do hash sem perda de precisão temporal. |

**Conclusão Final:** A atividade 4.1.1 foi concluída com êxito. O sistema de preparação de blocos é automatizado e garante que todo novo dado seja devidamente carimbado com tempo, dono e posição na cadeia.

---
**Próximo Passo Imediato:** Implementar o método `prepareNewBlock` no `BlockchainService.java`.
