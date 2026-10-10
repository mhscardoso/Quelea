package org.quelea.windows.main;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.services.utils.LyricLine;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.FXFontMetrics;
import org.quelea.utils.WrapTextResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Especificação dos cálculos e da delegação de WordDrawer, sem abrir o aplicativo.
 * As larguras usam a fonte instalada em tempo de execução, evitando depender de
 * medidas específicas de um sistema operacional. Não testa a renderização visual.
 *
 * Os testes com a tag "defeito-conhecido" expressam o resultado desejado e ficam
 * ativos: sua falha expõe uma limitação da implementação, não deve ser ocultada.
 * A região central de containsNotAtEnd segue o cálculo atual (remoção de 20%
 * de cada ponta, com truncamento), apesar do comentário mencionar 80% centrais.
 *
 * Executar na raiz: bash dev.sh test --tests '*TesteWordDrawer'
 */
@DisplayName("WordDrawer: quebra, dimensões, cifras e delegação do desenho")
class TesteWordDrawer {
    private static final Font FONT = Font.font("SansSerif", 40);
    private static final double EPS = 0.001;
    private DrawerTeste drawer;
    private DisplayCanvas canvas;
    private QueleaProperties properties;
    private QueleaApp app;
    private MockedStatic<QueleaProperties> propertiesStatic;
    private MockedStatic<QueleaApp> appStatic;

    @BeforeEach
    void preparar() {
        properties = mock(QueleaProperties.class);
        propertiesStatic = mockStatic(QueleaProperties.class);
        propertiesStatic.when(QueleaProperties::get).thenReturn(properties);
        app = mock(QueleaApp.class);
        appStatic = mockStatic(QueleaApp.class);
        appStatic.when(QueleaApp::get).thenReturn(app);
        canvas = mock(DisplayCanvas.class);
        when(canvas.getWidth()).thenReturn(800.0);
        when(canvas.getHeight()).thenReturn(1000.0);
        drawer = new DrawerTeste();
        drawer.setCanvas(canvas);
    }

    @AfterEach
    void liberarDubles() {
        if (appStatic != null) appStatic.close();
        if (propertiesStatic != null) propertiesStatic.close();
    }

    @Test
    @DisplayName("Quebra: preserva palavras, ordem e acentos ao distribuir em várias linhas")
    void quebraPreservaConteudo() {
        String original = "oração coração bênção alegria esperança";
        WrapTextResult result = drawer.normalWrapText(FONT, original, width(FONT, "oração coração"), 160);
        assertTrue(result.getNewText().size() > 1);
        assertEquals(original, content(result).trim().replaceAll("\\s+", " "));
    }

    @Test
    @DisplayName("Quebra: linhas de palavras que cabem respeitam a largura visual disponível")
    void quebraRespeitaLargura() {
        double available = width(FONT, "oração coração");
        WrapTextResult result = drawer.normalWrapText(FONT,
                "oração coração bênção alegria esperança", available, 160);
        Font adjusted = new Font(FONT.getName(), result.getFontSize());
        for (LyricLine line : result.getNewText()) {
            assertTrue(width(adjusted, line.getLine().stripTrailing()) <= available + EPS,
                    () -> "Linha excedeu a largura: " + line.getLine());
        }
    }

    @Test
    @Tag("defeito-conhecido")
    @DisplayName("Quebra: palavra longa não deve gerar uma linha vazia antes do conteúdo")
    void palavraLongaNaoCriaLinhaVazia() {
        WrapTextResult result = drawer.normalWrapText(FONT, "extraordinariamente", 5, 100);
        assertTrue(result.getNewText().stream().noneMatch(l -> l.getLine().isBlank()));
    }

