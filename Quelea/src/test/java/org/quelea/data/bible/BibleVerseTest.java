package org.quelea.data.bible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.lang.reflect.InvocationTargetException;

// Temos 14 métodos na classe original, sendo 12 com complexidade 1 e 2 com complexidade 5 e 7.
// Ou seja, deveremos ter 24 testes no total.

public class BibleVerseTest {

    private BibleVerse bibleVerse;

    private static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    @BeforeEach
    void createBibleVerse() {
        try {
            Constructor<BibleVerse> pcc = BibleVerse.class.getDeclaredConstructor();
            pcc.setAccessible(true);
            bibleVerse = pcc.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            fail(e);
        }
    }

    @Test
    void testEquals() {

    }

    @Test
    void testGetChapter() throws Exception {
        Constructor<BibleChapter> pcc = BibleChapter.class.getDeclaredConstructor(int.class);
        pcc.setAccessible(true);
        BibleChapter bibleChapter = pcc.newInstance(2);
        setPrivateField(bibleVerse, "chapter", bibleChapter);
        BibleChapter chapter = bibleVerse.getChapter();
        assertEquals(bibleChapter, chapter);
    }

    @Test
    void testGetChapterNum() throws Exception {
        setPrivateField(bibleVerse, "chapterNum", 7);
        int chapterNum = bibleVerse.getChapterNum();
        assertEquals(7, chapterNum);
    }

    @Test
    void testGetName() throws Exception {
        setPrivateField(bibleVerse, "verse", "No princípio");
        setPrivateField(bibleVerse, "num", 1);

        String resultado = bibleVerse.getName();
        String esperado = "1 No princípio";

        assertEquals(esperado, resultado);
    }

    @Test
    void testGetNum() throws Exception {
        setPrivateField(bibleVerse, "num", 7);
        int num = bibleVerse.getNum();
        assertEquals(7, num);
    }

    @Test
    void testGetParent() throws Exception {
        Constructor<BibleChapter> pcc = BibleChapter.class.getDeclaredConstructor(int.class);
        pcc.setAccessible(true);
        BibleChapter bibleChapter = pcc.newInstance(2);
        setPrivateField(bibleVerse, "chapter", bibleChapter);
        BibleChapter chapter = bibleVerse.getChapter();
        assertEquals(bibleChapter, chapter);
    }

    @Test
    void testGetText() throws Exception {
        setPrivateField(bibleVerse, "verse", "No princípio");

        String resultado = bibleVerse.getText();
        String esperado = "No princípio";

        assertEquals(esperado, resultado);
    }

    @Test
    void testGetVerseText() throws Exception {
        setPrivateField(bibleVerse, "verse", "No princípio");

        String resultado = bibleVerse.getVerseText();
        String esperado = "No princípio";

        assertEquals(esperado, resultado);
    }

    @Test
    void testHashCode() throws Exception {
        setPrivateField(bibleVerse, "verse", "No princípio");
        setPrivateField(bibleVerse, "num", 1);

        int hash = bibleVerse.hashCode();

        int expected = 5;
        expected = 97 * expected + Objects.hashCode("No princípio");
        expected = 97 * expected + 1;

        assertEquals(expected, hash);
    }

    @Test
    void testParseXML() throws Exception {

    }

    @Test
    void testSetChapter() throws Exception {
        Constructor<BibleChapter> pcc = BibleChapter.class.getDeclaredConstructor(int.class);
        pcc.setAccessible(true);
        BibleChapter bibleChapter = pcc.newInstance(2);
        bibleVerse.setChapter(bibleChapter);
        BibleChapter chapter = (BibleChapter) getPrivateField(bibleVerse, "chapter");
        assertEquals(bibleChapter, chapter);
    }

    @Test
    void testSetChapterNum() throws Exception {
        Method method = bibleVerse.getClass().getDeclaredMethod("setChapterNum", int.class);
        method.setAccessible(true);
        method.invoke(bibleVerse, 7);
        int chapterNum = (int) getPrivateField(bibleVerse, "chapterNum");
        assertEquals(7, chapterNum);
    }

    @Test
    void testToString() throws Exception {
        setPrivateField(bibleVerse, "verse", "No princípio");
        setPrivateField(bibleVerse, "num", 1);

        String resultado = bibleVerse.toString();
        String esperado = "1 No princípio";

        assertEquals(esperado, resultado);
    }

    @Test
    void testToXML() throws Exception {
        setPrivateField(bibleVerse, "chapterNum", 1);
        setPrivateField(bibleVerse, "num", 1);
        setPrivateField(bibleVerse, "verse", "No princípio");

        String result = bibleVerse.toXML();
        assertEquals(
                "<vers cnumber=\"1\" vnumber=\"1\">No princípio</vers>",
                result);
    }
}
