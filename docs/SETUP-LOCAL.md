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
bash dev.sh test      # Testar
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

Para executar todos os testes:

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