    @Test
    @Tag("defeito-conhecido")
    @DisplayName("Quebra: palavra indivisível deve caber quando há uma fonte mínima viável")
    void palavraLongaCabeNaArea() {
        String word = "extraordinariamente";
        double available = width(new Font(FONT.getName(), 2), word);
        WrapTextResult result = drawer.normalWrapText(FONT, word, available, 100);
        Font adjusted = new Font(FONT.getName(), result.getFontSize());
        assertEquals(word, content(result).trim());
        assertTrue(width(adjusted, word) <= available + EPS);
    }

    @Test
    @DisplayName("Quebra: preserva espaços internos repetidos e caracteres acentuados")
    void espacosEAcentos() {
        String input = "fé   e  ação";
        WrapTextResult result = drawer.normalWrapText(FONT, input, 10000, 100);
        assertEquals(input, content(result).stripTrailing());
    }

    @Test
    @DisplayName("Quebra: mantém as tags sup no conteúdo e ignora as tags na medição")
    void sobrescrito() {
        WrapTextResult marked = drawer.normalWrapText(FONT, "<sup>1</sup> fé", 100, 100);
        WrapTextResult plain = drawer.normalWrapText(FONT, "1 fé", 100, 100);
        assertEquals("<sup>1</sup> fé", content(marked).trim());
        assertEquals(plain.getFontSize(), marked.getFontSize(), EPS);
        assertEquals(plain.getNewText().size(), marked.getNewText().size());
    }

    @Test
    @DisplayName("Quebra: texto vazio ou apenas espaços não introduz conteúdo visível")
    void quebraTextoVazio() {
        for (String input : List.of("", "   ")) {
            WrapTextResult result = drawer.normalWrapText(FONT, input, 100, 100);
            assertTrue(content(result).isBlank());
            assertTrue(Double.isFinite(result.getFontSize()));
        }
    }

    @Test
    @DisplayName("Fonte: reduz para caber na largura, preservando o limite máximo original")
    void fonteCabeNaLargura() {
        List<LyricLine> text = lines("Uma frase comprida para a projeção");
        double available = width(FONT, text.get(0).getLine()) / 2;
        double size = drawer.pickFontSize(FONT, text, available, 1000);
        assertTrue(size >= 1 && size < FONT.getSize());
        // longestLine trunca medidas em pixels: tolerância inferior a um pixel.
        assertTrue(width(new Font(FONT.getName(), size), text.get(0).getLine()) < available + 1);
    }

    @Test
    @DisplayName("Fonte: altura inclui todas as linhas e o espaçamento adicional")
    void fonteCabeNaAltura() {
        when(properties.getAdditionalLineSpacing()).thenReturn(4.0);
        List<LyricLine> text = lines("oração", "esperança", "alegria");
        double size = drawer.pickFontSize(FONT, text, 10000, 75);
        assertTrue(size >= 1 && size < FONT.getSize());
        assertTrue((new FXFontMetrics(new Font(FONT.getName(), size)).getLineHeight() + 4)
                * text.size() <= 75 + EPS);
    }

    @Test
    @DisplayName("Fonte: mantém o tamanho original quando largura e altura são suficientes")
    void fonteNaoAumenta() {
        assertEquals(FONT.getSize(), drawer.pickFontSize(FONT, lines("oração"), 10000, 10000));
    }

    @Test
    @DisplayName("Fonte: retorna o mínimo 1 em áreas nulas ou pequenas demais")
    void fonteMinimaEmAreaInsuficiente() {
        for (double area : new double[]{0, 0.01}) {
            assertEquals(1, drawer.pickFontSize(FONT, lines("oração"), area, 100));
            assertEquals(1, drawer.pickFontSize(FONT, lines("oração"), 100, area));
            assertEquals(1, drawer.pickSmallFontSize(FONT, new String[]{"oração"}, area, area));
        }
    }

