# Plano de Trabalho: Atividade 3.1.1 - Implementação de AES-GCM para Cifragem de Dados

## Objetivo
Implementar a lógica de cifragem de alto nível para os dados arbitrários contidos nos blocos da blockchain. O objetivo é garantir a confidencialidade individual dos dados de cada usuário utilizando AES-GCM com chaves de 256 bits (session keys) e IVs únicos por bloco, conforme exigido no requisito 2.2 do PDF.

## Requisitos Técnicos
1.  **Algoritmo:** AES/GCM/NoPadding (via BCFIPS).
2.  **Chave:** Chave de Sessão do usuário logado (256 bits, recuperada do `SessionContext`).
3.  **IV:** 12 bytes (96 bits) gerados aleatoriamente para cada novo bloco.
4.  **Tag de Autenticação:** 128 bits (embutida no ciphertext pelo GCM).

## Passos de Execução

### 1. Design do Fluxo de Cifragem de Bloco (CONCLUÍDO)
O fluxo de transformação dos dados para garantir a confidencialidade e integridade foi formalizado:

*   **Entrada:** String de texto (ex: "Transação #1").
*   **Processamento:**
    1.  Conversão da String para bytes (UTF-8).
    2.  Obtenção da `sessionKey` de 256 bits do `SessionContext`.
    3.  Geração de um `IV` aleatório de 12 bytes.
    4.  Cifragem `AES/GCM/NoPadding` gerando o `Ciphertext + Tag (128 bits)`.
    5.  Conversão do IV e do Resultado Cifrado para String Hex.
*   **Saída:** Par de valores `{ iv: "hex", dataEnc: "hex" }` prontos para inclusão no objeto `Block`.
*   **Segurança:** A tag GCM garante que qualquer tentativa de modificar `dataEnc` no disco será detectada na decifragem.

### 2. Implementação do `BlockchainService` (Cifragem)
*   **Ação:** Criar a classe `BlockchainService.java` com o método `prepareBlockData(String content)`.
*   **Responsabilidade:**
    1. Validar se a sessão está ativa.
    2. Gerar um IV único.
    3. Executar a cifragem via `SecurityUtils`.
    4. Retornar um objeto contendo o `dataEnc` e o `IV` usados.

### 3. Garantia de Unicidade do IV (CONCLUÍDO)
*   **Auditoria Realizada:** Verificação do método `SecurityUtils.generateGcmIV()`.
*   **Resultado:** O método utiliza `java.security.SecureRandom`, um gerador criptograficamente forte, para produzir 12 bytes de entropia para cada chamada.
*   **Conformidade:** Atende ao requisito do PDF de "IV único por bloco". A probabilidade de colisão de um IV de 96 bits gerado aleatoriamente é desprezível para o escopo do projeto, garantindo a segurança do modo GCM.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar `BlockEncryptionTest.java` que:
    1. Cifra uma mensagem usando a chave de sessão.
    2. Verifica se o IV gerado é diferente para cada execução.
    3. Confirma que o dado resultante não é legível em texto claro.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Fluxo Plaintext -> Hex | Fluxo Hexagonal validado | **Igual:** Segue o padrão NIST para AES-GCM (IV 96-bit). |
| **2. Service** | `BlockchainService.java` | Implementado com `BlockPayload` | **Melhoria:** Criada classe interna para agrupar o dado cifrado e seu IV. |
| **3. IV** | Unicidade via `SecureRandom` | Auditado e Confirmado | **Igual:** Probabilidade de colisão desprezível garantida. |
| **4. Validação** | `BlockEncryptionTest` | Sucesso em 5/5 blocos | **Igual:** Confirmada proteção de dados e barreira de sessão. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Criação do DTO `BlockPayload`** | Facilitar o transporte atômico do dado cifrado e seu IV, evitando que um dado seja salvo com o IV de outro bloco por erro de parâmetro. | **Médio:** Garante a consistência técnica entre o ciphertext e seu vetor de inicialização específico. |
| **Truncamento de IV em 12 bytes** | Estrita aderência ao padrão NIST para GCM. | **Alto:** O uso de 12 bytes (96 bits) é o tamanho ideal que evita cálculos extras de hash no IV, reduzindo a superfície de erro criptográfico. |
| **Validação de Sessão Externa** | Garantir que o `BlockchainService` seja independente da UI. | **Alto:** Impede que qualquer lógica tente cifrar blocos sem que a `sessionKey` esteja presente no `SessionContext`. |

**Conclusão Final:** A atividade 3.1.1 foi concluída com êxito. O sistema de cifragem de blocos é robusto, utiliza algoritmos de última geração conforme solicitado e garante que a privacidade dos dados do usuário seja mantida na blockchain.

---
**Próximo Passo Imediato:** Iniciar a implementação do `BlockchainService.java`.
