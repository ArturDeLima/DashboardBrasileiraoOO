# Projeto: Dashboard de Análise  Exploratória de Dados do Campeonato Brasileiro
*Este é o repositório do trabalho prático da disciplina de Programação Orientada a Objetos.*

---

### 1. Identificação

* **Nome**: Artur Lima
* **Universidade**: UFS

---

### 2. Link para o Repositório

[https://github.com/ArturDeLima/DashboardBrasileiraoOO]

---

### 3. Descrição do Tema do Trabalho

O objetivo deste projeto foi desenvolver um dashboard de análise de dados do Campeonato Brasileiro de futebol. A aplicação permite aos usuários carregar dados de partidas de múltiplos anos, filtrá-los por período, região e turno, e visualizá-los através de tabelas e gráficos interativos.

---

### 4. O que Consegui Desenvolver

Desenvolvi uma aplicação completa em duas linguagens de programação, Java e Python, com as seguintes funcionalidades principais:

* **Análise de Dados:** A aplicação é capaz de ler e processar dados de partidas a partir de arquivos CSV, calculando estatísticas como pontuação, vitórias, derrotas, e saldo de gols para cada time e outros.
* **Filtros Dinâmicos:** A interface do usuário permite filtrar os dados por um intervalo de anos e por região (Sudeste, Sul, Nordeste, Centro-Oeste). A análise é atualizada dinamicamente com base nas seleções do usuário.
* **Visualização Interativa:** A aplicação exibe os dados em uma tabela de classificação e em diversos tipos de gráficos, incluindo gráficos de linha (desempenho ao longo das rodadas), gráficos de barras (histórico) e gráficos de dispersão (relação entre gols e vitórias) e outros.
* **Implementação em Duas Linguagens:** O projeto foi desenvolvido em Java, usando a biblioteca JavaFX, e em Python, usando a biblioteca PyQt6 para a interface e Pandas para a manipulação dos dados, demonstrando o domínio dos conceitos de programação em ambas as plataformas e tentando deixar o mais semelhante possível.
* **Com isso:** foi possível fazer certas análises, demonstra de forma mais clara e até confirmar julgamentos e visões pré existentes.
* **Pontos passíveis de desenvolver:** Gostaria de ter deixados os escudos dos times em cada ponto que representa o time mas deixei cerca de 40 horas nesse trabalho e tive que focar em outras partes e fazer uma parte preditiva.


---

### 5. Discução da POO na Segunda Linguagem Adotada (Python)

A implementação em Python foi guiada pelos mesmos princípios de Programação Orientada a Objetos (POO) da versão em Java, mas adaptada às características da linguagem.

* **Separação de Preocupações:** O projeto em Python é dividido em classes com responsabilidades bem definidas, como `Partida` e `Time` para o modelo de dados, `AnalisadorCampeonato` para a lógica de negócios e `DashboardApp` para a interface gráfica, usando o padrão "Controller" para conectar a lógica à UI.
* **Utilização de Padrões Pythonicos:** Em vez de usar `SimpleIntegerProperty` do JavaFX, a classe `Time` em Python utiliza `@property` para encapsular e controlar o acesso aos seus atributos. A classe `Partida` usa `**kwargs` em seu construtor, uma prática comum em Python para criar classes de dados que se adaptam a diferentes estruturas de arquivos CSV.
* **Gerenciamento de Dados:** O `DataFrame` do Pandas foi adotado como o modelo de dados principal, permitindo que a classe `AnalisadorCampeonato` realize operações complexas de análise e filtragem de forma muito mais concisa e eficiente do que seria possível com uma implementação manual em Python puro, mantendo os princípios de POO.
* **Herança e Especialização:** A classe 'DashboardApp' demonstra o uso de herança ao derivar de 'PyQt6.QtWidgets.QMainWindow'. Essa especialização permitiu que nossa classe herdasse todo o comportamento e a estrutura de uma janela de aplicativo complexa, como menus, barras de status e layouts, sem a necessidade de reescrever todo esse código-base.
A classe DashboardApp demonstra o uso de herança ao derivar de PyQt6.QtWidgets.QMainWindow. Essa especialização permitiu que nossa classe herdasse todo o comportamento e a estrutura de uma janela de aplicativo complexa, como menus, barras de 
* **Eventos e UI:** A interface em PyQt6 segue um padrão de "sinais e slots", que é a versão orientada a objetos de um modelo de programação reativa, análogo ao sistema de eventos do JavaFX.
* **Além disso:** Acredito que vale também destacar que cada uma das linguagens pode servir como ótima ferramenta para usos, py sendo menos verborágica mas menos "bem comportada" além de não ter alguns aspectos da OO com o encapsulamento, um dos motivo para eu ter mudado de c++ para py foi que gostaria de trazer uma visão entre linguagens mais diferentes na implementação de OO.


### 6. Para usar a aplicação

* **Organização e versionamento:** usei o javafx 21.0.8 e pyqt6, além de ter utilizado um venv e organizei os arquivos de forma semelhante ao padrão.
