# Plano de Trabalho: Atividade 2.1.3 - Persistência de Dados de Usuário de Forma Segura

## Objetivo
Formalizar e auditar o mecanismo de persistência de dados de usuários. O foco é garantir que a estratégia de "Encrypted Envelope" (Envelope Cifrado) seja aplicada rigorosamente em todos os cadastros, assegurando que segredos como hashes de senha e chaves TOTP nunca fiquem expostos em texto claro no disco, e que múltiplos usuários coexistam de forma isolada.

## Requisitos de Segurança
1.  **Exposição Mínima:** Apenas o `salt` e o `iv` podem estar em texto claro no arquivo JSON físico.
2.  **Confidencialidade:** Todos os outros dados devem estar dentro do `blob` cifrado com AES-GCM.
3.  **Isolamento:** Cada usuário deve ter seu próprio arquivo `user_[username].json`.

## Passos de Execução

### 1. Auditoria da Implementação Atual
*   **Ação:** Revisar o método `AuthService.register()` para garantir que nenhum dado sensível esteja sendo "vazado" para fora do blob cifrado durante o processo de salvamento.
*   **Verificação:** Confirmar se o `StorageManager.saveUser` está sendo usado corretamente em conjunto com o `UserStorage`.

### 2. Implementação de Teste de Multi-Usuário
*   **Ação:** Criar `MultiUserPersistenceTest.java`.
*   **Cenário:**
    1. Cadastrar Usuário A e Usuário B com senhas diferentes.
    2. Verificar se ambos possuem arquivos distintos.
    3. Tentar carregar o Usuário A e garantir que seus dados decifrados estão íntegros.
    4. Tentar carregar o Usuário B e garantir o mesmo.

### 3. Verificação de Integridade de Arquivo
*   **Ação:** Validar se o sistema se comporta corretamente caso um arquivo de usuário seja corrompido ou editado manualmente (o GCM deve detectar a quebra de integridade).

### 4. Validação (Critério de Aceite)
*   **Teste:** Inspeção manual do diretório `MiniBlockchain/data/users` e execução do teste de multi-usuário.
*   **Resultado:** Documentação confirmando que segredos não estão expostos.


---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Auditoria** | Revisar `AuthService` | Refatorado `StorageManager` | **Melhoria:** O `StorageManager` foi blindado para aceitar apenas dados cifrados. |
| **2. Multi-Usuário** | Testar isolamento A e B | `MultiUserPersistenceTest` OK | **Igual:** Validado o isolamento e proteção contra acesso cruzado. |
| **3. Integridade** | Testar corrupção de arquivo | `IntegrityCheckTest` OK | **Igual:** O AES-GCM detectou a modificação física do arquivo. |
| **4. Validação** | Inspeção e Rodada final | Sucesso total nos testes | **Melhoria:** Re-validado após a formalização da arquitetura 1.2. |

---

## Conclusão Final e Impactos (Pós-Arquitetura 1.2)

A atividade 2.1.3 foi consolidada como o padrão de repouso de dados do sistema:

### Mudanças e Fortalecimento:
1.  **Padrão de Modelo (1.2.1):** A classe `UserStorage` agora é o modelo oficial de persistência do esquema.
2.  **Blindagem via Fachada (1.2.3):** O acesso aos arquivos de dados agora é exclusivo do "lado servidor", cumprindo a separação física exigida.
3.  **Sanitização de Caminhos (2.1.1):** A restrição de usernames alfanuméricos eliminou riscos de caracteres ilegais em nomes de arquivos, tornando o sistema de arquivos mais robusto.

### Resultados da Re-validação Final:
*   **Isolamento:** Usuários `alice` e `bob` mantiveram seus dados íntegros e inacessíveis um ao outro.
*   **Integridade:** O sistema bloqueou o acesso ao usuário `charlie` após simulação de adulteração de 1 bit no arquivo JSON.
*   **Conformidade:** O requisito **6.vi** está plenamente atendido e auditado.

---
**Próximo Passo Imediato:** Executar o teste de persistência multi-usuário para validar o isolamento e segurança.
