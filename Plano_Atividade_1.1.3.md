# Plano de Trabalho: Atividade 1.1.3 - Definição da Estrutura de Persistência de Dados

## Objetivo
Definir e implementar a estrutura de armazenamento para usuários e blocos da blockchain. A persistência deve ser robusta o suficiente para suportar múltiplos usuários e garantir que dados sensíveis (exceto o salt) sejam protegidos, conforme os requisitos de segurança do PDF (item 6.vi).

## Requisitos de Dados
1.  **Usuários:** Username, Salt (público), Hash da Senha (derivado), Segredo TOTP (cifrado), Chave de Sessão (em memória, não persistida).
2.  **Blocos:** Dados cifrados (AES-GCM), IV único, Timestamp, Hash do bloco anterior, Owner (username).

## Passos de Execução

### 1. Definição do Formato e Esquema (CONCLUÍDO)
*   **Decisão:** Utilização de arquivos JSON individuais para cada usuário e uma cadeia de arquivos JSON para os blocos.
*   **Esquema do Usuário (`user_[username].json`):**
    ```json
    {
      "username": "string",
      "salt": "hex_string",
      "passwordHash": "hex_string",
      "totpSecretEnc": "hex_string",
      "totpIV": "hex_string"
    }
    ```
*   **Esquema do Bloco (`block_[index].json`):**
    ```json
    {
      "index": "int",
      "timestamp": "long",
      "dataEnc": "hex_string",
      "iv": "hex_string",
      "hashPrev": "hex_string",
      "owner": "string",
      "hash": "hex_string"
    }
    ```
*   **Implementação:** Criação de um `JsonUtils.java` manual para evitar dependências externas.

### 2. Criação das Classes de Modelo (CONCLUÍDO)
*   **Ação:** Implementação das classes `User.java` e `Block.java`.
*   **Destaque:** Ambas as classes possuem métodos `toMap()` e `fromMap()` para integração direta com o `JsonUtils`, facilitando a persistência sem bibliotecas externas.
*   **Segurança:** A classe `User` já contempla os campos para segredo TOTP cifrado e seu respectivo IV.

### 3. Implementação do Gerenciador de Arquivos (CONCLUÍDO)
*   **Ação:** Criação da classe `StorageManager.java`.
*   **Funcionalidades:**
    *   Gestão automática dos diretórios `data/users` e `data/blockchain`.
    *   Métodos `saveUser` e `loadUser` para persistência de perfis.
    *   Métodos `saveBlock` e `loadAllBlocks` para gestão da cadeia.
    *   Ordenação automática dos blocos por nome de arquivo (`block_00000.json`, etc).

### 4. Estratégia de Criptografia de Parâmetros (CONCLUÍDO)
*   **Estratégia "Encrypted Envelope":** Para cumprir o requisito 6.vi, os dados do usuário não serão salvos individualmente em texto claro.
*   **Lógica de Persistência:**
    1.  **Salt:** Salvo em texto claro (permitido).
    2.  **Envelope Cifrado:** Todos os outros campos (`username`, `passwordHash`, `totpSecret`) são agrupados em um JSON secundário.
    3.  **Cifragem:** Este JSON secundário é cifrado com AES-GCM usando uma chave derivada da senha do usuário.
    4.  **Armazenamento:** O arquivo final contém apenas o `salt`, o `iv` e o `blob` cifrado.
*   **Vantagem:** Sem a senha correta, é impossível ler qualquer parâmetro, e o GCM garante a integridade (se o arquivo for editado, a decifragem falha).

### 5. Validação (Critério de Aceite)
*   **Teste:** Criar um objeto `User`, salvá-lo, reiniciar a aplicação (simuladamente) e carregá-lo com sucesso, verificando a integridade dos campos.


---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. Formato** | JSON/Binário genérico | JSON Manual (`JsonUtils`) | **Mudança:** Optou-se por JSON manual para evitar bibliotecas externas, garantindo portabilidade. |
| **2. Modelos** | `User` e `Block` | `User`, `Block` e `UserStorage` | **Diferença:** Adicionada `UserStorage` para separar a lógica de negócio da lógica de armazenamento cifrado. |
| **3. Manager** | `StorageManager` | `StorageManager` funcional | **Igual:** Implementado conforme o plano, com suporte a múltiplos usuários e blocos. |
| **4. Segurança** | Cifragem de parâmetros | Estratégia "Encrypted Envelope" | **Evolução:** A estratégia foi refinada para cifrar TODO o perfil do usuário com a senha dele, excedendo o requisito mínimo. |
| **5. Validação** | Teste de carga/recarga | `PersistenceTest` com Sucesso | **Igual:** Validou-se que segredos são inacessíveis sem a senha e recuperáveis com ela. |

### Justificativa das Mudanças
- **Uso de `UserStorage`:** A necessidade de cumprir o requisito **6.vi** (parâmetros cifrados em arquivo) exigiu uma classe que representasse o "envelope" (Salt + IV + Blob Cifrado), enquanto a classe `User` lida com os dados em memória após a decifragem.
- **`JsonUtils` manual:** Devido à restrição de bibliotecas externas, um parser minimalista foi a solução mais segura e leve.
- **Estratégia de Envelope:** Em vez de cifrar apenas o segredo TOTP, cifrou-se o objeto `User` inteiro. Isso aumenta a segurança, pois nem mesmo o `username` interno ou o `passwordHash` ficam visíveis para quem não possui a senha, restando apenas o `salt` e o `iv` no arquivo JSON físico.
