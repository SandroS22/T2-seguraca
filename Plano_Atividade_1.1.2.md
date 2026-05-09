# Plano de Trabalho: Atividade 1.1.2 - Importação e Adaptação dos Códigos de Exemplo

## Objetivo
Isolar as funcionalidades criptográficas essenciais dos arquivos de exemplo e consolidá-las em uma classe utilitária centralizada (`SecurityUtils.java`) dentro do projeto `MiniBlockchain`. Isso garantirá que o código seja limpo, modular e siga rigorosamente os algoritmos permitidos.

## Resultados por Passo

### 1. Mapeamento de Métodos (CONCLUÍDO)
*   **Ação:** Análise dos exemplos `Exemplo 4`, `Exemplo 9` e `Exemplo 10`.
*   **Funções Mapeadas:**
    *   `PBKDF2WithHmacSHA512` (Exemplo 10) -> Para derivação de chaves a partir de senhas.
    *   `AES/GCM/NoPadding` (Exemplo 9) -> Para criptografia autenticada de blocos.
    *   `HMacSHA256` (Exemplo 4) -> Para cálculo de integridade e suporte ao TOTP.

### 2. Criação da Classe `SecurityUtils.java` (CONCLUÍDO)
*   **Finalidade:** Prover uma API de alto nível para operações criptográficas, garantindo o uso do provedor `BCFIPS`.
*   **Parâmetros Técnicos Definidos:**
    *   **Algoritmo KDF:** PBKDF2 com HMAC-SHA512.
    *   **Iterações KDF:** 10.000 (equilíbrio entre segurança e performance).
    *   **Tamanho da Chave:** 256 bits (AES-256).
    *   **Modo AES:** GCM (Galois/Counter Mode).
    *   **Tamanho da Tag (MAC) GCM:** 128 bits (16 bytes) - Padrão para detecção de adulteração.
    *   **Tamanho do IV GCM:** 96 bits (12 bytes) - Recomendado pelo NIST para eficiência.
    *   **Salt:** 128 bits (16 bytes) - Aleatório por usuário.

### 3. Adaptação da Classe `Utils.java` para `BlockchainUtils.java` (CONCLUÍDO)
*   **Finalidade:** Centralizar manipulações de tipos de dados sem dependências externas (exceto o encoder nativo da Bouncy Castle).
*   **Métodos Implementados:**
    *   `toHex` / `fromHex`: Conversão binário <-> texto hexadecimal.
    *   `strToBytes` / `bytesToStr`: Conversão String (UTF-8) <-> binário.
    *   `concatenate`: Agrupamento de múltiplos arrays de bytes para criação de hashes de blocos.

### 4. Teste de Unidade Integrado (CONCLUÍDO)
*   **Validação:** Execução bem-sucedida do `SecurityTest.java`.
*   **Evidências:**
    *   **Round-trip:** Mensagem cifrada e decifrada com sucesso (mesmo conteúdo original).
    *   **Integridade:** GCM bloqueou corretamente a decifragem após a alteração de 1 bit no ciphertext (`mac check in GCM failed`).
    *   **FIPS:** Provedor registrado automaticamente via bloco `static` na `SecurityUtils`.

## Resumo de Classes Criadas
*   `SecurityUtils.java`: O "motor" criptográfico. Gerencia chaves, IVs e cifras.
*   `BlockchainUtils.java`: O "tradutor" de dados. Facilita a visualização e transporte de informações.
*   `SecurityTest.java`: O "validador". Garante que as engrenagens de segurança estão funcionando antes da montagem da blockchain.

---
**Status Final:** Atividade 1.1.2 concluída com sucesso e documentada. Próximo passo: Fase 1.1.3 (Persistência).
