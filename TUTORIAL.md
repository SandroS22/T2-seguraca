# Guia de Testes e Utilização - MiniBlockchain

Este guia fornece um passo a passo detalhado para que você possa testar todas as funcionalidades de segurança, autenticação e blockchain implementadas no sistema.

---

## 🏗️ 1. Preparação Inicial
Certifique-se de que o projeto foi compilado conforme o `README.md`. Inicie a aplicação com o comando atualizado (adicione `--debug` para ver o que acontece "por baixo do capô"):

```bash
java -cp "MiniBlockchain/src;MiniBlockchain/lib/*" Main --debug
```

### 🔍 O que o Modo Debug mostra?
Ao usar o código TOTP do seu celular, o terminal exibirá:
*   O **Segredo Hexadecimal** que o servidor está usando.
*   O **Passo de Tempo** atual (janela de 30s).
*   Os **Códigos Esperados** para a janela atual e as janelas vizinhas (tolerância).

---

## 👤 2. Fluxo de Identidade (Cadastro e Login MFA)

### Passo 2.1: Cadastro de Usuário
1.  No menu principal, escolha a opção **1 (Cadastrar Novo Usuário)**.
2.  Insira um nome (ex: `alice123`) e uma senha forte (mínimo 8 caracteres).
3.  **IMPORTANTE:** O sistema exibirá um **Segredo TOTP** (formato Base32 compatível com Google Authenticator). Copie este código.
4.  Abra um aplicativo de autenticação (como Google Authenticator ou 2FAS) e adicione uma nova conta manualmente usando este segredo.

### Passo 2.2: Login em Dois Fatores
1.  No menu principal, escolha a opção **2 (Realizar Login)**.
2.  Insira o username e a senha cadastrados.
3.  Se a senha estiver correta, o sistema solicitará o código de 6 dígitos do seu aplicativo.
4.  Insira o código atual do app. Se validado, você entrará no **Menu Blockchain**.

---

## ⛓️ 3. Fluxo de Blockchain (Operações e Privacidade)

### Passo 3.1: Adicionar Dados
1.  No menu autenticado, escolha a opção **1 (Adicionar Novo Bloco)**.
2.  Escreva qualquer mensagem (ex: "Minha primeira transação").
3.  O sistema confirmará que o bloco foi selado e exibirá o Hash SHA-256 gerado.

### Passo 3.2: Privacidade Multiusuário (O Teste Decisivo)
1.  Adicione mais um bloco com a conta atual.
2.  Realize o **Logout (Opção 4)**.
3.  Crie um **novo usuário** (ex: `bob999`) e faça login com ele (repetindo o processo de TOTP).
4.  Escolha a opção **2 (Listar Blockchain)**.
5.  **Observe:** Você verá os blocos da `alice123`, mas o conteúdo deles aparecerá como `[CONTEÚDO CIFRADO - ACESSO NEGADO]`.
6.  Adicione um bloco como `bob999`. Agora, na listagem, você verá o seu conteúdo em texto claro e o da Alice protegido.

---

## 🛡️ 4. Fluxo de Auditoria e Integridade

### Passo 4.1: Auditoria Estrutural
1.  No menu autenticado, escolha a opção **3 (Realizar Auditoria de Integridade)**.
2.  O sistema percorrerá toda a cadeia, verificando se os hashes e vínculos estão corretos.
3.  Você receberá um relatório de "Sistema íntegro e consistente".

### Passo 4.2: Teste de Resistência (Simulação de Ataque)
*Este teste demonstra a detecção de fraude.*
1.  Sem fechar o programa, vá até a pasta `MiniBlockchain/data/blockchain` no seu computador.
2.  Abra um dos arquivos `block_XXXXX.json` com um editor de texto (ex: Bloco #0).
3.  Altere um único caractere dentro do campo `"dataEnc"` e salve o arquivo.
4.  Volte ao terminal e peça para **Listar Blockchain**.
5.  **Observe:** O sistema detectará a falha e exibirá `[ERRO DE INTEGRIDADE: Conteudo corrompido]` para aquele bloco específico.
6.  Execute a **Auditoria (Opção 3)**. O sistema reportará uma falha estrutural de selo violado.

---

## 📊 5. Monitoramento (Logs)
Após realizar os testes, você pode abrir o arquivo `MiniBlockchain/data/system.log` para verificar a trilha de auditoria. Note que:
- Todos os seus sucessos e falhas foram registrados.
- Nenhuma senha ou segredo aparece no log (Conformidade 6.v).

---
**Parabéns!** Você validou todas as camadas de segurança do MiniBlockchain.
