# Plano de Trabalho: Atividade 1.2.3 - Especificação do Protocolo de Comunicação

## Objetivo
Definir a arquitetura de interação entre a Interface de Usuário (Cliente) e o Núcleo de Segurança/Blockchain (Servidor). O foco é cumprir o requisito **6.i** do PDF: "Você deve agir como se o cliente e o servidor estivessem localizados em máquinas diferentes", impedindo o uso de variáveis globais ou estado compartilhado inseguro para chaves e IVs.

## Premissas de Design (Requisito 6.i)
1.  **Isolamento:** O "Cliente" (CLI) não deve ter acesso direto ao sistema de arquivos ou às chaves brutas.
2.  **Abstração:** Todas as operações sensíveis (Login, Registro, Adição de Bloco, Leitura) devem ser mediadas por uma "API Interna" (Serviço).
3.  **Estado de Sessão:** Chaves de sessão e dados decifrados residem apenas no contexto do "Servidor" (em memória protegida) durante o tempo necessário para a operação.

## Passos de Execução

### 1. Definição da Interface do "Servidor" (CONCLUÍDO)
Para garantir o isolamento, o sistema utilizará uma classe Facade (`MiniBlockchainServer`) que atuará como o ponto único de entrada para a Interface de Usuário.

*   **Métodos da API:**
    1.  `ServerResponse register(String user, String pass)`: Realiza o cadastro e retorna o segredo TOTP.
    2.  `ServerResponse loginStep1(String user, String pass)`: Valida a senha e prepara o ambiente para o 2FA.
    3.  `ServerResponse loginStep2(String code)`: Valida o TOTP e estabelece a sessão definitiva.
    4.  `ServerResponse addBlock(String content)`: Cifra os dados e anexa à blockchain (exige sessão ativa).
    5.  `ServerResponse getBlockchain()`: Retorna a lista de blocos (os dados serão decifrados apenas para o dono).
    6.  `ServerResponse logout()`: Encerra a sessão e limpa as chaves da memória.

*   **Estrutura de Resposta (`ServerResponse`):**
    *   `boolean success`: Indica se a operação foi bem-sucedida.
    *   `String message`: Mensagem descritiva ou erro.
    *   `Object data`: Dados retornados (ex: segredo TOTP, lista de blocos).

### 2. Especificação do Fluxo de Mensagens (CONCLUÍDO)
O tráfego de dados entre as camadas seguirá regras estritas para simular um ambiente distribuído e seguro:

*   **Fluxo de Solicitação (Cliente -> Servidor):**
    *   O Cliente chama métodos da classe `MiniBlockchainServer` passando apenas tipos primitivos ou Strings (ex: `username`, `password`, `code`).
    *   **Proibição:** O Cliente nunca deve instanciar classes de segurança (`SecurityUtils`) ou de persistência (`StorageManager`) diretamente.

*   **Fluxo de Processamento (Servidor):**
    *   O Servidor utiliza o `SessionContext` interno (não acessível ao cliente) para realizar operações de cifragem/decifragem.
    *   Todas as chaves e IVs são gerados, usados e mantidos dentro do escopo do Servidor.

*   **Fluxo de Resposta (Servidor -> Cliente):**
    *   O Servidor encapsula o resultado em um `ServerResponse`.
    *   **Isolamento de Dados:** Se a operação envolver blocos da blockchain, o Servidor os decifra (se o usuário for o dono) e retorna apenas o conteúdo em texto claro dentro da resposta, protegendo a chave de sessão.

### 3. Design do Controle de Acesso de Camada (CONCLUÍDO)
*   **Decisão:** A classe `SessionContext` foi encapsulada para ser acessível apenas pelo pacote/camada de serviço (`AuthService` e `MiniBlockchainServer`).
*   **Isolamento:** O Cliente (CLI) não mantém referências a objetos `User` decifrados. Ele apenas recebe uma confirmação de "Login OK" e utiliza o estado interno do Servidor para as chamadas subsequentes.

### 4. Validação da Arquitetura (CONCLUÍDO)
*   **Verificação:** O fluxo de "Envelope Cifrado" (2.1.3) aliado à "Fachada do Servidor" (1.2.3) garante o cumprimento do requisito 6.i.
*   **Evidência:** Mesmo que o código da CLI seja comprometido, ele não possui variáveis globais contendo as chaves mestras, pois estas residem apenas no contexto volátil do `SessionContext` dentro da camada lógica.

---
## Resultados Obtidos vs. Planejado

| Passo | Planejado | Obtido | Observações |
| :--- | :--- | :--- | :--- |
| **1. API** | Lista de métodos | Classe `MiniBlockchainServer` | **Igual:** Criada a estrutura Facade para isolar o núcleo conforme planejado. |
| **2. Fluxo** | DTOs e Mensagens | `ServerResponse` + Regras de Fluxo | **Melhoria:** Implementada uma classe de resposta padrão para centralizar o tráfego de dados seguros. |
| **3. Acesso** | Restrição de camada | Encapsulamento de Sessão | **Igual:** O Cliente (CLI) não possui acesso ao `SessionContext`. |
| **4. Validação** | Revisão de vazamentos | `MiniBlockchainServerTest` OK | **Diferença:** O teste inicial falhou devido às novas regras de validação de caracteres (alfanumérico), exigindo ajuste no nome do usuário de teste. |

### Detalhamento das Mudanças e Justificativas

| Mudança Realizada | Motivação | Impacto no Requisito 6.i |
| :--- | :--- | :--- |
| **Criação da `ServerResponse`** | Evitar que o Cliente precise conhecer a estrutura interna de exceções ou objetos de persistência. | **Alto:** Garante que apenas dados "limpos" cheguem à interface. |
| **Uso do Padrão Facade** | Atender à exigência de "agir como se fossem máquinas diferentes". | **Crítico:** Sem a fachada, o cliente poderia manipular chaves, violando o isolamento. |
| **Ocultação do `SessionContext`** | Impedir o uso de variáveis globais pelo cliente. | **Alto:** Atende à proibição do item 6.i sobre chaves armazenadas pelo cliente. |
| **Restrição Alfanumérica** | Durante os testes, percebeu-se a necessidade de sanitizar entradas para garantir a integridade do sistema de arquivos. | **Baixo:** Aumenta a robustez contra injeção de nomes de arquivos inválidos. |

**Conclusão Final:** A atividade 1.2.3 foi concluída com êxito. A arquitetura implementada através da fachada `MiniBlockchainServer` provou ser eficaz no isolamento das responsabilidades e na proteção de parâmetros criptográficos sensíveis, cumprindo integralmente o requisito **6.i** do projeto.

---
**Próximo Passo Imediato:** Definir formalmente a interface de métodos do "Servidor".
