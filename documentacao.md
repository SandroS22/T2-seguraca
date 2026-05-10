> **Aviso:** Geramos esta documentação com auxílio de IA. Além disso, utilizamos IA para correção de bugs e geração da interface no CLI. 

# Documentação do Projeto MiniBlockchain

## Comandos de Compilação e Execução

Para compilar e executar o projeto, você deve estar na raiz do diretório onde a pasta `MiniBlockchain` está localizada.

### 1. Compilação
Execute o seguinte comando para compilar todos os arquivos fonte e incluí-las no classpath:

```bash
javac -cp ".;MiniBlockchain/lib/*" MiniBlockchain/src/*.java -d MiniBlockchain/src/
```

### 2. Execução
Execute a classe principal (`Main`) utilizando o classpath que inclui as bibliotecas Bouncy Castle FIPS e commons-codec:

```bash
java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" Main
```

---

## Documentação Técnica (Requisitos 5.2)

Abaixo estão as explicações sobre as escolhas técnicas e implementações de segurança do projeto, bem como o fluxo de métodos invocados.

### 1. Uso de TOTP (Time-based One-Time Password)
O sistema utiliza o algoritmo TOTP para autenticação de dois fatores (2FA). No cadastro, é gerado um segredo único de 256 bits para o usuário. 
- **Cálculo:** O código de 6 dígitos é gerado a partir do HMAC-SHA1 do segredo com o timestamp atual (janelas de 30 segundos).
- **Validação:** Durante o login (Passo 2), o servidor recalcula o código esperado para o instante atual e valida a entrada do usuário.
- **Fluxo de Métodos:**
  - **Geração (Cadastro):** `Main.handleRegister()` -> `MiniBlockchainServer.register()` -> `AuthService.register()` -> `SecurityUtils.generateTotpSecret()`.
  - **Verificação (Login):** `Main.handleLogin()` -> `MiniBlockchainServer.loginStep2()` -> `TotpService.verifyCode()` -> `SecurityUtils.calculateHMACSHA1()`.

### 2. Derivação de Chave Simétrica (KDF)
Para cumprir os requisitos de segurança e evitar o armazenamento de senhas em texto claro, utilizamos o **PBKDF2 (Password-Based Key Derivation Function 2)**.
- **Algoritmo:** `PBKDF2WithHmacSHA512` através do provedor Bouncy Castle FIPS.
- **Parâmetros:** 10.000 iterações e um salt aleatório de 16 bytes por usuário.
- **Função:** A senha do usuário nunca é armazenada; ela é usada apenas para derivar a chave mestra que abre o envelope cifrado do usuário e cifra seus blocos.
- **Fluxo de Métodos:**
  - **Cadastro:** `AuthService.register()` -> `SecurityUtils.deriveKey()` (utilizando `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512", PROVIDER)`).
  - **Autenticação:** `AuthService.authenticateStep1()` -> `SecurityUtils.deriveKey()`.

### 3. Criptografia por Bloco e Encadeamento da Blockchain
A integridade e confidencialidade da blockchain são garantidas por criptografia autenticada e encadeamento criptográfico.
- **AES-GCM (Criptografia Autenticada):** Cada bloco possui dados cifrados com AES em modo GCM. Se o conteúdo for alterado, a decifragem falhará devido à violação da tag de autenticação.
- **Encadeamento de Hashes:** Cada bloco armazena o hash SHA-256 do bloco anterior (`hashPrev`).
- **Selagem:** O sistema calcula o `hash` do bloco atual incluindo o `hashPrev`, o índice, o timestamp e o `dataEnc`.
- **Multiusuário:** O conteúdo (`dataEnc`) só pode ser decifrado pelo proprietário, usando a chave de sessão derivada via PBKDF2.
- **Fluxo de Métodos:**
  - **Criptografia (Adição de Bloco):** `Main.handleAddBlock()` -> `MiniBlockchainServer.addBlock()` -> `BlockchainService.addBlock()` -> `BlockchainService.encryptBlockPayload()` -> `SecurityUtils.encryptAESGCM()`.
  - **Encadeamento (Hashing do Bloco):** `BlockchainService.addBlock()` -> `Block.seal()` -> `BlockchainUtils.calculateHash()` -> `SecurityUtils.calculateSHA256()`.
  - **Decifragem (Listagem de Blocos):** `Main.handleListBlockchain()` -> `MiniBlockchainServer.getBlockchain()` -> `BlockchainService.getBlockchainListing()` -> `BlockchainService.decryptBlockPayload()` -> `SecurityUtils.decryptAESGCM()`.
