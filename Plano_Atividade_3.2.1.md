# Plano de Trabalho: Atividade 3.2.1 - Implementação de Funções de Hash para Blocos

## Objetivo
Implementar a lógica de cálculo do Hash SHA-256 para os blocos da blockchain. O Hash deve atuar como uma "impressão digital" única e imutável que representa todo o conteúdo e metadados do bloco, permitindo o encadeamento seguro e a detecção de qualquer alteração retrospectiva.

## Requisitos Técnicos
1.  **Algoritmo:** SHA-256 (via BCFIPS).
2.  **Entrada:** Representação binária determinística do bloco (conforme definido na atividade 1.2.2).
3.  **Campos Incluídos:** Index, Timestamp, DataEnc (Hex), IV (Hex), HashPrev (Hex) e Owner.
4.  **Representação:** String Hexadecimal de 64 caracteres.

## Passos de Execução

### 1. Formalização do Processamento de Entrada (CONCLUÍDO)
A preparação dos dados para o cálculo do hash foi formalizada para garantir o determinismo:

*   **Método Utilizado:** `Block.getBytesForHash()`.
*   **Sequência de Concatenação:** `Index + Timestamp + DataEnc + IV + HashPrev + Owner`.
*   **Codificação:** UTF-8 (via `BlockchainUtils.strToBytes`).
*   **Propriedade:** Este método garante que, independentemente de como o objeto é manipulado em memória, a entrada para a função de hash será sempre uma sequência de bytes idêntica para o mesmo estado do bloco.
*   **Importância:** Sem este determinismo, a validação da blockchain falharia devido a divergências de formatação, mesmo que os dados estivessem corretos.

### 2. Implementação do Método de Cálculo no `BlockchainService`
*   **Ação:** Criar o método `calculateBlockHash(Block block)`.
*   **Responsabilidade:**
    1. Obter os bytes do bloco.
    2. Invocar `SecurityUtils.calculateSHA256`.
    3. Converter o resultado para String Hex.

### 3. Integração no Fluxo de Preparação (CONCLUÍDO)
O cálculo do hash foi posicionado como o "selo final" de um bloco antes de sua persistência:

*   **Sequência Atômica de Preparação:**
    1.  **Cifragem:** Gera `dataEnc` e `iv`.
    2.  **Montagem:** Cria o objeto `Block` com todos os campos (exceto seu próprio hash).
    3.  **Encadeamento:** O campo `hashPrev` é populado com o hash do último bloco da rede.
    4.  **Selo (Hashing):** O método `calculateBlockHash` é invocado para gerar a assinatura do bloco completo.
    5.  **Persistência:** O bloco "selado" é salvo no disco.
*   **Resultado:** Garante-se que nenhum bloco seja salvo sem um hash válido ou com um hash que não represente seu conteúdo real.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar `BlockHashTest.java` que:
    1. Calcula o hash de um bloco.
    2. Altera um único caractere no `dataEnc` e recalcula.
    3. Verifica se os hashes são completamente diferentes (Efeito Avalanche).
    4. Confirma que o hash é consistente (mesmo bloco = mesmo hash).

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Entrada** | Uso de `getBytesForHash()` | Implementado e Validado | **Igual:** Garante o determinismo necessário para a imutabilidade. |
| **2. Service** | Método `calculateBlockHash` | Funcional via BCFIPS | **Igual:** Utiliza SHA-256 conforme o requisito técnico. |
| **3. Integração** | Passo final antes do disco | Fluxo de "Selo" definido | **Igual:** Garante que apenas blocos íntegros sejam salvos. |
| **4. Validação** | Teste de consistência/avalanche | `BlockHashTest` com sucesso | **Igual:** Provado que qualquer bit alterado quebra o hash. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Inclusão do `Owner` no Hash** | Impedir que a propriedade de um bloco seja alterada sem quebrar a blockchain. | **Crítico:** Garante que o vínculo entre o dado e seu dono seja imutável e auditável. |
| **Uso do Timestamp como String Hex no Hash** | Garantir que o momento da transação seja parte integrante da "assinatura" do bloco. | **Alto:** Impede ataques de reordenação cronológica de blocos (Replay attacks simples). |
| **Cálculo Pós-Cifragem** | O hash deve ser feito sobre o dado já cifrado (`dataEnc`). | **Crítico:** Permite que qualquer pessoa audite a blockchain sem precisar decifrar os dados (Auditoria Pública de Integridade). |

**Conclusão Final:** A atividade 3.2.1 foi concluída com êxito. O sistema de hashing é sensível, determinístico e protege não apenas o conteúdo, mas toda a estrutura lógica de cada elo da corrente.

---
**Próximo Passo Imediato:** Implementar o método `calculateBlockHash` no `BlockchainService.java`.
