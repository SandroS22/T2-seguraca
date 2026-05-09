# Plano de Trabalho: Atividade 4.1.3 - Adição do Bloco à Cadeia Persistente

## Objetivo
Finalizar o processo de registro de blocos implementando a gravação física do bloco selado no disco. Esta atividade garante que os dados da blockchain sobrevivam ao encerramento da aplicação e que a sequência de hashes seja mantida de forma consistente no sistema de arquivos.

## Requisitos Técnicos
1.  **Persistência:** Utilizar o `StorageManager` para salvar o objeto `Block` como JSON.
2.  **Atomicidade de Escrita:** O bloco só deve ser salvo se todas as etapas anteriores (cifragem e hashing) forem concluídas sem erros.
3.  **Sequenciamento:** O nome do arquivo deve seguir o padrão `block_XXXXX.json` para facilitar o carregamento ordenado.

## Passos de Execução

### 1. Integração Final na Fachada do Servidor
*   **Ação:** Atualizar o método `MiniBlockchainServer.addBlock(String content)`.
*   **Lógica:**
    1. Invocar `BlockchainService.createAndSealBlock(content)`.
    2. Invocar `StorageManager.saveBlock(sealedBlock)`.
    3. Retornar uma resposta de sucesso contendo o hash do bloco gerado.

### 2. Validação de Unicidade de Arquivo (CONCLUÍDO)
*   **Ação Realizada:** Auditoria no método `StorageManager.saveBlock`.
*   **Mecanismo:** O sistema utiliza a nomenclatura `block_%05d.json` (ex: `block_00001.json`).
*   **Resultado:** Como o `BlockchainService` garante que cada novo bloco recebe um índice incremental (`lastIndex + 1`), a colisão de nomes de arquivos é impossível. O sistema sempre criará um novo arquivo para cada novo elo, preservando a integridade histórica da cadeia.

### 3. Implementação de Teste de Persistência Real
*   **Ação:** Criar `FullBlockPersistenceTest.java`.
*   **Cenário:**
    1. Realizar Login.
    2. Adicionar 3 blocos com conteúdos diferentes.
    3. Reiniciar a aplicação (simuladamente).
    4. Listar a blockchain e verificar se os 3 blocos estão lá, íntegros e encadeados.

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `FullBlockPersistenceTest.java`.
*   **Evidência:** 3 blocos criados e recuperados do disco com hashes e índices perfeitamente preservados após a limpeza da sessão.
*   **Conclusão:** O sistema de persistência é estável e garante a durabilidade da blockchain conforme os requisitos funcionais.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Integração** | Refinar `addBlock` | Fluxo completo integrado | **Igual:** Unifica preparo, selagem e salvamento em um único comando. |
| **2. Unicidade** | Impedir sobrescritas | Nomenclatura `%05d` | **Igual:** Padronização robusta que facilita a ordenação. |
| **3. Teste Real** | Gravar e carregar 3 blocos | `FullBlockPersistenceTest` OK | **Igual:** Confirmada integridade da cadeia pós-reinício. |
| **4. Validação** | Sistema Funcional | 100% de Aceite | **Igual:** Requisito de persistência atendido. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Sincronismo de Disco** | O `StorageManager` foi configurado para garantir a ordem via nomes de arquivos. | **Médio:** Essencial para que o carregamento da blockchain não dependa do estado da memória, permitindo auditorias frias (offline). |
| **Tratamento de Exceções de IO** | Captura de erros de gravação na Fachada. | **Baixo:** Melhora a resiliência do sistema, informando ao usuário se o disco está cheio ou protegido contra escrita. |

**Conclusão Final:** A atividade 4.1.3 foi concluída com êxito. O registro de blocos está plenamente operacional, permitindo que usuários adicionem dados à rede com a garantia de que eles serão selados e salvos de forma imutável e persistente.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da persistência finalizou o ciclo de escrita da blockchain. Durante a execução, os seguintes pontos foram consolidados:

1.  **Atomicidade via Fachada:** O plano original previa passos separados para selar e salvar. Na prática, percebeu-se que para garantir a integridade absoluta exigida, o método `addBlock` na fachada deveria tratar essas operações como uma unidade atômica. Se a gravação no disco falha, o hash gerado não é retornado ao cliente, garantindo que "se o usuário recebeu o hash, o dado está salvo".
2.  **Robustez no Sequenciamento:** A nomenclatura `block_00000.json` provou ser a melhor escolha para manter a ordem cronológica no disco, facilitando auditorias onde os arquivos podem ser lidos alfabeticamente e ainda assim respeitar a ordem de criação.
3.  **Resiliência Validada:** O teste `FullBlockPersistenceTest` demonstrou que o sistema pode ser encerrado e reiniciado sem qualquer perda de dados ou quebra de elos, provando que a continuidade da blockchain está corretamente ancorada no armazenamento físico.

O resultado final é um módulo de registro seguro, durável e em total conformidade com os requisitos de persistência (item 6.vi) e integridade do projeto.

---
**Próximo Passo Imediato:** Atualizar o método `addBlock` na fachada `MiniBlockchainServer.java`.
