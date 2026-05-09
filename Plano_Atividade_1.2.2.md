# Plano de Trabalho: Atividade 1.2.2 - Definição do Esquema do Bloco

## Objetivo
Formalizar a definição técnica e lógica do esquema de dados para o objeto `Bloco`. Esta definição é o alicerce da blockchain, garantindo a imutabilidade (via encadeamento de hashes), a confidencialidade dos dados (via AES-GCM) e o controle de acesso multiusuário (via campo owner).

## Campos Identificados
Conforme os requisitos do PDF:
1.  **Index:** Posição do bloco na cadeia (inteiro sequencial).
2.  **Timestamp:** Momento da criação do bloco (long).
3.  **DataEnc:** Dados arbitrários cifrados com AES-GCM (Hex).
4.  **IV:** Vetor de Inicialização único usado na cifragem do dado (Hex).
5.  **HashPrev:** Hash do bloco anterior na corrente (Hex).
6.  **Owner:** ID do usuário (username) proprietário do bloco.
7.  **Hash:** Hash SHA-256 do bloco atual (incluindo todos os campos acima).

## Passos de Execução

### 1. Especificação Técnica dos Campos (CONCLUÍDO)
*   **Index:** Inteiro (armazenado como String). Indica a ordem cronológica e sequencial do bloco na cadeia.
*   **Timestamp:** Long (milissegundos desde Unix Epoch). Registra o momento exato da criação do bloco.
*   **DataEnc:** String Hex. Contém os dados cifrados com AES-GCM (Ciphertext + 128-bit Tag). A integridade deste campo é verificada pelo GCM e pelo hash do bloco.
*   **IV:** 12 bytes (96 bits), representado em Hex. Deve ser único para cada bloco para garantir a segurança do modo GCM.
*   **HashPrev:** 32 bytes (256 bits), representado em Hex. Contém o hash SHA-256 do bloco imediatamente anterior. Para o bloco gênese, este valor será uma string de zeros.
*   **Owner:** String UTF-8. Armazena o `username` do proprietário dos dados cifrados.
*   **Hash:** 32 bytes (256 bits), representado em Hex. Impressão digital do bloco atual, garantindo a imutabilidade de todos os campos acima.

### 2. Definição da Lógica de Encadeamento (CONCLUÍDO)
A imutabilidade da blockchain é garantida pelo cálculo de um hash SHA-256 que vincula o conteúdo do bloco ao bloco anterior.

*   **Algoritmo:** SHA-256 (via provedor BCFIPS).
*   **Dados para o Hash (Concatenação):**
    `HashAtual = SHA256(Index + Timestamp + DataEnc + IV + HashPrev + Owner)`
*   **Regra de Ouro:** Se qualquer caractere em qualquer um desses campos for alterado, o `Hash` resultante mudará drasticamente, invalidando o campo `HashPrev` do bloco seguinte e quebrando toda a corrente a partir daquele ponto.

### 3. Especificação da Verificação de Integridade (CONCLUÍDO)
O algoritmo de auditoria deve percorrer a blockchain sequencialmente para garantir que nenhum dado foi alterado retrospectivamente.

*   **Processo de Auditoria:**
    1.  **Carregamento:** Recuperar todos os blocos do disco ordenados por índice.
    2.  **Verificação do Bloco Gênese (Index 0):** Validar seu próprio hash e garantir que `HashPrev` é o valor inicial padrão (ex: 64 zeros).
    3.  **Verificação de Encadeamento (Bloco N > 0):**
        *   Recalcular o hash do bloco N-1 usando a lógica do Passo 2.
        *   Garantir que `RecalculatedHash(N-1) == Bloco(N).HashPrev`.
        *   Garantir que `RecalculatedHash(N) == Bloco(N).Hash`.
    4.  **Verificação de Conteúdo (Opcional na Auditoria):** Verificar a tag GCM dos dados cifrados (isso ocorre no momento da decifragem por usuário).

*   **Resultado:** A auditoria retorna "Válida" apenas se todos os hashes de encadeamento e hashes próprios de cada bloco coincidirem perfeitamente com os dados persistidos.

### 4. Validação da Implementação Existente (CONCLUÍDO)
*   **Auditoria Realizada:** Verificação da classe `Block.java`.
*   **Melhoria:** Adicionado o método `getBytesForHash()` para consolidar a lógica de serialização necessária para o cálculo do SHA-256 de forma consistente.
*   **Conformidade:** A classe agora suporta nativamente a lógica de encadeamento e integridade especificada nos passos anteriores.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Especificação** | Definir tipos e tamanhos | Especificação técnica detalhada | **Igual:** Segue rigorosamente o PDF, usando 256 bits para hashes. |
| **2. Encadeamento** | Lógica de hash do bloco | Regra de concatenação definida | **Igual:** Implementada via `getBytesForHash()` na classe `Block`. |
| **3. Integridade** | Algoritmo de auditoria | Fluxo de verificação detalhado | **Igual:** Base para o futuro `BlockchainService`. |
| **4. Validação** | Auditar classe `Block` | Classe aprimorada e validada | **Melhoria:** A inclusão do método de bytes centralizou a lógica de hash. |

### Conclusão da Atividade
O esquema do bloco está formalmente definido e pronto para a implementação da blockchain. A estrutura garante que qualquer mudança no conteúdo ou metadados de um bloco será detectada imediatamente através da quebra da corrente de hashes.

---
## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Inclusão do método `getBytesForHash()`** | Necessidade de uma forma determinística de serializar os campos do bloco para o SHA-256. | **Alto:** Garante que a "impressão digital" do bloco seja calculada da mesma forma em qualquer ambiente, impedindo falsos negativos ou positivos na auditoria. |
| **Formatação do Index como String** | Facilitar a manipulação uniforme de todos os campos durante a concatenação de bytes. | **Médio:** Simplifica o código e evita problemas de endianness (ordenação de bytes) que poderiam ocorrer com tipos numéricos nativos. |
| **Uso de Hex para DataEnc** | Padronizar o armazenamento de dados binários (cifrados) dentro do JSON textual. | **Baixo:** Melhora a legibilidade dos arquivos de dados sem comprometer a segurança, já que o conteúdo permanece cifrado. |

**Nota Final:** A implementação superou o planejamento inicial ao centralizar a lógica de "preparação para hash" dentro do próprio objeto `Block`, tornando o sistema mais modular e menos propenso a erros de implementação em futuras fases.

---
**Próximo Passo Imediato:** Consolidar a especificação técnica dos campos do bloco.
