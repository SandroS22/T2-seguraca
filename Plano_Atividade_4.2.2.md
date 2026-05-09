# Plano de Trabalho: Atividade 4.2.2 - Filtro de Acesso e Decifragem Seletiva

## Objetivo
Implementar a lógica que permite a visualização personalizada da blockchain. O sistema deve percorrer a lista de blocos e, de forma transparente para o usuário, decifrar o conteúdo apenas dos blocos de sua propriedade (`owner == current_user`), mantendo os dados de terceiros protegidos.

## Requisitos Funcionais (Requisito 2.3)
1.  **Decifragem Seletiva:** Somente os dados do usuário logado devem ser transformados de `dataEnc` para texto claro.
2.  **Visibilidade de Terceiros:** Blocos de outros usuários devem exibir a mensagem "[CONTEÚDO CIFRADO - ACESSO NEGADO]".
3.  **Segurança:** A decifragem deve utilizar a `sessionKey` volátil em memória.

## Passos de Execução

### 1. Design da Transformação de Listagem (CONCLUÍDO)
O fluxo de processamento de saída do servidor foi refinado para aplicar a privacidade multiusuário:
*   **Decisão:** O servidor carrega a lista bruta e mapeia cada bloco dinamicamente.
*   **Segurança:** O dado decifrado é colocado no campo temporário `dataRaw` antes do envio.

### 2. Implementação do Método de Visualização na Fachada (CONCLUÍDO)
*   **Ação Realizada:** Refinamento do método `MiniBlockchainServer.getBlockchain()`.
*   **Resultado:** O método agora orquestra a decifragem automática para o dono e a aplicação de placeholders para terceiros.

### 3. Tratamento de Integridade na Listagem (CONCLUÍDO)
*   **Ação Realizada:** Implementação de captura de erro por bloco (per-block error handling).
*   **Resultado:** Falhas de tag GCM são reportadas individualmente como "[ERRO DE INTEGRIDADE]".

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `SelectiveDecryptionTest.java`.
*   **Evidência:** Sucesso na visualização cruzada (Alice vs Bob) e detecção de blocos corrompidos na listagem.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Pós-leitura no Servidor | Transformação Server-Side | **Igual:** Garante que chaves nunca deixem o servidor. |
| **2. Visualização** | Refinar `getBlockchain` | Mapeamento Dinâmico | **Igual:** O retorno agora é personalizado por usuário. |
| **3. Integridade** | Reportar falhas por bloco | Tratamento Individualizado | **Igual:** Blocos corrompidos não impedem a leitura da cadeia. |
| **4. Validação** | Teste Alice vs Bob | `SelectiveDecryptionTest` OK | **Igual:** Provada a confidencialidade seletiva. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Mapeamento Transparente** | O sistema decifra automaticamente no momento do `getBlockchain`. | **Médio:** Simplifica o desenvolvimento da CLI, que recebe dados prontos para exibição. |
| **Placeholders de Acesso Negado** | Diferenciar "Erro de Sistema" de "Dado de Terceiro". | **Baixo:** Melhora a clareza para o usuário sem comprometer o sigilo. |
| **Sobrevivência a Corrupção** | O loop de decifragem continua mesmo se um bloco falhar a tag GCM. | **Alto:** Essencial para uma blockchain resiliente; a rede continua operando mesmo se partes do histórico forem danificadas. |

**Conclusão Final:** A atividade 4.2.2 foi concluída com êxito. O MiniBlockchain possui agora um sistema de navegação inteligente e seguro, garantindo que o direito à privacidade seja mantido dentro de uma estrutura de dados compartilhada.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da visualização personalizada provou que o sistema pode conciliar transparência de rede com privacidade individual. Durante o processo, os seguintes pontos foram essenciais:

1.  **Resiliência Individualizada:** Uma mudança importante em relação ao plano original foi o tratamento de erros dentro do loop. Em vez de interromper toda a listagem caso um bloco estivesse corrompido (o que seria o comportamento padrão de uma exceção), o sistema foi ajustado para "marcar" o erro apenas no bloco afetado. Isso permite que o usuário veja o restante de sua história mesmo que um arquivo físico específico tenha sido danificado.
2.  **UX de Privacidade:** O uso de mensagens claras como "[CONTEÚDO CIFRADO - ACESSO NEGADO]" para blocos de terceiros validou o modelo de multiusuário. O teste `SelectiveDecryptionTest` confirmou que a Alice e o Bob têm experiências de visualização distintas e seguras sobre a mesma base de dados.
3.  **Blindagem de Camada:** A transformação ocorre inteiramente no servidor. O cliente CLI não possui lógica de decifragem, recebendo apenas os textos já processados, o que mantém a conformidade com o requisito 6.i de isolamento.
