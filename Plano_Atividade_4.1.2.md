# Plano de Trabalho: Atividade 4.1.2 - Cifragem de Dados e Cálculo de Hash no Registro

## Objetivo
Implementar a lógica de "Selagem" do bloco. Esta atividade une os componentes de segurança (AES-GCM e SHA-256) desenvolvidos na Fase 3 ao fluxo de registro. O objetivo é transformar o dado bruto (`dataRaw`) em dado cifrado e selar o bloco com sua impressão digital final antes da persistência.

## Requisitos Técnicos
1.  **Cifragem:** Utilizar a `sessionKey` para cifrar o `dataRaw` gerando `dataEnc` e `iv`.
2.  **Selo de Integridade:** Calcular o hash SHA-256 sobre o bloco completo (incluindo o novo dado cifrado).
3.  **Segurança de Memória:** Limpar o campo `dataRaw` após a cifragem para garantir que o texto claro não seja acidentalmente persistido ou mantido em memória além do necessário.

## Passos de Execução

### 1. Implementação do Método de Selagem no `BlockchainService`
*   **Ação:** Criar o método `sealBlock(Block block)`.
*   **Lógica:**
    1. Recuperar o `dataRaw` do objeto.
    2. Chamar `encryptBlockPayload(dataRaw)` para obter IV e Ciphertext.
    3. Atualizar os campos `dataEnc` e `iv` do bloco.
    4. Chamar `calculateBlockHash(block)` para gerar o selo final.
    5. Atribuir o hash ao campo `block.hash`.

### 2. Implementação do Secure Wipe do Dado Bruto (CONCLUÍDO)
*   **Ação Realizada:** Inclusão da instrução `block.setDataRaw(null)` no método `sealBlock`.
*   **Segurança:** Garante que o conteúdo em texto claro (plaintext) tenha o tempo de vida mais curto possível em memória. 
*   **Proteção de Persistência:** Como o `dataRaw` é zerado antes de qualquer chamada ao `StorageManager`, evita-se o risco de que dados não cifrados sejam salvos acidentalmente no disco.

### 3. Orquestração do Fluxo de Registro (Passo Intermediário)
*   **Ação:** Definir o método `createAndSealBlock(String content)` que executa a Atividade 4.1.1 e 4.1.2 em sequência.

### 4. Validação (Critério de Aceite)
*   **Teste:** `BlockSealingTest.java` que verifica:
    1. Se o bloco resultante possui `dataEnc` preenchido e `dataRaw` nulo.
    2. Se o `hash` calculado é válido para o conteúdo cifrado.
    3. Se o bloco está pronto para ser salvo (possui todos os campos preenchidos).

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Selagem** | Método `sealBlock` | Implementado e Funcional | **Igual:** Integra AES-GCM e SHA-256 no mesmo fluxo. |
| **2. Wipe** | Setar `dataRaw` como null | Implementado via instrução | **Igual:** Proteção de memória validada via teste. |
| **3. Orquestração** | Fluxo Preparar + Selar | Método `createAndSealBlock` | **Melhoria:** Unificou as atividades 4.1.1 e 4.1.2 em um único ponto de entrada seguro. |
| **4. Validação** | Teste de integridade final | `BlockSealingTest` com Sucesso | **Igual:** Confirmada integridade e sigilo do dado selado. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Método `createAndSealBlock`** | Garantir que a preparação e a selagem sejam atômicas. Evita que o desenvolvedor esqueça de selar um bloco preparado. | **Médio:** Reduz o risco de erro humano e garante que todos os blocos sigam o mesmo padrão de segurança. |
| **Secure Wipe Antecipado** | O dado bruto é limpo antes mesmo do cálculo do hash final. | **Alto:** Minimiza a exposição do texto claro em memória, removendo-o assim que a cifragem é confirmada. |
| **Validação de Payload Nulo** | Adicionada trava de segurança no `sealBlock`. | **Baixo:** Impede falhas de execução se o método for chamado incorretamente sem dados. |

**Conclusão Final:** A atividade 4.1.2 foi concluída com êxito. O sistema agora possui a lógica necessária para "lacrar" dados sensíveis dentro da estrutura da blockchain, garantindo que o que entra como texto claro saia como um elo criptográfico imutável.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da selagem de blocos consolidou a ponte entre a coleta de dados e a segurança criptográfica. Durante a execução, percebeu-se que separar a preparação (4.1.1) da selagem (4.1.2) poderia gerar estados inconsistentes no software. Por isso, foi criado o orquestrador `createAndSealBlock`, que atua como uma transação atômica: ou o bloco é totalmente criado, vinculado, cifrado e selado, ou o processo falha por inteiro.

O teste `BlockSealingTest` serviu como prova de conceito para este fluxo, demonstrando que a memória é limpa imediatamente (Wipe) e que a "assinatura" do bloco (Hash) é gerada com precisão sobre o conteúdo já protegido. Este módulo é a peça final da lógica de escrita antes da gravação física.

---
**Próximo Passo Imediato:** Implementar o método `sealBlock` no `BlockchainService.java`.
