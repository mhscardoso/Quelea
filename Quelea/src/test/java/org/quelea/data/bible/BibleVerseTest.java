package org.quelea.data.bible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.*; 

import java.io.StringReader;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class BibleVerseTest {
    private BibleVerse bibleVerse;
    private BibleChapter chapterNull;
    @BeforeEach
    void createBibleVerse() {
        chapterNull = null;
        bibleVerse = new BibleVerse(chapterNull, "No princípio", 1);
    }

    @Test // Caso 1: Objeto null
    void testEquals_1() {
        boolean result = bibleVerse.equals(null);
        assertEquals(false, result);
    }

    @Test // Caso 2: Objeto de classe diferente
    void testEquals_2() {
        String verso = "No princípio";
        boolean result = bibleVerse.equals(verso);
        assertEquals(false, result);
    }

    @Test // Caso 3: Objeto com verso diferente
    void testEquals_3() {
        BibleVerse bibleVerseTest = new BibleVerse(chapterNull, "No final", 1);
        boolean result = bibleVerse.equals(bibleVerseTest);
        assertEquals(false, result);
    }

    @Test // Caso 4: Objeto com número diferente
    void testEquals_4() {
        BibleVerse bibleVerseTest = new BibleVerse(chapterNull, "No princípio", 2);
        boolean result = bibleVerse.equals(bibleVerseTest);
        assertEquals(false, result);
    }

    @Test // Caso 5: Objetos iguais 
    void testEquals_5() {
        BibleVerse bibleVerseTest = new BibleVerse(chapterNull, "No princípio", 1);
        boolean result = bibleVerse.equals(bibleVerseTest);
        assertEquals(true, result);
    }

    @Test // Caso 1: cnumber null - cria BibleVerse sem chapter e chapterNum
    void testParseXML_1() throws Exception{
        String xmlContent = "<vers vnumber=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNull(createdBibleChapter);
        assertNotNull(createdBibleVerse);

        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(0, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());
    }

    @Test // Caso 2: Usando cnumber + vnumber
    void testParseXML_2() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" vnumber=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNotNull(createdBibleVerse);
        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(1, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());

        assertNotNull(createdBibleChapter);
        assertEquals(1, createdBibleChapter.getNum());
    }

    @Test // Caso 3: Usando cnumber + number
    void testParseXML_3() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" number=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNotNull(createdBibleVerse);
        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(1, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());

        assertNotNull(createdBibleChapter);
        assertEquals(1, createdBibleChapter.getNum());
    }

    @Test  // Caso 4: Usando cnumber + n
    void testParseXML_4() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" n=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNotNull(createdBibleVerse);
        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(1, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());

        assertNotNull(createdBibleChapter);
        assertEquals(1, createdBibleChapter.getNum());
    }

    @Test  // Caso 5: Usando cnumber + id
    void testParseXML_5() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" id=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNotNull(createdBibleVerse);
        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(1, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());

        assertNotNull(createdBibleChapter);
        assertEquals(1, createdBibleChapter.getNum());
    }

    @Test  // Caso 6: Usando cnumber + osisID
    void testParseXML_6() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" osisID=\"2\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);
        BibleChapter createdBibleChapter = createdBibleVerse.getChapter();

        assertNotNull(createdBibleVerse);
        assertEquals(2, createdBibleVerse.getNum());
        assertEquals(1, createdBibleVerse.getChapterNum());
        assertEquals("No meio", createdBibleVerse.getVerseText());

        assertNotNull(createdBibleChapter);
        assertEquals(1, createdBibleChapter.getNum());
    }

    @Test  // Caso 7: Exceção de formatação de número de capítulo (retorna null)
    void testParseXML_7() throws Exception{
        String xmlContent = "<vers cnumber =\"1\" vnumber=\"nan\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertNull(createdBibleVerse);
    }

    @Test // Caso 8: Exceção de formatação no cnumber (lança NumberFormatException)
    void testParseXML_8() throws Exception {
        String xmlContent = "<vers cnumber=\"nan\" vnumber=\"1\">No meio</vers>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        assertThrows(NumberFormatException.class, () -> {
            BibleVerse.parseXML(node);
        });
    }

    @Test
    void testHashCode() {
        int hash = bibleVerse.hashCode();

        int expected = 5;
        expected = 97 * expected + Objects.hashCode("No princípio");
        expected = 97 * expected + 1;

        assertEquals(expected, hash);
    }

    @Test
    void testToXML() {
        String result = bibleVerse.toXML();
        assertEquals(
                "<vers cnumber=\"0\" vnumber=\"1\">No princípio</vers>",
                result);
    }
}
