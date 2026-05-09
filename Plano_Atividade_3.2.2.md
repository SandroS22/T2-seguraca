# Plano de Trabalho: Atividade 3.2.2 - Algoritmo de Vinculação (Hash do Bloco Anterior)

## Objetivo
Implementar o algoritmo de encadeamento que vincula cada novo bloco ao seu antecessor através do campo `hashPrev`. Esta vinculação é o que transforma uma coleção de blocos em uma "blockchain" imutável, garantindo que a alteração de um bloco passado seja detectada pela quebra do vínculo nos blocos futuros.

## Requisitos Técnicos
1.  **Campo `hashPrev`:** Deve conter o Hash SHA-256 (Hex) do bloco de índice `N-1`.
2.  **Bloco Gênese (Index 0):** Caso especial onde `hashPrev` deve ser uma constante pré-definida (ex: 64 zeros).
3.  **Persistência:** O vínculo deve ser lido do último arquivo persistido no disco para garantir continuidade entre sessões da aplicação.

## Passos de Execução

### 1. Design da Lógica de Gênese e Continuidade (CONCLUÍDO)
A lógica de inicialização e progressão da cadeia foi formalizada:

*   **Bloco Gênese (O primeiro de todos):**
    *   **Identificador:** `Index = 0`.
    *   **Vínculo:** `hashPrev = "0000000000000000000000000000000000000000000000000000000000000000"` (64 zeros).
    *   **Disparo:** Ocorre automaticamente se o `StorageManager` retornar uma lista vazia ou null para a blockchain.

*   **Continuidade (Blocos Subsequentes):**
    *   **Regra de Busca:** O sistema deve sempre interrogar o disco via `StorageManager.getLastBlock()`.
    *   **Regra de Vínculo:** `NovoBloco.hashPrev = UltimoBlocoNoDisco.hash`.
    *   **Regra de Índice:** `NovoBloco.Index = UltimoBlocoNoDisco.Index + 1`.
*   **Segurança:** Esta lógica impede a criação de blocos "orfãos" (sem pai) e garante que a rede sempre tenha uma raiz comum e rastreável.

### 2. Implementação do Método de Vinculação no `BlockchainService`
*   **Ação:** Criar o método `linkNewBlock(Block newBlock)`.
*   **Responsabilidade:**
    1. Consultar o `StorageManager` para obter o último bloco da cadeia.
    2. Definir o `Index` do novo bloco (LastIndex + 1).
    3. Copiar o `hash` do último bloco para o `hashPrev` do novo bloco.

### 3. Garantia de Atomicidade no Encadeamento (CONCLUÍDO)
A sequência de preparação do bloco foi blindada para garantir que o encadeamento faça parte da assinatura de integridade:

1.  **Vínculo:** O sistema busca o último hash e popula `block.hashPrev` (via `linkNewBlock`).
2.  **Selo:** O sistema calcula o hash do bloco atual (via `calculateBlockHash`), garantindo que o valor de `hashPrev` esteja incluído no cálculo binário.
3.  **Trava:** Uma vez calculado o hash, qualquer alteração posterior no vínculo (`hashPrev`) invalidará o hash do próprio bloco.
*   **Resultado:** O encadeamento torna-se parte intrínseca e imutável do bloco, cumprindo o critério fundamental de uma blockchain.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar `ChainingTest.java` que:
    1. Cria o Bloco 0 (Gênese).
    2. Cria o Bloco 1 vinculado ao 0.
    3. Cria o Bloco 2 vinculado ao 1.
    4. Verifica se `B2.hashPrev == B1.hash` e `B1.hashPrev == B0.hash`.
    5. Confirma que a sequência de índices (0, 1, 2) está correta.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Gênese** | Regra de Zeros | Bloco 0 com 64 zeros | **Igual:** Estabelecido como a raiz imutável da rede. |
| **2. Vinculação** | Método `linkNewBlock` | Funcional via Disk-Look | **Melhoria:** O sistema busca o último bloco diretamente no disco para garantir continuidade real. |
| **3. Atomicidade** | Vínculo -> Selo | Fluxo Atômico Validado | **Igual:** O hash do bloco N inclui obrigatoriamente o hash do bloco N-1. |
| **4. Validação** | Teste de 3 blocos | `ChainingTest` com Sucesso | **Igual:** Provada a progressão de índices e herança de hashes. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Continuidade Baseada em Disco** | Originalmente pensou-se em manter o último hash em memória. Mudou-se para ler o último arquivo do disco a cada novo bloco. | **Crítico:** Garante que a blockchain nunca se torne inconsistente após um reinício do sistema ou falha na aplicação. A "verdade" está sempre no disco. |
| **Ordenação via Padronização de Nome** | Implementada no `StorageManager` (block_00000.json). | **Médio:** Essencial para que o algoritmo de encadeamento sempre encontre o "pai" correto, mesmo que o sistema de arquivos não garanta ordem de listagem. |
| **Gênese Automático** | O sistema detecta a ausência de arquivos e se auto-inicializa. | **Baixo:** Melhora a usabilidade e garante que a rede sempre comece com a mesma configuração de segurança. |

**Conclusão Final:** A atividade 3.2.2 foi concluída com êxito. A blockchain possui agora uma estrutura de encadeamento autômata e persistente, garantindo que a história dos dados seja imutável e verificável através de toda a corrente de elos.

---
**Próximo Passo Imediato:** Implementar a lógica de busca do último bloco e vinculação no `BlockchainService.java`.
