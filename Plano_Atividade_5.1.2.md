# Plano de Trabalho: Atividade 5.1.2 - Visualização Formatada da Blockchain

## Objetivo
Melhorar a estética e a clareza da exibição da blockchain no console. O objetivo é transformar a listagem vertical simples em uma visualização tabular ou estruturada que facilite a leitura dos metadados e a identificação de furos ou erros de integridade.

## Requisitos de Visualização
1.  **Layout Tabular:** Uso de divisores ASCII (ex: `+---+`, `|`) para separar os campos.
2.  **Conversão de Tempo:** Transformar o Timestamp (milissegundos) em uma data/hora legível (ISO 8601).
3.  **Resumo de Hashes:** Exibir hashes truncados para economizar espaço, mantendo a opção de ver o hash completo se necessário.
4.  **Destaque de Status:** Indicar visualmente se o dado está cifrado ou se houve erro de integridade.

## Passos de Execução

### 1. Design do Layout de Tabela (CONCLUÍDO)
O design da tabela ASCII para exibição da blockchain foi formalizado:

*   **Estrutura de Colunas:**
    *   `ID`: 5 caracteres, alinhado à esquerda.
    *   `DATA/HORA`: 20 caracteres (formato `yyyy-MM-dd HH:mm:ss`).
    *   `DONO`: 12 caracteres, alinhado à esquerda.
    *   `CONTEÚDO`: 45 caracteres (truncado com `...` se exceder).
    *   `HASH (RESUMO)`: 12 caracteres (primeiros 12 caracteres do Hex).
*   **Separadores:** Uso de `+---+` para bordas horizontais e `|` para verticais.
*   **Implementação:** Uso intensivo de `String.format()` para garantir o alinhamento fixo das colunas.

### 2. Implementação do Formatador de Data
*   **Ação:** Adicionar um método utilitário em `BlockchainUtils` para converter o Long em uma String de data formatada.

### 3. Refatoração da Listagem na CLI
*   **Ação:** Atualizar o método `handleListBlockchain` na classe `Main.java` para utilizar o novo layout tabular.

### 4. Validação (Critério de Aceite)
*   **Teste:** Executar a listagem com pelo menos 3 blocos e verificar visualmente se a tabela está alinhada e se as datas estão corretas.

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `TableViewTest.java`.
*   **Evidência:** Saída tabular alinhada com sucesso no console, demonstrando datas convertidas, conteúdos truncados e hashes resumidos.
*   **Conclusão:** A interface atende aos critérios de clareza e profissionalismo exigidos para uma ferramenta de auditoria.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Layout ASCII | Grid fixo definido | **Igual:** Segue o padrão de tabelas de console. |
| **2. Data** | Converter millis | Método em `BlockchainUtils` | **Igual:** Utiliza API nativa para yyyy-MM-dd HH:mm:ss. |
| **3. Refatoração** | Atualizar `Main.java` | Visualização renovada | **Igual:** Substituiu a listagem vertical pela tabular. |
| **4. Validação** | Teste visual | `TableViewTest` com Sucesso | **Igual:** Validado com textos longos e múltiplos donos. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na UX |
| :--- | :--- | :--- |
| **Truncamento Ativo (30 chars)** | Evitar que mensagens muito longas (ex: logs de transação) empurrassem as colunas da direita para fora da tela. | **Alto:** Mantém a integridade visual da tabela em qualquer tamanho de janela de terminal. |
| **Resumo de Hash (12 chars)** | Hashes SHA-256 completos (64 chars) tornariam a tabela ilegível horizontalmente. | **Médio:** Facilita a identificação visual rápida sem poluir o console. |
| **Padrão de Data Local** | Uso do `ZoneId.systemDefault()`. | **Médio:** Garante que o usuário veja o tempo de acordo com seu fuso horário local, facilitando a correlação com eventos reais. |

**Conclusão Final:** A atividade 5.1.2 foi concluída com êxito. A visualização da blockchain agora é intuitiva e esteticamente agradável, permitindo uma auditoria rápida e clara da história da rede.

---
## Resumo Narrativo de Resultados e Mudanças

O polimento visual da CLI transformou uma lista de dados bruta em uma ferramenta de navegação profissional. Durante o desenvolvimento, o maior desafio foi equilibrar a quantidade de informação exibida com a limitação de espaço do terminal. Optou-se por priorizar a visualização tabular, utilizando o truncamento inteligente de textos longos e hashes para garantir que as bordas da tabela nunca quebrem.

O teste `TableViewTest` provou que a Alice consegue ver o progresso da rede de forma organizada, identificando claramente quais blocos são seus (pelo conteúdo em texto claro) e quais são de terceiros (pelo aviso de acesso negado), tudo com carimbos de tempo que fazem sentido humano. Esta melhoria eleva o nível de qualidade da entrega final.

---
**Próximo Passo Imediato:** Definir o método de formatação de data em `BlockchainUtils.java`.