    @Test
    @DisplayName("Ajuste: encerra a busca mesmo quando nenhuma fonte consegue caber")
    void ajusteEncerraEmAreaZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            WrapTextResult result = drawer.normalWrapText(FONT, "oração esperança", 0, 0);
            assertTrue(Double.isFinite(result.getFontSize()));
            assertTrue(result.getFontSize() >= 1 && result.getFontSize() <= FONT.getSize());
        });
    }

    @Test
    @Tag("defeito-conhecido")
    @DisplayName("Ajuste: quebra com fonte inicial 1 não deve produzir tamanho inferior a 1")
    void quebraRespeitaFonteMinima() {
        WrapTextResult result = drawer.normalWrapText(new Font(FONT.getName(), 1), "fé", 100, 1);
        assertTrue(result.getFontSize() >= 1);
    }

    @Test
    @DisplayName("Medição: maior largura visual pode pertencer à string com menos caracteres")
    void maiorLarguraVisual() {
        ArrayList<String> text = new ArrayList<>(List.of("iiiiii", "WWW", "fé"));
        assertTrue(width(FONT, "WWW") > width(FONT, "iiiiii"));
        assertEquals("WWW", drawer.longestLine(FONT, text));
        assertEquals((int) width(FONT, "WWW"), drawer.longestLine(FONT, lines("iiiiii", "WWW", "fé")));
    }

    @Test
    @DisplayName("Cifras: a posição usa a largura do prefixo da letra seguinte")
    void cifrasUsamLinhaSeguinte() {
        String chordLine = "    Cmaj7";
        String lyrics = "WWWW";
        int expected = (int) width(FONT, lyrics) + (int) width(FONT, "Cmaj7");
        assertEquals(expected, drawer.longestLine(FONT, lines(chordLine, lyrics)));
        assertTrue(drawer.longestLine(FONT, lines(chordLine, lyrics))
                > drawer.longestLine(FONT, lines(chordLine, "iiii")));
    }

    @Test
    @DisplayName("Cifras: completa a letra curta com espaços até a posição da cifra")
    void cifrasAlemDoFimDaLetra() {
        int expected = (int) width(FONT, "fé    ") + (int) width(FONT, "Cmaj7");
        assertEquals(expected, drawer.longestLine(FONT, lines("      Cmaj7", "fé")));
    }

    @Test
    @DisplayName("Cifras: sem próxima linha, mede a própria linha sem acessar índice inexistente")
    void cifraNaUltimaLinha() {
        assertEquals((int) width(FONT, "C G Am"), drawer.longestLine(FONT, lines("C G Am")));
    }

    @Test
    @DisplayName("Limite: lista vazia tem largura zero, nenhuma maior string e mantém a fonte")
    void listasVazias() {
        assertEquals(0, drawer.longestLine(FONT, List.<LyricLine>of()));
        assertNull(drawer.longestLine(FONT, new ArrayList<String>()));
        assertEquals(FONT.getSize(), drawer.pickFontSize(FONT, List.of(), 100, 100));
        assertEquals(FONT.getSize(), drawer.pickSmallFontSize(FONT, new String[0], 100, 100));
    }

    @Test
    @DisplayName("Divisão: escolhe o delimitador mais próximo do centro e o mantém na primeira parte")
    void divideNoCentro() {
        assertArrayEquals(new String[]{"ab,cd,", "ef,gh"}, WordDrawer.splitMiddle("ab,cd,ef,gh", ','));
    }

    @Test
    @DisplayName("Divisão: em empate entre delimitadores mantém a primeira ocorrência")
    void empateNaDivisao() {
        assertArrayEquals(new String[]{"abc,", "e,gh"}, WordDrawer.splitMiddle("abc,e,gh", ','));
    }

    @Test
    @DisplayName("Divisão: delimitador ausente retorna primeira parte vazia e segunda intacta")
    void delimitadorAusente() {
        assertArrayEquals(new String[]{"", "oração"}, WordDrawer.splitMiddle("oração", ','));
        assertArrayEquals(new String[]{"", ""}, WordDrawer.splitMiddle("", ','));
    }

    @Test
    @DisplayName("Delimitadores: em 10 caracteres aceita posições 2 a 7 e rejeita as extremidades")
    void regiaoCentral() {
        for (int index = 0; index < 10; index++) {
            StringBuilder line = new StringBuilder("abcdefghij");
            line.setCharAt(index, ',');
            assertEquals(index >= 2 && index <= 7,
                    WordDrawer.containsNotAtEnd(line.toString(), ","), "Posição " + index);
        }
        assertFalse(WordDrawer.containsNotAtEnd("abcdefghij", ","));
    }

    @Test
    @DisplayName("Delimitadores: texto curto não perde caracteres por arredondamento; vazio não contém vírgula")
    void delimitadoresEmTextoCurto() {
        assertTrue(WordDrawer.containsNotAtEnd(",abc", ","));
        assertFalse(WordDrawer.containsNotAtEnd("", ","));
        assertFalse(WordDrawer.containsNotAtEnd("abXYefghij", "XYeX"));
    }

    @Test
    @Tag("defeito-conhecido")
    @DisplayName("Texto secundário: quebra interna equivale a dois elementos separados")
    void textoSecundarioComQuebrasInternas() {
        double height = new FXFontMetrics(FONT).getLineHeight() * 1.25;
        double split = drawer.pickSmallFontSize(FONT, new String[]{"oração", "esperança"}, 10000, height);
        double embedded = drawer.pickSmallFontSize(FONT, new String[]{"oração\nesperança"}, 10000, height);
        assertTrue(split < FONT.getSize());
        assertEquals(split, embedded, EPS, "A mesma quantidade de linhas deve produzir o mesmo tamanho");
    }

    @Test
    @Tag("defeito-conhecido")
    @DisplayName("Texto secundário: linha vazia interna também ocupa altura")
    void textoSecundarioContaLinhaVazia() {
        double split = drawer.pickSmallFontSize(FONT, new String[]{"fé", "", "ação"}, 10000, 70);
        double embedded = drawer.pickSmallFontSize(FONT, new String[]{"fé\n\nação"}, 10000, 70);
        assertEquals(split, embedded, EPS);
    }

    @Test
    @DisplayName("Texto secundário: ajusta largura da maior linha")
    void larguraTextoSecundario() {
        double available = width(FONT, "oração esperança") / 2;
        double size = drawer.pickSmallFontSize(FONT, new String[]{"fé", "oração esperança"}, available, 1000);
        assertTrue(size >= 1 && size < FONT.getSize());
        assertTrue(width(new Font(FONT.getName(), size), "oração esperança") <= available + EPS);
    }

    @Test
    @DisplayName("Espaçamento: escala com a altura do canvas e zera quando a altura é zero")
    void espacamentoProporcional() {
        when(properties.getAdditionalLineSpacing()).thenReturn(10.0);
        for (double height : new double[]{0, 500, 1000, 2000}) {
            when(canvas.getHeight()).thenReturn(height);
            assertEquals(height / 100, drawer.getLineSpacing(), EPS);
        }
    }

    @Test
    @DisplayName("Escala: sem janela de projeção ou com largura zero retorna fator 1")
    void escalaSemProjecaoValida() {
        assertEquals(1, drawer.canvasScalingFactor());
        DisplayStage projection = mock(DisplayStage.class);
        when(app.getProjectionWindow()).thenReturn(projection);
        when(projection.getWidth()).thenReturn(0.0);
        assertEquals(1, drawer.canvasScalingFactor());
    }

    @Test
    @DisplayName("Escala: compara larguras, inclusive canvas de largura zero")
    void escalaComDimensoesDiferentes() {
        DisplayStage projection = mock(DisplayStage.class);
        when(app.getProjectionWindow()).thenReturn(projection);
        when(projection.getWidth()).thenReturn(1600.0);
        for (double width : new double[]{0, 400, 800, 1600, 3200}) {
            when(canvas.getWidth()).thenReturn(width);
            assertEquals(width / 1600, drawer.canvasScalingFactor(), EPS);
        }
    }

    @Test
    @DisplayName("Limpeza: estado inicial falso, atualização e isolamento por canvas")
    void estadoDeLimpezaPorCanvas() {
        assertFalse(drawer.getLastClearedState());
        drawer.setLastClearedState(true);
        assertTrue(drawer.getLastClearedState());
        drawer.setCanvas(mock(DisplayCanvas.class));
        assertFalse(drawer.getLastClearedState());
        drawer.setLastClearedState(false);
        drawer.setCanvas(canvas);
        assertTrue(drawer.getLastClearedState());
        drawer.setLastClearedState(false);
        assertFalse(drawer.getLastClearedState());
    }

    @Test
    @DisplayName("Limpeza: eraseText solicita texto nulo, transição e tamanho automático")
    void solicitaRemocaoDoTexto() {
        drawer.eraseText();
        assertEquals(1, drawer.setTextCalls);
        assertNull(drawer.text);
        assertNull(drawer.translations);
        assertNull(drawer.smallText);
        assertTrue(drawer.fade);
        assertEquals(-1, drawer.fontSize);
    }

    @Test
    @DisplayName("Desenho: delega fonte automática e ajusta fundo às dimensões atuais do canvas")
    void desenhaEAjustaImagemDeFundo() {
        ImageView image = new ImageView();
        when(canvas.getCanvasBackground()).thenReturn(image);
        drawer.draw(mock(Displayable.class));
        assertEquals(1, drawer.drawCalls);
        assertEquals(-1, drawer.fontSize);
        assertFalse(drawer.dumbWrap);
        assertEquals(800, image.getFitWidth());
        assertEquals(1000, image.getFitHeight());
        when(canvas.getWidth()).thenReturn(320.0);
        when(canvas.getHeight()).thenReturn(180.0);
        drawer.draw(mock(Displayable.class), 24);
        assertEquals(24, drawer.fontSize);
        assertEquals(320, image.getFitWidth());
        assertEquals(180, image.getFitHeight());
    }

    @Test
    @DisplayName("Desenho: passagem bíblica ativa quebra simples mesmo sem imagem de fundo")
    void desenhoBiblicoSemFundo() {
        drawer.draw(mock(BiblePassage.class));
        assertEquals(1, drawer.drawCalls);
        assertTrue(drawer.dumbWrap);
    }

    private static double width(Font font, String value) {
        return new FXFontMetrics(font).computeStringWidth(value);
    }

    private static List<LyricLine> lines(String... values) {
        return java.util.Arrays.stream(values).map(LyricLine::new).toList();
    }

    private static String content(WrapTextResult result) {
        return result.getNewText().stream().map(LyricLine::getLine).collect(Collectors.joining());
    }

    /** Dublê concreto: registra somente as chamadas abstratas feitas pela classe base. */
    private static class DrawerTeste extends WordDrawer {
        int setTextCalls;
        int drawCalls;
        String[] text;
        String[] translations;
        String[] smallText;
        boolean fade;
        boolean dumbWrap;
        double fontSize;

        DrawerTeste() { lastClearedState = new HashMap<>(); }
        @Override public void setTheme(ThemeDTO theme) { }
        @Override public ThemeDTO getTheme() { return null; }
        @Override public void setCapitaliseFirst(boolean value) { }
        @Override public void setText(TextDisplayable displayable, int index) { }
        @Override public void clear() { }
        @Override public void requestFocus() { }

        @Override
        public void setText(String[] text, String[] translations, String[] smallText, boolean fade, double size) {
            setTextCalls++;
            this.text = text;
            this.translations = translations;
            this.smallText = smallText;
            this.fade = fade;
            this.fontSize = size;
        }

        @Override
        protected void drawText(double size, boolean dumbWrap) {
            drawCalls++;
            this.fontSize = size;
            this.dumbWrap = dumbWrap;
        }
    }
}
