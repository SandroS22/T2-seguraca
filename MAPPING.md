# Mapeamento de Requisitos e Implementação - MiniBlockchain

Este documento detalha como cada requisito solicitado no arquivo `INE5680-20261-MiniBlockchain.pdf` e nas instruções do usuário foi atendido pelo sistema desenvolvido.

---

## 1. Segurança e Criptografia (Requisito 2.4 e Atenção Especial 6)

### 1.1 Confidencialidade (AES-GCM)
*   **Solicitação:** Dados de cada usuário cifrados individualmente com AES-GCM usando chaves de 256 bits (session keys).
*   **Implementação:** 
    *   **Entrada:** `String` de conteúdo bruto do usuário.
    *   **Fluxo:** `Main.handleAddBlock()` -> `MiniBlockchainServer.addBlock()` -> `BlockchainService.encryptBlockPayload()` -> `SecurityUtils.encryptAESGCM()`.
    *   **Saída:** `dataEnc` (Hex) e `iv` (Hex) armazenados no objeto `Block`.
    *   **Conformidade:** O sistema utiliza `AES/GCM/NoPadding` do provedor BCFIPS com Tag de 128 bits e chaves voláteis de 256 bits recuperadas do `SessionContext`.

### 1.2 Geração Única de IV
*   **Solicitação:** IV único por bloco (Requisito 2.4) e não permitido ter IVs fixos (Item 6.v).
*   **Implementação:**
    *   **Fluxo:** `BlockchainService.encryptBlockPayload()` invoca `SecurityUtils.generateGcmIV()`.
    *   **Mecânica:** Utiliza `java.security.SecureRandom` para gerar 12 bytes aleatórios a cada nova cifragem.
    *   **Validação:** Testado pelo `IVUniquenessTest.java` com 10.000 amostras e zero colisões.

### 1.3 Derivação de Chave (PBKDF2)
*   **Solicitação:** Senha derivada com PBKDF2 ou Scrypt (Requisito 2.1).
*   **Implementação:**
    *   **Entrada:** `password` (String) e `salt` (16 bytes Hex).
    *   **Fluxo:** `AuthService.register()` ou `authenticateStep1()` -> `SecurityUtils.deriveKey()`.
    *   **Parâmetros:** Algoritmo `PBKDF2WithHmacSHA512`, 10.000 iterações, 256 bits de saída.

---

## 2. Autenticação e Identidade (Requisito 2.1)

### 2.1 Autenticação Forte (MFA / TOTP)
*   **Solicitação:** Usuário deve fornecer Senha + TOTP para interagir.
*   **Implementação:**
    *   **Passo 1 (Senha):** `Main.handleLogin()` -> `MiniBlockchainServer.loginStep1()`. Valida a senha tentando decifrar o envelope cifrado do usuário.
    *   **Passo 2 (TOTP):** `Main.handleLogin()` -> `MiniBlockchainServer.loginStep2()`. Valida o código de 6 dígitos via `TotpService.validateCode(HMAC-SHA256, timeStep)`.
    *   **Saída:** Sessão estabelecida no `SessionContext` apenas após o sucesso de AMBOS os passos.

### 2.2 Isolamento de Segredos (Requisito 6.i)
*   **Solicitação:** Agir como se cliente e servidor estivessem em máquinas diferentes. Proibido chaves globais no cliente.
*   **Implementação:**
    *   **Arquitetura:** Padrão **Facade** através da classe `MiniBlockchainServer`.
    *   **Fluxo:** O `Main.java` (Cliente) nunca recebe objetos `SecretKey`. Ele envia strings e recebe `ServerResponse` com dados processados ou mensagens.
    *   **Residência:** Chaves residem apenas nas variáveis privadas e estáticas da camada de "Servidor" (`SessionContext`).

---

## 3. Integridade da Blockchain (Requisito 2.2 e 2.3)

### 3.1 Encadeamento de Blocos (Chaining)
*   **Solicitação:** Cada bloco depende do hash do anterior, formando uma cadeia imutável.
*   **Implementação:**
    *   **Fluxo:** `BlockchainService.linkNewBlock()` -> Captura `StorageManager.getLastBlock().hash` e insere em `newBlock.hashPrev`.
    *   **Selo:** `BlockchainService.calculateBlockHash()` gera o SHA-256 do bloco completo (incluindo o vínculo com o pai).

### 3.2 Auditoria Estrutural e Criptográfica
*   **Solicitação:** Verificar integridade do `hash_prev` e validade do AES-GCM.
*   **Implementação:**
    *   **Método:** `BlockchainService.verifyBlockFullIntegrity()`.
    *   **Camada 1:** Recalcula SHA-256 e compara com o campo `hash`. Detecta deleções ou trocas de ordem.
    *   **Camada 2:** Tenta a decifragem GCM. A falha na Tag (MAC) indica que o conteúdo cifrado foi alterado, mesmo que o hash tenha sido recalculado pelo atacante.

---

## 4. Persistência e Manuseio de Dados (Requisito 6.vi)

### 4.1 Armazenamento Cifrado de Parâmetros
*   **Solicitação:** Segredos (exceto salt) devem ser guardados em arquivo cifrado.
*   **Implementação:**
    *   **Estratégia:** **Encrypted Envelope**. 
    *   **Fluxo:** O objeto `User` (contendo Hash da Senha e Segredo TOTP) é cifrado com a chave derivada da senha e salvo como um `blob` hexadecimal no arquivo `user_[name].json`.
    *   **Saída:** Inspeção visual prova que o arquivo físico contém apenas `salt` (público), `iv` e `blob` (cifrado).

---

## 5. Funcionalidades de Interface (Item 4)

### 5.1 Menu e Navegação
*   **Solicitação:** Scripts/Funções via menu simples em modo texto.
*   **Implementação:**
    *   **Classe `Main.java`:** Loop infinito com troca dinâmica de menus (Visitante vs Logado).
    *   **Listagem:** `handleListBlockchain()` exibe tabela ASCII com decifragem seletiva (o usuário vê seus dados decifrados e o de terceiros como `[ACESSO NEGADO]`).
