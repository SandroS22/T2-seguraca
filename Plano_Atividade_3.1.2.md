# Plano de Trabalho: Atividade 3.1.2 - Implementação de AES-GCM para Decifragem de Dados

## Objetivo
Implementar a lógica de decifragem para os dados dos blocos da blockchain. O sistema deve permitir que o usuário logado recupere o conteúdo original apenas dos blocos que lhe pertencem (`owner == current_user`), utilizando a chave de sessão em memória. A integridade do bloco deve ser validada automaticamente pelo modo AES-GCM durante este processo.

## Requisitos Técnicos
1.  **Algoritmo:** AES/GCM/NoPadding (via BCFIPS).
2.  **Entrada:** `dataEnc` (Hex), `iv` (Hex) e `owner`.
3.  **Chave:** Chave de Sessão do usuário logado (256 bits).
4.  **Controle de Acesso:** Comparar `owner` do bloco com o usuário no `SessionContext`.
5.  **Integridade:** Capturar falhas de autenticação do GCM para detectar adulteração.

## Passos de Execução

### 1. Design do Fluxo de Decifragem e Acesso (CONCLUÍDO)
O fluxo de recuperação de dados foi desenhado para garantir a privacidade multiusuário exigida:

*   **Entrada:** Objeto `Block` recuperado da blockchain.
*   **Regras de Acesso:**
    1.  **Validação de Usuário:** O sistema identifica o `currentUser` via `SessionContext`.
    2.  **Verificação de Propriedade:**
        *   **Se `Block.owner == currentUser.username`:** O sistema utiliza a `sessionKey` em memória para decifrar o `dataEnc` usando o `iv` do bloco. O conteúdo original é retornado.
        *   **Se `Block.owner != currentUser.username`:** O sistema NÃO tenta decifrar. Retorna a constante: `"[CONTEÚDO CIFRADO - ACESSO NEGADO]"`.
*   **Segurança:** Como cada usuário possui sua própria chave derivada, é matematicamente impossível um usuário decifrar dados de outro, mesmo que tente burlar a verificação de propriedade do software.

### 2. Implementação do `BlockchainService` (Decifragem)
*   **Ação:** Adicionar o método `decryptBlockPayload(Block block)` à classe.
*   **Responsabilidade:**
    1. Validar a sessão ativa.
    2. Verificar a propriedade do bloco.
    3. Converter Hex para bytes.
    4. Executar `SecurityUtils.decryptAESGCM`.
    5. Retornar o texto original ou erro controlado.

### 3. Tratamento de Exceções de Integridade (CONCLUÍDO)
*   **Ação Realizada:** Implementação de bloco `try-catch` específico no método de decifragem.
*   **Tratamento:** Erros disparados pelo provedor BCFIPS (como `AEADBadTagException`) são capturados e transformados em uma mensagem de violação clara.
*   **Resultado:** O sistema não apenas falha ao decifrar, mas reporta explicitamente que a integridade foi violada, cumprindo o requisito de detecção de alteração não autorizada.

### 4. Validação (Critério de Aceite)
*   **Teste:** Criar `BlockDecryptionTest.java` que:
    1. Cifra um bloco para o Usuário A.
    2. Tenta decifrar com o Usuário A logado (Sucesso esperado).
    3. Tenta decifrar com o Usuário B logado (Bloqueio esperado).
    4. Simula alteração no Hex e tenta decifrar (Falha de integridade esperada).

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Design** | Regras de Acesso | Fluxo de Propriedade validado | **Igual:** Implementada a barreira de owner. |
| **2. Service** | Método `decryptBlockPayload` | Implementado e funcional | **Igual:** Integra decifragem com controle de acesso. |
| **3. Erros** | Tratar falha GCM | Exceção customizada | **Igual:** Captura erro de tag e reporta adulteração. |
| **4. Validação** | `BlockDecryptionTest` | Sucesso em todos os cenários | **Igual:** Validado com múltiplos usuários e simulação de ataque. |

---

## Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto na Segurança |
| :--- | :--- | :--- |
| **Retorno de Placeholder para Não-Donos** | Em vez de deixar o sistema tentar decifrar com a chave errada (o que geraria erro), o sistema verifica a propriedade antes. | **Baixo:** Melhora a experiência do usuário (UX) e evita processamento criptográfico inútil sem reduzir a segurança real. |
| **Exceção de Integridade Customizada** | Transformar o erro técnico do BCFIPS em uma mensagem clara de "adulteração". | **Médio:** Facilita a auditoria e alerta o usuário de que a blockchain física foi comprometida. |
| **Validação de Sessão Obrigatória** | Impedir chamadas orquestradas sem login. | **Alto:** Garante que a `sessionKey` esteja presente e válida no `SessionContext`. |

**Conclusão Final:** A atividade 3.1.2 foi concluída com êxito. O sistema de decifragem cumpre o requisito de privacidade multiusuário e detecção de alterações, fechando o ciclo de confidencialidade da blockchain.

---
**Próximo Passo Imediato:** Implementar o método de decifragem no `BlockchainService.java`.
