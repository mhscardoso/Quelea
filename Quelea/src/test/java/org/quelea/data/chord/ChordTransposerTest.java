package org.quelea.data.chord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe ChordTransposer.
 * Os casos seguem a ordem dos "Itens a serem testados" do Plano de Testes (seção 2.6).
 * Os casos marcados como DEFEITO PROVÁVEL devem falhar na implementação atual e
 * serão classificados na triagem (seção 2.3).
 */
public class ChordTransposerTest {

    private String transpose(String chord, int semitones, String newKey) {
        return new ChordTransposer(chord).transpose(semitones, newKey);
    }

    // ---------------------------------------------------------------
    // Item 1: Separação de nota e sufixo
    // ---------------------------------------------------------------

    @Test // Caso 1: Acorde simples, sem sufixo
    void testSufixo_1() {
        assertEquals("D", transpose("C", 2, null));
    }

    @Test // Caso 2: Sufixo menor é preservado
    void testSufixo_2() {
        assertEquals("Bm", transpose("Am", 2, null));
    }

    @Test // Caso 3: Sufixo composto (maj7) é preservado
    void testSufixo_3() {
        assertEquals("Dmaj7", transpose("Cmaj7", 2, null));
    }

    @Test // Caso 4: Sufixo com número (sus4) é preservado
    void testSufixo_4() {
        assertEquals("Asus4", transpose("Gsus4", 2, null));
    }

    @Test // Caso 5: Nota com alteração (#) e sufixo (m7) são separados corretamente
    void testSufixo_5() {
        assertEquals("Ebm7", transpose("C#m7", 2, null));
    }

    // ---------------------------------------------------------------
    // Item 2: Acordes com baixo invertido
    // ---------------------------------------------------------------

    @Test // Caso 6: Baixo natural transposto junto com o acorde
    void testBaixo_1() {
        assertEquals("D/F#", transpose("C/E", 2, null));
    }

    @Test // Caso 7: Baixo com sustenido
    void testBaixo_2() {
        assertEquals("E/G#", transpose("D/F#", 2, null));
    }

    @Test // Caso 8: Baixo com bemol
    void testBaixo_3() {
        assertEquals("E/G#", transpose("Eb/G", 1, null));
    }

    // ---------------------------------------------------------------
    // Item 3: Normalização da entrada
    // ---------------------------------------------------------------

    @Test // Caso 9: Primeira letra minúscula é convertida para maiúscula
    void testNormalizacao_1() {
        assertEquals("Bm", transpose("am", 2, null));
    }

    @Test // Caso 10: Espaços nas bordas são ignorados
    void testNormalizacao_2() {
        assertEquals("D", transpose("  C  ", 2, null));
    }

    @Test // Caso 11: A# é normalizado para Bb
    void testNormalizacao_3() {
        assertEquals("Bb", transpose("A#", 0, null));
    }

    @Test // Caso 12: Db é normalizado para C#
    void testNormalizacao_4() {
        assertEquals("C#", transpose("Db", 0, null));
    }

    @Test // Caso 13: Gb é normalizado para F#
    void testNormalizacao_5() {
        assertEquals("F#", transpose("Gb", 0, null));
    }

    @Test // Caso 14: G# é normalizado para Ab
    void testNormalizacao_6() {
        assertEquals("Ab", transpose("G#", 0, null));
    }

    @Test // Caso 15: Enarmônicos de notas naturais (B#, Cb, E#, Fb)
    void testNormalizacao_7() {
        assertEquals("C", transpose("B#", 0, null));
        assertEquals("B", transpose("Cb", 0, null));
        assertEquals("F", transpose("E#", 0, null));
        assertEquals("E", transpose("Fb", 0, null));
    }

    // ---------------------------------------------------------------
    // Item 4: Transposição por semitons
    // ---------------------------------------------------------------

    @Test // Caso 16: Zero semitons retorna o mesmo acorde
    void testSemitons_1() {
        assertEquals("C", transpose("C", 0, null));
    }

    @Test // Caso 17: +12 semitons (uma oitava) retorna o mesmo acorde
    void testSemitons_2() {
        assertEquals("C", transpose("C", 12, null));
    }

    @Test // Caso 18: -12 semitons retorna o mesmo acorde
    void testSemitons_3() {
        assertEquals("C", transpose("C", -12, null));
    }

    @Test // Caso 19: Transposição positiva com volta no ciclo (B + 1 = C)
    void testSemitons_4() {
        assertEquals("C", transpose("B", 1, null));
    }

    @Test // Caso 20: Transposição negativa com volta no ciclo (C - 1 = B)
    void testSemitons_5() {
        assertEquals("B", transpose("C", -1, null));
    }

    @Test // Caso 21: Transposição negativa gerando bemol (C - 2 = Bb)
    void testSemitons_6() {
        assertEquals("Bb", transpose("C", -2, null));
    }

    @Test // Caso 22: Mais de uma oitava (C + 14 = D)
    void testSemitons_7() {
        assertEquals("D", transpose("C", 14, null));
    }

