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
