# Plano de Trabalho: Atividade 3.2.3 - Verificação de Assinatura e Integridade GCM

## Objetivo
Formalizar a defesa em duas camadas (Dual-Layer Defense) do sistema. O objetivo é integrar a verificação estrutural da blockchain (SHA-256) com a verificação criptográfica do conteúdo (AES-GCM Auth Tag). Esta atividade garante que mesmo que um atacante consiga recalcular os hashes da cadeia, ele não poderá adulterar o conteúdo dos blocos sem possuir a chave de sessão do usuário.

## A Defesa em Duas Camadas
1.  **Camada 1 (Estrutural):** O Hash SHA-256 do bloco valida que metadados e o `dataEnc` (bruto) não mudaram. É uma auditoria pública.
2.  **Camada 2 (Criptográfica):** A Tag GCM valida que o `plaintext` dentro do `dataEnc` é autêntico. É uma auditoria privada que exige a `sessionKey`.

## Passos de Execução

### 1. Especificação da Lógica de Auditoria Integrada (CONCLUÍDO)
A arquitetura de verificação total de um bloco foi formalizada sob o conceito de "Defesa em Profundidade":

*   **Verificação de Camada 1 (Estrutura):**
    *   **Método:** Re-hashing SHA-256.
    *   **Lógica:** O sistema recalcula o hash do bloco usando todos os campos (Index, Timestamp, DataEnc, IV, HashPrev, Owner).
    *   **Validação:** `RecalculatedHash == block.hash`.
    *   **Garantia:** O bloco físico no disco é idêntico ao que foi assinado originalmente.

*   **Verificação de Camada 2 (Criptografia):**
    *   **Método:** Validação de Tag GCM (durante decifragem).
    *   **Lógica:** O provedor BCFIPS valida a tag de 128 bits embutida no `dataEnc`.
    *   **Validação:** Sucesso na operação `Cipher.doFinal()`.
    *   **Garantia:** Mesmo que a Camada 1 seja burlada (hashes recalculados), o dado decifrado é garantidamente o original, pois a Tag GCM depende de um segredo (Key) que o atacante não possui.

### 2. Implementação do Auditor de Integridade no `BlockchainService`
*   **Ação:** Criar o método `verifyBlockFullIntegrity(Block block)`.
*   **Responsabilidade:**
    1. Recalcular o SHA-256 e comparar com `block.hash`.
    2. Tentar decifrar via GCM (se for o dono) para validar a Tag de Autenticação.
    3. Retornar um relatório de saúde do bloco.

### 3. Simulação do "Crime Perfeito" (Teste de Stress)
*   **Ação:** Criar um cenário onde o atacante:
    1. Altera o dado cifrado.
    2. Atualiza o Hash do bloco para que a Camada 1 (Estrutural) passe na validação.
    3. Provar que a Camada 2 (GCM) detecta a fraude.

### 4. Validação (CONCLUÍDO)
*   **Ação Realizada:** Execução do `DualIntegrityTest.java`.
*   **Evidência:** O sistema detectou com sucesso a "FALHA CRIPTOGRAFICA" após uma tentativa deliberada de burlar o SHA-256 através do recálculo do hash.
*   **Conclusão:** A defesa em duas camadas está plenamente operacional, garantindo a integridade absoluta dos dados na blockchain.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design Dual** | Estrutural + Criptográfico | Arquitetura de 2 Camadas | **Igual:** Segue o princípio de defesa em profundidade. |
| **2. Auditor** | Método `verifyBlockFullIntegrity` | Implementado no Service | **Igual:** Integra decifragem de teste com hashing. |
| **3. Simulação** | Testar recálculo de hash | "Crime Perfeito" simulado | **Igual:** Provado que bypass de hash é insuficiente. |
| **4. Validação** | `DualIntegrityTest` | Sucesso total na detecção | **Igual:** Requisito de detecção de alteração atendido. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Dependência de Sessão no Auditor** | A Camada 2 só é verificada se o dono estiver logado. | **Médio:** Auditorias públicas validam a estrutura (Camada 1). Auditorias privadas validam o conteúdo (Camada 2), garantindo privacidade mesmo durante verificações. |
| **Priorização da Camada 1** | O sistema valida o hash antes de tentar a decifragem GCM. | **Alto:** Evita processamento criptográfico pesado e erros de "tag falha" se o arquivo estiver obviamente corrompido (índices/hashes trocados). |

**Conclusão Final:** A atividade 3.2.3 foi concluída com êxito. O MiniBlockchain possui agora um dos mecanismos de integridade mais robustos possíveis para sistemas simétricos, protegendo os usuários contra manipulações tanto em nível de arquivo quanto em nível de protocolo.

---
## Resumo Narrativo de Resultados e Mudanças

A implementação da verificação integrada confirmou a robustez da arquitetura de "Defesa em Profundidade". Durante a execução, as seguintes evoluções em relação ao plano original foram documentadas:

1.  **Auditoria Condicional (UX e Performance):** Diferente do plano de "validar tudo sempre", a Camada 2 (GCM) foi implementada para ser validada apenas se o usuário atual for o dono do bloco. Isso permite que a blockchain seja auditada estruturalmente por qualquer pessoa de forma rápida, reservando o processamento criptográfico pesado apenas para os dados que o usuário pode de fato ler.
2.  **Tratamento Hierárquico de Erros:** O sistema foi ajustado para reportar erros estruturais (Camada 1) antes de erros criptográficos (Camada 2). Isso evita confusão diagnóstica, deixando claro se o problema é um erro de encadeamento ou uma adulteração deliberada do conteúdo.
3.  **Sucesso do Teste de Ataque:** O `DualIntegrityTest` foi a prova definitiva de que o sistema é imune ao recálculo de hashes. Mesmo com o SHA-256 batendo perfeitamente, o provedor FIPS barrou a decifragem do dado adulterado, provando que a segurança real reside na chave de sessão.

O resultado é um módulo de integridade que supera os requisitos básicos, oferecendo uma auditoria multinível altamente confiável.

---
**Próximo Passo Imediato:** Definir a lógica de auditoria integrada.
