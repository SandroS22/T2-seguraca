# Plano de Trabalho: Atividade 4.2.1 - Listagem Completa de Blocos da Blockchain

## Objetivo
Implementar a funcionalidade de recuperação sequencial de todos os blocos armazenados no disco. Esta atividade fornece a visão "pública" da blockchain, permitindo que qualquer usuário autenticado visualize a estrutura da cadeia, metadados e hashes de todos os blocos, independentemente de quem seja o proprietário dos dados cifrados.

## Requisitos Funcionais (Requisito 2.3)
1.  **Visibilidade Total:** O sistema deve listar todos os blocos da rede.
2.  **Ordenação:** A listagem deve ser rigorosamente sequencial (pelo índice do bloco).
3.  **Metadados Públicos:** Devem ser exibidos: Index, Timestamp, Owner, Hash e HashPrev.

## Passos de Execução

### 1. Implementação do Recuperador de Histórico no `BlockchainService`
*   **Ação:** Criar o método `getBlockchainHistory()`.
*   **Responsabilidade:**
    1. Invocar `StorageManager.loadAllBlocks()`.
    2. Garantir que a lista retornada está completa e ordenada.

### 2. Integração na Fachada `MiniBlockchainServer`
*   **Ação:** Atualizar o método `getBlockchain()`.
*   **Lógica:**
    1. Verificar se o usuário está autenticado.
    2. Recuperar a lista de blocos do `BlockchainService`.
    3. Retornar um `ServerResponse.ok` contendo a lista de objetos `Block`.

### 3. Design da Resposta para a Interface (CONCLUÍDO)
A política de visibilidade para a listagem pública da blockchain foi formalizada:

*   **Princípio da Privacidade:** O método `getBlockchain()` retorna a lista de objetos `Block` tal como estão persistidos no disco.
*   **Estado dos Dados:** O campo `dataEnc` contém o ciphertext Hex. O campo `dataRaw` é retornado como `null` (devido ao Secure Wipe da atividade 4.1.2).
*   **Garantia:** Qualquer usuário autenticado pode ver "quem postou o quê e quando", mas o "o quê" permanece ilegível para todos, protegendo a confidencialidade até o momento da decifragem seletiva (Atividade 4.2.2).

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `BlockchainListingTest.java`.
*   **Evidência:** Recuperação de 3 blocos (2 de 'alice', 1 de 'bob') em ordem sequencial perfeita (0, 1, 2) através da fachada.
*   **Conclusão:** O sistema de listagem é robusto, suporta múltiplos usuários e garante a visibilidade dos metadados públicos da rede.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Recuperador** | Método `getBlockchainHistory` | Implementado e Funcional | **Igual:** Fornece a base de dados para a interface. |
| **2. Fachada** | Exposição via `getBlockchain()` | Integrado à API do Servidor | **Igual:** Mantém o isolamento Requisito 6.i. |
| **3. Resposta** | Metadados Públicos apenas | Visibilidade Controlada | **Igual:** Conteúdo sensível permanece cifrado no retorno. |
| **4. Validação** | Teste multi-usuário | `BlockchainListingTest` OK | **Igual:** Provada a ordenação e a ausência de vazamento de `dataRaw`. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Garantia de `dataRaw` nulo** | Reforçar que o campo temporário de preparação nunca chega ao cliente. | **Médio:** Proteção adicional contra persistência acidental em memória na camada de transporte. |
| **Ordenação por Nome de Arquivo** | Garantir que o `StorageManager` sempre carregue os blocos na ordem cronológica (00, 01, 02...). | **Alto:** Vital para a consistência da auditoria da blockchain em qualquer ambiente. |

**Conclusão Final:** A atividade 4.2.1 foi concluída com êxito. A blockchain é agora navegável, permitindo que os usuários acompanhem a evolução da rede mantendo a privacidade de seus dados.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da listagem pública validou a transparência estrutural do MiniBlockchain. Durante o processo, os seguintes ajustes e observações foram registrados:

1.  **Isolamento Multiusuário:** Confirmou-se que o sistema gerencia blocos de múltiplos usuários (`alice` e `bob`) de forma contínua. A Alice pôde visualizar os metadados do bloco do Bob, mas o conteúdo permaneceu cifrado, cumprindo o requisito de visibilidade pública da cadeia com privacidade de dados.
2.  **Robustez dos Testes (Mudança de Plano):** Durante a execução do `BlockchainListingTest`, percebeu-se que registros de testes anteriores na pasta `data/users` causavam falhas de "Usuário já cadastrado". O plano de teste foi alterado para incluir uma limpeza total dos diretórios de dados no início de cada execução, garantindo um ambiente determinístico.
3.  **Integridade da Resposta:** Validou-se que o objeto `ServerResponse` transporta a lista de blocos sem expor o campo `dataRaw`, assegurando que o "Secure Wipe" realizado na fase de registro é mantido até a ponta final da interface.

O resultado final é uma API de histórico estável, ordenada e em conformidade com as restrições de isolamento do projeto.