    // ---------------------------------------------------------------
    // Item 5: Escolha entre sustenido e bemol pelo tom de destino
    // ---------------------------------------------------------------

    @Test // Caso 23: Tom de destino com # força sustenido (C + 3 em D# = D#)
    void testTomDestino_1() {
        assertEquals("D#", transpose("C", 3, "D#"));
    }

    @Test // Caso 24: Tom de destino com b força bemol (C + 6 em Gb = Gb)
    void testTomDestino_2() {
        assertEquals("Gb", transpose("C", 6, "Gb"));
    }

    @Test // Caso 25: Tom natural da lista de sustenidos (E) força sustenido
    void testTomDestino_3() {
        assertEquals("F#", transpose("D", 4, "E"));
    }

    @Test // Caso 26: Tom natural fora da lista (F) força bemol
    void testTomDestino_4() {
        assertEquals("Bb", transpose("G", 3, "F"));
    }

    @Test // Caso 27: Tom menor da lista (Bm) força sustenido
    void testTomDestino_5() {
        assertEquals("F#", transpose("E", 2, "Bm"));
    }

    @Test // Caso 28: Tom menor fora da lista (Fm) força bemol
    void testTomDestino_6() {
        assertEquals("Ab", transpose("F", 3, "Fm"));
    }

    @Test // Caso 29: Grafia forçada mantém o sufixo (C#m7 + 2 em F = Ebm7)
    void testTomDestino_7() {
        assertEquals("Ebm7", transpose("C#m7", 2, "F"));
    }

    // ---------------------------------------------------------------
    // Item 6: Grafia padrão sem tom de destino
    // ---------------------------------------------------------------

    @Test // Caso 30: Tabela interna: C#, Eb, F#, Ab, Bb
    void testGrafiaPadrao_1() {
        assertEquals("C#", transpose("C", 1, null));
        assertEquals("Eb", transpose("C", 3, null));
        assertEquals("F#", transpose("C", 6, null));
        assertEquals("Ab", transpose("C", 8, null));
        assertEquals("Bb", transpose("C", 10, null));
    }

    // ---------------------------------------------------------------
    // Item 7: Coerência do baixo com o acorde principal
    // ---------------------------------------------------------------

    @Test // Caso 31: Sem tom de destino, baixo segue a grafia (sustenido) do acorde principal
    void testBaixoCoerente_1() {
        assertEquals("F#/A#", transpose("C/E", 6, null));
    }

    @Test // Caso 32: Com tom bemol, acorde e baixo ficam em bemol
    void testBaixoCoerente_2() {
        assertEquals("Gb/Bb", transpose("C/E", 6, "Gb"));
    }

    @Test // Caso 33: Acorde principal em bemol converte baixo sustenido para natural equivalente
    void testBaixoCoerente_3() {
        assertEquals("Bb/D", transpose("A/C#", 1, null));
    }

    // ---------------------------------------------------------------
    // Item 8: Entradas nulas e vazias
    // ---------------------------------------------------------------

    @Test // Caso 34: Acorde nulo retorna nulo sem exceção
    void testNulo_1() {
        assertNull(transpose(null, 2, null));
    }

    @Test // Caso 35: String vazia retorna vazia sem exceção
    void testNulo_2() {
        assertEquals("", transpose("", 2, null));
    }

    // ---------------------------------------------------------------
    // Item 9: Casos de limite e defeitos prováveis
    // ---------------------------------------------------------------

    @Test // Caso 36: DEFEITO - nota H (notação alemã) aceita pelo LineTypeChecker.
          // Esperado: tratar como B (H + 2 = C#) ou retornar a entrada inalterada.
          // Observado: "Bb" (indexOf retorna -1 e o índice vira 1 silenciosamente).
    void testLimite_1() {
        String result = transpose("H", 2, null);
        assertTrue("C#".equals(result) || "H".equals(result),
                "Resultado inesperado para nota H: " + result);
    }

    @Test // Caso 37: DEFEITO - símbolo Unicode ♭ aceito pelo LineTypeChecker.
          // Esperado: B♭ + 2 = C. Observado: "C#♭" (♭ é tratado como sufixo).
    void testLimite_2() {
        assertEquals("C", transpose("B♭", 2, null));
    }

    @Test // Caso 38: DEFEITO - símbolo Unicode ♯ aceito pelo LineTypeChecker.
          // Esperado: E♯ + 2 = G. Observado: "F#♯" (♯ é tratado como sufixo).
    void testLimite_3() {
        assertEquals("G", transpose("E♯", 2, null));
    }

    @Test // Caso 39: DEFEITO - barra sem baixo ("C/") lança ArrayIndexOutOfBoundsException.
          // Esperado: não lançar exceção (tratar como "C").
    void testLimite_4() {
        assertDoesNotThrow(() -> transpose("C/", 2, null));
    }

    @Test // Caso 40: DEFEITO - entrada composta só pela barra ("/") lança exceção.
          // Esperado: não lançar exceção.
    void testLimite_5() {
        assertDoesNotThrow(() -> transpose("/", 2, null));
    }
}
