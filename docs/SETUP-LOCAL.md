# Ambiente local da Mikaela (Linux)

O build atual usa **JDK 25**, JavaFX 25 e o Gradle Wrapper 9.2.1.
As referências a Java 11 em `SETUP.md` são antigas; a versão 25 também é
utilizada no CI em `.github/workflows/build.yml`.

O JDK está em `.local-tools/jdk-25` e o cache do Gradle em
`.local-tools/gradle`. Essa pasta é ignorada pelo Git.

## Terminal

Na raiz do repositório:

```bash
bash dev.sh classes   # Compilar
bash dev.sh test      # Testes unitários
bash dev.sh integrationTest  # Testes de integração (requer sessão gráfica)
bash dev.sh run       # Abrir o aplicativo
```

O script seleciona o JDK local e executa o Gradle dentro da subpasta `Quelea`,
necessária para localizar os recursos do aplicativo. Ele pula a tarefa
`dependencyUpdates`, que consulta novas versões de bibliotecas e não é
necessária para desenvolver. A primeira execução precisa de internet para
baixar as dependências.

## VS Code

Abra `Quelea-local.code-workspace` pelo menu **Arquivo > Abrir Workspace do
Arquivo**. Ele abre a subpasta do projeto Gradle e configura o JDK 25.
Os caminhos do JDK são específicos desta máquina.

Em **Terminal > Executar Tarefa**, escolha `Quelea: executar`,
`Quelea: compilar` ou `Quelea: testar`.

## IntelliJ IDEA

Abra a pasta `Quelea` que contém `build.gradle`, importe como projeto Gradle
e selecione `.local-tools/jdk-25` da raiz do repositório como SDK e Gradle JVM.
Use o Gradle Wrapper do projeto e a tarefa `application > run`.

GTK 3 e GStreamer já estão presentes nesta máquina. A execução da interface
precisa ocorrer em uma sessão gráfica.

## Testes do WordDrawer

O arquivo [TesteWordDrawer.java](../Quelea/src/test/java/org/quelea/windows/main/TesteWordDrawer.java)
contém 33 testes com descrições em português.

- **JUnit Jupiter 6.0.0:** framework dos testes. Usa `@Test`, `@BeforeEach`,
  `@AfterEach`, `@DisplayName`, `@Tag` e asserções para verificar os resultados.
- **Mockito 5.20.0:** cria dublês do canvas, das configurações e da janela de
  projeção, permitindo testar a classe sem abrir a interface gráfica.
- **Gradle:** executa a suíte por meio do JUnit Platform, habilitado com
  `useJUnitPlatform()` no `Quelea/build.gradle`.

Os testes cobrem quebra de linhas, ajuste de fonte, medição de textos e cifras,
divisão de strings, delimitadores centrais, textos secundários, espaçamento,
escala, limpeza e delegação do desenho. Incluem textos e listas vazias, espaços
repetidos, acentos, sobrescrito e áreas de exibição nulas ou muito pequenas.
As verificações usam medidas de fonte do JavaFX; não validam visualmente a tela.

Para executar somente essa classe, na raiz do repositório:

```bash
bash dev.sh test --tests '*TesteWordDrawer'
```

Para executar todos os testes unitários:

```bash
bash dev.sh test
```

Na execução realizada após a criação da classe, **28 dos 33 testes novos
passaram** e **5 falharam**, revelando problemas no `WordDrawer`:

1. Palavra longa pode gerar uma linha vazia antes do conteúdo.
2. Palavra indivisível pode ultrapassar a largura mesmo havendo uma fonte menor viável.
3. A quebra de texto pode retornar fonte abaixo do mínimo 1.
4. Quebra de linha interna em texto secundário não é contada como dois elementos separados.
5. Linha vazia interna em texto secundário não é considerada corretamente na altura.

Esses cinco testes permanecem ativos, identificados com `@Tag("defeito-conhecido")`.
A tag não os ignora: enquanto os problemas persistirem, a execução termina com
falha. O código de produção do `WordDrawer` não foi alterado nessa etapa.
Os 15 testes anteriores passaram, totalizando **48 testes: 43 aprovados e 5 falhos**.

Após executar a suíte, o relatório fica em
`Quelea/build/reports/tests/test/index.html`.

## Testes de integração do WordDrawer

O arquivo [TesteIntegracaoWordDrawer.java](../Quelea/src/integrationTest/java/org/quelea/windows/main/TesteIntegracaoWordDrawer.java)
exercita o fluxo `SongDisplayable/BiblePassage → StageDrawer → WordDrawer → DisplayCanvas`
com objetos reais. Usa JUnit Jupiter e JavaFX, sem Mockito ou substituição dos
métodos de desenho. As asserções examinam o conteúdo e as propriedades dos nós
JavaFX produzidos pelo fluxo.

| Caso | Integração verificada |
|---|---|
| Canção e troca de seção | O parsing da letra produz seções; selecionar outra seção substitui o grupo anterior no canvas. |
| Redimensionamento | Uma nova dimensão reduz a fonte, preserva o texto e ajusta o fundo. |
| Cifras | A posição da cifra acompanha a letra seguinte; a configuração de exibição filtra as cifras do modelo. |
| Fonte uniforme | Seções de tamanhos diferentes recebem a mesma fonte quando a opção está ativa. |
| Passagem bíblica | Um versículo real passa pela quebra de linhas e cria nós de sobrescrito. |
| Espaçamento | Alterar a preferência modifica a distância vertical entre os nós de texto. |
| Remoção de texto | `eraseText()` remove os nós da letra, mantendo a imagem de fundo. |
| Estado de limpeza | O estado do canvas oculta o grupo de texto e permite exibi-lo novamente após redesenhar. |
| Tema | A cor configurada é aplicada à imagem real de fundo, removendo a anterior. |
| Escala | Uma janela real, não exibida, fornece a largura de referência; também são verificados janela ausente e largura zero. |

Na validação inicial, **10 testes passaram, sem falhas ou casos ignorados**.
Eles complementam os testes unitários; não corrigem nem substituem os cinco
casos unitários que expõem defeitos conhecidos.

```bash
# Somente integração
bash dev.sh integrationTest

# As duas suítes; --continue permite executar integração mesmo se a unitária falhar
bash dev.sh test integrationTest --continue
```

A tarefa `integrationTest` usa fontes separadas em `src/integrationTest/java`,
uma JVM própria, execução sequencial e preferências/logs em diretório temporário.
As operações gráficas são executadas na thread JavaFX, com espera limitada;
as janelas não são exibidas. O processo ainda precisa de uma sessão gráfica
disponível. Em CI Linux sem display, é necessário fornecer um servidor virtual,
como Xvfb; essa execução em CI não foi validada nesta etapa.

O relatório desta suíte fica em
`Quelea/build/reports/tests/integrationTest/index.html`, separado do relatório
unitário. A tarefa é explícita e não é adicionada automaticamente a `check`,
para que compilações sem sessão gráfica continuem executando a suíte unitária.

Esses testes verificam integração e a árvore de nós do JavaFX, não comparação
de screenshots, duração visual das transições ou o fluxo de `LyricDrawer`.
