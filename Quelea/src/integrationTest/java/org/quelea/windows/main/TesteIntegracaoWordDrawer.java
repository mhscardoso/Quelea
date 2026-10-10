package org.quelea.windows.main;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.FXFontMetrics;
import org.quelea.windows.lyrics.FormattedText;
import org.quelea.windows.stage.StageDrawer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integra modelos, configurações, StageDrawer/WordDrawer e a árvore real do JavaFX.
 * Não usa Mockito nem substitui métodos de desenho. Precisa de sessão gráfica
 * (ou Xvfb no CI), mas não mostra janelas. Executar: bash dev.sh integrationTest.
 * A tarefa roda em JVM separada; preferências e logs usam um diretório temporário.
 */
@Tag("integracao")
@DisplayName("Integração WordDrawer: modelos, palco e canvas JavaFX reais")
class TesteIntegracaoWordDrawer {
    @TempDir static Path userDirectory;
    private static final AtomicReference<Throwable> asyncError = new AtomicReference<>();
    private DisplayCanvas canvas;
    private StageDrawer drawer;

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        QueleaProperties.init(userDirectory.toString());
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> asyncError.compareAndSet(null, error));
            started.countDown();
        });
        assertTrue(started.await(15, TimeUnit.SECONDS), "JavaFX não iniciou em 15 segundos");
    }

    @BeforeEach
    void prepararComponentesReais() throws Exception {
        fx(() -> {
            QueleaProperties properties = QueleaProperties.get();
            properties.clear();
            properties.setMaxFontSize(80);
            properties.setUseUniformFontSize(false);
            properties.setAdditionalLineSpacing(10);
            properties.setShowChords(true);
            properties.setProperty("stage.text.alignment", "CENTER");
            properties.setProperty("clear.fade.duration", "0");
            QueleaApp.get().setProjectionWindow(null);
            canvas = new DisplayCanvas(true, null, DisplayCanvas.Priority.LOW);
            new Scene(canvas, 800, 600);
            canvas.resize(800, 600);
            drawer = new StageDrawer();
            drawer.setCanvas(canvas);
            drawer.setTheme(ThemeDTO.DEFAULT_THEME);
        });
    }

    @AfterEach
    void verificarErrosAssincronos() throws Exception {
        fx(() -> {
            if (QueleaApp.get().getProjectionWindow() != null) {
                QueleaApp.get().getProjectionWindow().close();
                QueleaApp.get().setProjectionWindow(null);
            }
        });
        Throwable error = asyncError.getAndSet(null);
        if (error != null) throw new AssertionError("Erro não tratado na thread JavaFX", error);
    }

    @AfterAll
    static void encerrarJavaFx() {
        Platform.exit();
    }

    @Test
    @DisplayName("Canção: parsing de seções chega aos nós de texto; trocar seção remove o conteúdo anterior")
    void cancaoAteCanvasETrocaDeSecao() throws Exception {
        fx(() -> {
            SongDisplayable song = song("Oração e esperança\nAlegria no coração\n\nNovo caminho");
            assertEquals(2, song.getSections().length);
            drawer.setText(song, 0);
            assertEquals(List.of("Oração e esperança", "Alegria no coração"), renderedLines());
            Group previous = textGroup();
            drawer.setText(song, 1);
            assertEquals(List.of("Novo caminho"), renderedLines());
            assertFalse(canvas.getChildren().contains(previous), "O grupo antigo não deve continuar no canvas");
        });
    }

    @Test
    @DisplayName("Redimensionamento: modelo real é redesenhado com fonte menor e fundo ajustado")
    void redimensionamentoRecalculaTextoEFundo() throws Exception {
        fx(() -> {
            SongDisplayable song = song("Uma oração de esperança para toda a comunidade");
            drawer.setText(song, 0);
            double originalSize = firstText().getFont().getSize();
            canvas.resize(320, 200);
            drawer.draw(song);
            layout();
            assertTrue(firstText().getFont().getSize() < originalSize);
            assertEquals(List.of("Uma oração de esperança para toda a comunidade"), renderedLines());
            assertTrue(formattedLines().get(0).getLayoutBounds().getWidth() <= 320 * 0.92 + 1);
            ImageView background = (ImageView) canvas.getCanvasBackground();
            assertEquals(320, background.getFitWidth());
            assertEquals(200, background.getFitHeight());
        });
    }

    @Test
    @DisplayName("Cifras: configuração real controla filtragem e posição sobre a letra seguinte")
    void cifrasDoModeloSaoAlinhadasEFiltradas() throws Exception {
        fx(() -> {
            SongDisplayable song = song("    Cmaj7\nWWWW oração");
            drawer.setText(song, 0);
            List<FormattedText> nodes = formattedLines();
            assertEquals(List.of("Cmaj7", "WWWW oração"), renderedLines());
            double expectedOffset = new FXFontMetrics(firstText().getFont()).computeStringWidth("WWWW");
            assertEquals(expectedOffset, nodes.get(0).getLayoutX() - nodes.get(1).getLayoutX(), 0.01);
            assertTrue(nodes.get(0).getLayoutY() < nodes.get(1).getLayoutY());
            QueleaProperties.get().setShowChords(false);
            drawer.setText(song, 0);
            assertEquals(List.of("WWWW oração"), renderedLines());
        });
    }

    @Test
    @DisplayName("Fonte uniforme: seções curtas e longas da mesma canção recebem o mesmo tamanho")
    void fonteUniformeEntreSecoes() throws Exception {
        fx(() -> {
            SongDisplayable song = song("Paz\n\nUma oração de esperança para toda a comunidade reunida");
            QueleaProperties.get().setUseUniformFontSize(true);
            drawer.setText(song, 0);
            double shortSize = firstText().getFont().getSize();
            drawer.setText(song, 1);
            assertEquals(shortSize, firstText().getFont().getSize(), 0.001);
            assertTrue(shortSize < 80);
            QueleaProperties.get().setUseUniformFontSize(false);
            drawer.setText(song, 0);
            assertTrue(firstText().getFont().getSize() > shortSize);
        });
    }

    @Test
    @DisplayName("Bíblia: versículo real passa por quebra de linhas e gera sobrescrito no JavaFX")
    void passagemBiblicaGeraTextoFormatado() throws Exception {
        fx(() -> {
            QueleaProperties.get().setShowVerseNumbers(true);
            canvas.resize(320, 300);
            String verseText = "A esperança renova o coração e acompanha todos os nossos passos pelo caminho da paz";
            BiblePassage passage = new BiblePassage("Referência de teste", new BibleVerse[]{
                    new BibleVerse(null, verseText, 1)
            }, ThemeDTO.DEFAULT_THEME, false);
            drawer.setText(passage, 0);
            assertTrue(formattedLines().size() > 1);
            String rendered = String.join(" ", renderedLines()).trim().replaceAll("\\s+", " ");
            assertEquals(verseText, rendered.replaceFirst("^1\\s*", ""));
            assertFalse(rendered.contains("<sup>"));
            Text verseNumber = formattedLines().stream().flatMap(line -> line.getChildren().stream())
                    .filter(Text.class::isInstance).map(Text.class::cast)
                    .filter(text -> text.getText().equals("1")).findFirst().orElseThrow();
            assertEquals(0.5, verseNumber.getScaleX());
            assertEquals(0.5, verseNumber.getScaleY());
        });
    }

    @Test
    @DisplayName("Espaçamento: preferências reais alteram a distância vertical dos textos gerados")
    void espacamentoChegaAosNosJavaFx() throws Exception {
        fx(() -> {
            SongDisplayable song = song("Oração\nEsperança");
            QueleaProperties.get().setAdditionalLineSpacing(0);
            drawer.setText(song, 0);
            double initialGap = formattedLines().get(1).getLayoutY() - formattedLines().get(0).getLayoutY();
            QueleaProperties.get().setAdditionalLineSpacing(20);
            drawer.draw(song);
            double newGap = formattedLines().get(1).getLayoutY() - formattedLines().get(0).getLayoutY();
            assertEquals(12, newGap - initialGap, 1, "20 pixels escalados pela altura 600/1000");
        });
    }

    @Test
    @DisplayName("Limpeza: eraseText retira os nós da letra e preserva o fundo real")
    void limpezaRemoveTextoEPreservaFundo() throws Exception {
        fx(() -> {
            drawer.setText(song("Uma oração"), 0);
            assertFalse(formattedLines().isEmpty());
            Node background = canvas.getCanvasBackground();
            drawer.eraseText();
            assertTrue(formattedLines().isEmpty());
            assertEquals(0, drawer.getText().length);
            assertSame(background, canvas.getCanvasBackground());
            assertTrue(canvas.getChildren().contains(background));
        });
    }

    @Test
    @DisplayName("Canvas limpo: estado real oculta o grupo e permite mostrar a letra novamente")
    void estadoDoCanvasControlaVisibilidade() throws Exception {
        fx(() -> {
            SongDisplayable song = song("Uma oração");
            drawer.setText(song, 0);
            canvas.setCleared(true);
            drawer.draw(song);
            drawer.draw(song);
            assertEquals(0, textGroup().getOpacity());
            canvas.setCleared(false);
            drawer.draw(song);
            drawer.draw(song);
            assertEquals(1, textGroup().getOpacity());
            assertEquals(List.of("Uma oração"), renderedLines());
        });
    }

    @Test
    @DisplayName("Tema: troca do fundo usa preferências reais e descarta a imagem anterior")
    void temaAtualizaFundoDoCanvas() throws Exception {
        fx(() -> {
            Node previous = canvas.getCanvasBackground();
            QueleaProperties.get().setStageBackgroundColor(Color.DARKBLUE);
            drawer.setTheme(ThemeDTO.DEFAULT_THEME);
            drawer.setText(song("Paz"), 0);
            ImageView background = (ImageView) canvas.getCanvasBackground();
            assertNotSame(previous, background);
            assertFalse(canvas.getChildren().contains(previous));
            assertTrue(canvas.getChildren().contains(background));
            assertEquals(Color.DARKBLUE, background.getImage().getPixelReader().getColor(0, 0));
            assertEquals(800, background.getFitWidth());
            assertEquals(600, background.getFitHeight());
        });
    }

    @Test
    @DisplayName("Escala: janela de projeção real fornece a referência para o canvas, sem ser exibida")
    void escalaComJanelaReal() throws Exception {
        fx(() -> {
            WordDrawer wordDrawer = drawer;
            assertEquals(1, wordDrawer.canvasScalingFactor());
            DisplayStage projection = new DisplayStage(new BoundingBox(0, 0, 1600, 900), false);
            QueleaApp.get().setProjectionWindow(projection);
            assertFalse(projection.isShowing());
            assertEquals(0.5, wordDrawer.canvasScalingFactor(), 0.001);
            canvas.resize(400, 300);
            assertEquals(0.25, wordDrawer.canvasScalingFactor(), 0.001);
            projection.setWidth(0);
            assertEquals(1, wordDrawer.canvasScalingFactor());
        });
    }

    private SongDisplayable song(String lyrics) {
        SongDisplayable song = new SongDisplayable("Canção de teste", "Autoria de teste");
        song.setNoDBUpdate();
        song.setLyrics(lyrics);
        return song;
    }

    private Group textGroup() {
        return canvas.getChildren().stream().filter(Group.class::isInstance)
                .map(Group.class::cast).findFirst().orElseThrow();
    }

    private List<FormattedText> formattedLines() {
        return textGroup().getChildren().stream().filter(FormattedText.class::isInstance)
                .map(FormattedText.class::cast).toList();
    }

    private List<String> renderedLines() {
        return formattedLines().stream().map(line -> line.getChildren().stream()
                .filter(Text.class::isInstance).map(Text.class::cast).map(Text::getText)
                .collect(Collectors.joining())).toList();
    }

    private Text firstText() {
        return (Text) formattedLines().get(0).getChildren().get(0);
    }

    private void layout() {
        canvas.applyCss();
        canvas.layout();
    }

    private static void fx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try {
            task.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException error) {
            if (error.getCause() instanceof Error cause) throw cause;
            if (error.getCause() instanceof Exception cause) throw cause;
            throw new RuntimeException(error.getCause());
        }
    }
}
