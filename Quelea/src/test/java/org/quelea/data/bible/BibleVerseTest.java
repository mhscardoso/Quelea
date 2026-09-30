package org.quelea.data.bible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.w3c.dom.Node;

// Import correto de todas as asserções do JUnit 5 Jupiter
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

    @Test // Caso 1: 
    void testParseXML_1() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(null, createdBibleVerse);
    }

    @Test // Caso 2: 
    void testParseXML_2() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
    }

    @Test // Caso 3: 
    void testParseXML_3() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
    }

    @Test  // Caso 4: 
    void testParseXML_4() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
    }

    @Test  // Caso 5: 
    void testParseXML_5() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
    }

    @Test  // Caso 6: 
    void testParseXML_6() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
    }


    @Test  // Caso 7: 
    void testParseXML_7() throws Exception{
        String xmlContent = 
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Node node = doc.getDocumentElement();

        BibleVerse createdBibleVerse = BibleVerse.parseXML(node);

        assertEquals(bibleVerse, createdBibleVerse);
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
