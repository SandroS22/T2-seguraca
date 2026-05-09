# Work Breakdown Structure (WBS) - MiniBlockchain

**Nível 0: MiniBlockchain**
> Implementação de uma mini-blockchain multiusuário com foco em confidencialidade (AES-GCM), autenticação forte (TOTP) e integridade da cadeia de blocos.

---

## 1.0 Gestão de Projeto e Arquitetura
*Fase focada na infraestrutura básica, organização do ambiente de desenvolvimento e design das estruturas de dados que suportarão o sistema.*

### 1.1 Configuração do Ambiente
*Estabelecimento da base técnica e importação de recursos necessários.*
* **1.1.1 Integração das Bibliotecas Bouncy Castle FIPS:** Inclusão dos JARs de criptografia FIPS no projeto para conformidade com os requisitos.
* **1.1.2 Importação e Adaptação dos Códigos de Exemplo:** Seleção e ajuste das classes utilitárias de AES-GCM, PBKDF2 e HMAC fornecidas nos exemplos.
* **1.1.3 Definição da Estrutura de Persistência de Dados:** Planejamento de como os usuários e blocos serão salvos em disco (ex: JSON ou arquivos binários).

### 1.2 Design de Estruturas de Dados
*Modelagem dos objetos principais do sistema.*
* **1.2.1 Definição do Esquema do Usuário (Username, Salt, Hash):** Estruturação dos dados necessários para identificar e autenticar um usuário com segurança.
* **1.2.2 Definição do Esquema do Bloco (Dados, IV, HashPrev, Owner):** Especificação dos campos que compõem cada nó da blockchain, incluindo metadados de segurança.
* **1.2.3 Especificação do Protocolo de Comunicação:** Definição da interface entre a lógica de negócio ("servidor") e a interface de usuário ("cliente").

---

## 2.0 Módulo de Autenticação e Identidade
*Fase responsável por garantir que apenas usuários autorizados possam interagir com a blockchain e seus dados.*

### 2.1 Cadastro de Usuários
*Processo de registro seguro de novos participantes.*
* **2.1.1 Implementação da Derivação de Senha com PBKDF2/Scrypt:** Transformação de senhas em texto puro em hashes robustos para armazenamento seguro.
* **2.1.2 Geração e Armazenamento do Segredo TOTP:** Criação da chave compartilhada para autenticação de dois fatores (2FA).
* **2.1.3 Persistência de Dados de Usuário de Forma Segura:** Gravação dos dados cadastrais garantindo que segredos (exceto o salt) nunca fiquem expostos.

### 2.2 Sistema de Login
*Verificação da identidade do usuário em dois estágios.*
* **2.2.1 Verificação de Credenciais de Primeiro Fator (Senha):** Comparação do hash da senha fornecida com o hash armazenado.
* **2.2.2 Validação de Segundo Fator (TOTP via HMAC):** Cálculo e verificação do código de tempo variável para confirmar a posse do dispositivo 2FA.
* **2.2.3 Geração de Chaves de Sessão Temporárias:** Criação de chaves voláteis em memória para operações de cifragem após o login bem-sucedido.

---

## 3.0 Camada de Criptografia e Integridade
*Fase técnica dedicada à implementação dos algoritmos criptográficos que garantem a segurança dos dados e da cadeia.*

### 3.1 Criptografia de Dados (Confidencialidade)
*Proteção das informações sensíveis contidas nos blocos.*
* **3.1.1 Implementação de AES-GCM para Cifragem:** Aplicação do algoritmo AES no modo GCM para proteger os dados arbitrários de cada bloco.
* **3.1.2 Implementação de AES-GCM para Decifragem:** Processo de recuperação dos dados originais para usuários autorizados.
* **3.1.3 Sistema de Geração Única de IV por Bloco:** Garantia de que cada operação de cifragem utilize um Vetor de Inicialização único, conforme exigido.

### 3.2 Mecanismos de Encadeamento (Integridade)
*Vinculação criptográfica entre os blocos para impedir modificações não detectadas.*
* **3.2.1 Implementação de Funções de Hash para Blocos:** Criação da impressão digital de cada bloco contendo todos os seus campos.
* **3.2.2 Algoritmo de Vinculação (Hash do Bloco Anterior):** Inclusão do hash do bloco N-1 no cabeçalho do bloco N para formar a corrente.
* **3.2.3 Verificação de Assinatura e Integridade GCM:** Utilização da tag de autenticação do GCM para detectar qualquer alteração no conteúdo cifrado.

---

## 4.0 Gerenciamento da Blockchain
*Fase que coordena a adição de novos dados e a leitura da estrutura encadeada.*

### 4.1 Registro de Blocos (Escrita)
*Fluxo completo de criação de um novo elo na cadeia.*
* **4.1.1 Coleta de Dados e Timestamping:** Captura das informações do usuário e marcação temporal da operação.
* **4.1.2 Cifragem de Dados e Cálculo de Hash:** Execução das operações criptográficas preparatórias para o registro.
* **4.1.3 Adição do Bloco à Cadeia Persistente:** Gravação do novo bloco no arquivo ou base de dados da blockchain.

### 4.2 Navegação e Auditoria (Leitura)
*Visualização e verificação de saúde da blockchain.*
* **4.2.1 Listagem Completa de Blocos da Blockchain:** Recuperação sequencial de todos os registros armazenados.
* **4.2.2 Filtro de Acesso e Decifragem Seletiva:** Lógica que permite ao usuário visualizar o conteúdo apenas de blocos de sua propriedade.
* **4.2.3 Validação de Consistência de Toda a Cadeia:** Algoritmo que percorre a blockchain verificando se todos os hashes de conexão permanecem válidos.

---

## 5.0 Interface, Integração e Qualidade
*Fase final de interface com o usuário e validação rigorosa dos requisitos de segurança.*

### 5.1 Interface de Usuário (CLI)
*Desenvolvimento do console de interação.*
* **5.1.1 Desenvolvimento do Menu de Operações:** Criação do fluxo textual para Cadastro, Login, Adicionar Bloco e Listar Blockchain.
* **5.1.2 Visualização Formatada da Blockchain:** Exibição clara dos blocos, destacando hashes e metadados.
* **5.1.3 Sistema de Logs de Operações e Erros:** Registro de eventos (sem expor segredos) para auditoria e depuração.

### 5.2 Testes e Validação
*Garantia de que o sistema é seguro e funcional.*
* **5.2.1 Testes de Fluxo de Autenticação:** Validação de cenários de sucesso e tentativas de acesso com credenciais inválidas.
* **5.2.2 Testes de Integridade:** Simulação de ataques de modificação em arquivos para verificar se o sistema detecta a quebra da cadeia.
* **5.2.3 Validação da Não-Exposição de Chaves:** Auditoria do código para garantir que chaves e IVs não estão "hardcoded" ou em variáveis globais.
