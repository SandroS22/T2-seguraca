# Plano de Trabalho: Atividade 5.2.2 - Testes de Integridade (Simulação de Alteração)

## Objetivo
Realizar uma rodada exaustiva de testes de "Red Teaming" para validar a capacidade do sistema em detectar adulterações em qualquer parte da blockchain. O foco é garantir que nenhuma alteração nos arquivos JSON (dados cifrados ou metadados) passe despercebida pelo auditor estrutural ou pela camada criptográfica.

## Cenários de Ataque (Simulação)
1.  **Adulteração de Payload:** Modificar 1 bit do `dataEnc` (Deve falhar na decifragem GCM).
2.  **Adulteração de IV:** Modificar o vetor de inicialização (Deve falhar na decifragem GCM).
3.  **Quebra de Vínculo:** Modificar o `hashPrev` de um bloco (Deve falhar na auditoria estrutural).
4.  **Reordenação de Blocos:** Trocar o conteúdo de dois arquivos de bloco (Deve falhar no índice/hash).
5.  **Ataque de Recálculo (Perfect Crime):** Modificar o dado e atualizar o hash do próprio bloco (Deve ser barrado pela Tag GCM).

## Passos de Execução

### 1. Implementação do `IntegrityStressTest.java` (CONCLUÍDO)
*   **Resultado:** Classe implementada com a automação de 6 cenários de ataque (5 planejados + 1 extra).
*   **Diferença:** Adicionado o **Ataque 6 (Gênese)** para cobrir a base da confiança da rede.

### 2. Validação de Mensagens e Logs (CONCLUÍDO)
*   **Resultado:** Sincronização realizada entre as exceções do `BlockchainService` e as capturas do `IntegrityStressTest`.
*   **Diferença:** Foi necessário padronizar a string de erro para `[ERRO DE INTEGRIDADE]` para evitar falsos negativos nos testes automatizados devido a case-sensitivity e colchetes.

### 3. Teste de Blindagem Gênese (CONCLUÍDO)
*   **Resultado:** Sucesso na detecção de alteração no `hashPrev` do bloco zero.
*   **Diferença:** Integrado como o sexto cenário da suite principal para facilitar a execução repetível.

### 4. Validação Final (CONCLUÍDO)
*   **Resultado:** 100% de cobertura nos ataques simulados.

---
## Resultados Obtidos vs. Planejado (Ataques)

| Ataque | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Payload** | Bloqueio GCM | PASS | **Igual:** Detectado na decifragem seletiva. |
| **2. IV** | Bloqueio GCM | PASS | **Igual:** Quebra a integridade da Tag GCM. |
| **3. Vínculo** | Falha Auditoria | PASS | **Igual:** O hash recalculado revelou a troca do pai. |
| **4. Ordem** | Falha Sequência | PASS | **Igual:** Identificado pela falha de continuidade de índice. |
| **5. Recálculo** | Falha GCM | PASS | **Igual:** Prova que Recalcular Hash != Burlar Segurança. |
| **6. Gênese** | N/A (Extra) | PASS | **Melhoria:** Validada a imutabilidade da raiz da rede. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Sincronização de Strings de Erro** | Garantir que o teste detectasse a mensagem exata independentemente da formatação JSON. | **Baixo:** Melhoria técnica no código de teste para evitar erros de comparação. |
| **Busca de Bloco por ID na Listagem** | O teste inicial falhava ao buscar por índice de array; mudou-se para busca por ID real do bloco. | **Médio:** Torna o teste imune a reordenações acidentais na resposta do servidor. |
| **Inclusão da Verificação Gênese** | Requisito fundamental de qualquer blockchain é a imutabilidade da raiz. | **Alto:** Garante que o ataque de "Substituição de Cadeia" seja detectado no primeiro elo. |

**Conclusão Final:** A atividade 5.2.2 foi concluída com êxito. O MiniBlockchain possui agora uma validação de integridade auditada em 6 níveis, garantindo resiliência contra ataques de dados e estrutura.

---
## Resumo Narrativo de Resultados e Mudanças

A execução dos testes de estresse de integridade foi a validação definitiva da imutabilidade da rede. Durante o processo, os seguintes marcos foram alcançados:

1.  **Impenetralidade do "Crime Perfeito":** O maior êxito foi provar que o recálculo do SHA-256 é insuficiente para fraudar o sistema. Mesmo com um hash estruturalmente válido, o sistema barrou a leitura através da Camada 2 (GCM), provando que o segredo da chave de sessão é o guardião final da integridade.
2.  **Monitoramento Ativo:** O teste confirmou que toda tentativa de fraude gerou logs detalhados, permitindo uma auditoria forense precisa sobre qual bloco e qual proprietário foram alvo de manipulação.
3.  **Resiliência Estrutural:** A detecção de furos e reordenações validou o algoritmo de continuidade, garantindo que a história da blockchain não possa ser reescrita sem quebras óbvias na rede.

O resultado é uma blockchain tecnicamente imutável que atende e supera todos os requisitos de segurança e auditoria do projeto.
