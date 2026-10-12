package org.quelea.services.importexport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.StatusPanel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Automated tests for {@link PlainTextParser}.
 * <p>
 * Scope: file import and filtering, page breaks, title rules, metadata extraction,
 * section separation and chord positioning. Chord <em>recognition</em> (which bracketed text
 * counts as a chord) belongs to the LineTypeChecker tests, not here.
 */
@ExtendWith(MockitoExtension.class)
class PlainTextParserTest {

    @TempDir
    Path tempDir;

    // Only a collaborator placeholder: the parser's signature requires it but never uses it.
    @Mock
    private StatusPanel statusPanel;

    private PlainTextParser parser;

    @BeforeAll
    static void setUpGlobalEnvironment(@TempDir Path propertiesDir) {
        QueleaProperties.init(propertiesDir.toString());
    }

    @BeforeEach
    void setUp() {
        parser = new PlainTextParser();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private File writeFile(String fileName, String content) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, content);
        return path.toFile();
    }

    private SongDisplayable parseSingleSong(String fileName, String content) throws IOException {
        List<SongDisplayable> songs = parser.getSongs(writeFile(fileName, content), statusPanel);
        assertThat(songs).hasSize(1);
        return songs.get(0);
    }

    private static String plainLyrics(SongDisplayable song) {
        return song.getLyrics(false, false, false);
    }

    private static String chordLyrics(SongDisplayable song) {
        return song.getLyrics(true, false, false);
    }

    // =========================================================================
    // File import & extension filtering
    // =========================================================================

    @Test
    @DisplayName("Imports a single .txt file, deriving the title from its filename")
    void should_ImportSingleTxtFile_When_ValidTxtFileProvided() throws IOException {
        SongDisplayable song = parseSingleSong("Amazing Grace.txt",
                "Amazing grace, how sweet the sound\nThat saved a wretch like me");

        assertThat(song.getTitle()).isEqualTo("Amazing Grace");
        assertThat(plainLyrics(song))
                .contains("Amazing grace, how sweet the sound")
                .contains("That saved a wretch like me");
    }

    @Test
    @DisplayName("Imports every .txt file in a directory and ignores other extensions")
    void should_ImportOnlyTxtFiles_When_DirectoryProvided() throws IOException {
        writeFile("Song One.txt", "Lyrics for the first song");
        writeFile("Song Two.txt", "Lyrics for the second song");
        writeFile("Chords.pdf", "PDF placeholder");
        writeFile("Notes.docx", "Word placeholder");

        List<SongDisplayable> songs = parser.getSongs(tempDir.toFile(), statusPanel);

        assertThat(songs).extracting(SongDisplayable::getTitle)
                .containsExactlyInAnyOrder("Song One", "Song Two");
    }

    @Test
    @DisplayName("Returns an empty list when a single non-.txt file is provided")
    void should_ReturnEmptyList_When_SingleNonTxtFileProvided() throws IOException {
        File nonTxtFile = writeFile("Hymn.doc", "Some content in doc format");

        assertThat(parser.getSongs(nonTxtFile, statusPanel)).isEmpty();
    }

    @Test
    @DisplayName("Imports an empty .txt file as a song with empty lyrics")
    void should_ImportEmptySong_When_TxtFileIsEmpty() throws IOException {
        SongDisplayable song = parseSingleSong("Empty Song.txt", "");

        assertThat(song.getTitle()).isEqualTo("Empty Song");
        assertThat(plainLyrics(song)).isEmpty();
    }


    @Test
    @Disabled("Pending fix for issue: PlainTextParser swallows missing file instead of throwing IOException")
    @DisplayName("Should propagate IOException when the target file does not exist")
    void should_ThrowIOException_When_FileDoesNotExist(@TempDir Path temporaryFolder) {
        File nonExistentFile = temporaryFolder.resolve("NonExistent.txt").toFile();

        assertThrows(IOException.class, () -> parser.getSongs(nonExistentFile, statusPanel));
    }

    // =========================================================================
    // Page break handling
    // =========================================================================

    @Test
    @DisplayName("Replaces a form feed with a newline (not just removes it)")
    void should_ReplaceFormFeedWithNewline_When_FileContainsPageBreak() throws IOException {
        SongDisplayable song = parseSingleSong("FormFeedSong.txt",
                "Line before page break\fLine after page break");

        assertThat(plainLyrics(song))
                .doesNotContain("\f")
                .contains("Line before page break\nLine after page break");
    }

    // =========================================================================
    // Title rules
    // =========================================================================

    @Test
    @DisplayName("Uses the filename as title when there is no Title metadata")
    void should_UseFilenameAsTitle_When_NoTitleMetadataExists() throws IOException {
        SongDisplayable song = parseSingleSong("Default Filename Title.txt",
                "Verse 1 line 1\nVerse 1 line 2");

        assertThat(song.getTitle()).isEqualTo("Default Filename Title");
    }

    @Test
    @DisplayName("Title metadata overrides the filename")
    void should_OverrideFilenameWithMetadataTitle_When_TitleMetadataIsPresent() throws IOException {
        SongDisplayable song = parseSingleSong("OriginalFilename.txt",
                "Title: Overridden Song Title\nAuthor: John Newton\n\nFirst verse line");

        assertThat(song.getTitle()).isEqualTo("Overridden Song Title");
    }

    @Test
    @DisplayName("Promotes a single-line first section to title when the filename gives no title")
    void should_PromoteFirstLineToTitle_When_FilenameIsUnknownAndFirstSectionIsSingleLine() throws IOException {
        SongDisplayable song = parseSingleSong(".txt",
                "Promoted Song Title\n\nVerse 1 Line 1\nVerse 1 Line 2");

        assertThat(song.getTitle()).isEqualTo("Promoted Song Title");
        assertThat(plainLyrics(song))
                .doesNotContain("Promoted Song Title")
                .contains("Verse 1 Line 1");
    }

    @Test
    @DisplayName("Keeps the (Unknown) title when the first section has several lines")
    void should_KeepUnknownTitle_When_FirstSectionContainsMultipleLines() throws IOException {
        SongDisplayable song = parseSingleSong(".txt",
                "Line 1 of section\nLine 2 of section\n\nVerse 2");

        assertThat(song.getTitle()).isEqualTo("(Unknown)");
        assertThat(plainLyrics(song)).contains("Line 1 of section");
    }

    // =========================================================================
    // Metadata extraction
    // =========================================================================

    @Test
    @DisplayName("Imports all supported metadata fields")
    void should_ImportAllSupportedMetadataFields_When_AllMetadataPresent() throws IOException {
        SongDisplayable song = parseSingleSong("CompleteMetadataSong.txt", """
                Title: My Awesome Hymn
                Author: Jane Doe
                CCLI: 9876543
                Copyright: 2026 Sovereign Grace
                Year: 2026
                Publisher: Quelea Publishing
                Key: E
                Capo: 2
                Notes: Fast tempo with acoustic intro

                Verse 1 line 1
                Verse 1 line 2
                """);

        assertThat(song.getTitle()).isEqualTo("My Awesome Hymn");
        assertThat(song.getAuthor()).isEqualTo("Jane Doe");
        assertThat(song.getCcli()).isEqualTo("9876543");
        assertThat(song.getCopyright()).isEqualTo("2026 Sovereign Grace");
        assertThat(song.getYear()).isEqualTo("2026");
        assertThat(song.getPublisher()).isEqualTo("Quelea Publishing");
        assertThat(song.getKey()).isEqualTo("E");
        assertThat(song.getCapo()).isEqualTo("2");
        assertThat(song.getInfo()).isEqualTo("Fast tempo with acoustic intro");
    }

    @Test
    @DisplayName("Matches prefixes case-insensitively and trims whitespace around values")
    void should_ImportMetadata_When_PrefixesHaveMixedCaseAndExtraWhitespace() throws IOException {
        // Built with explicit concatenation: a text block would strip the trailing spaces.
        String content = "title:   Spaced Title  \n"
                + "AUTHOR:   Spaced Author  \n"
                + "key:   G#m  \n"
                + "\n"
                + "Sing unto the Lord";

        SongDisplayable song = parseSingleSong("MixedCaseMetadata.txt", content);

        assertThat(song.getTitle()).isEqualTo("Spaced Title");
        assertThat(song.getAuthor()).isEqualTo("Spaced Author");
        assertThat(song.getKey()).isEqualTo("G#m");
    }

    @Test
    @DisplayName("Leaves metadata empty and uses the filename title when no metadata is present")
    void should_RetainDefaultValues_When_MetadataFieldsAreMissing() throws IOException {
        SongDisplayable song = parseSingleSong("NoMetadataSong.txt",
                "Just simple lyrics without any metadata headers");

        assertThat(song.getTitle()).isEqualTo("NoMetadataSong");
        assertThat(song.getAuthor()).isEmpty();
        assertThat(song.getCcli()).isEmpty();
        assertThat(song.getCopyright()).isEmpty();
        assertThat(song.getYear()).isEmpty();
        assertThat(song.getPublisher()).isEmpty();
        assertThat(song.getKey()).isEmpty();
        assertThat(song.getCapo()).isEmpty();
        assertThat(song.getInfo()).isEmpty();
    }

    static Stream<Arguments> multiPrefixMetadata() {
        return Stream.of(
                Arguments.of("CCLI Song # 1234567", (Function<SongDisplayable, String>) SongDisplayable::getCcli, "1234567"),
                Arguments.of("CCLI number: 7654321", (Function<SongDisplayable, String>) SongDisplayable::getCcli, "7654321"),
                Arguments.of("CCLI: 1122334", (Function<SongDisplayable, String>) SongDisplayable::getCcli, "1122334"),
                Arguments.of("Copyright: 2024 Integrity Music", (Function<SongDisplayable, String>) SongDisplayable::getCopyright, "2024 Integrity Music"),
                Arguments.of("© 2024 Bethel Music Publishing", (Function<SongDisplayable, String>) SongDisplayable::getCopyright, "2024 Bethel Music Publishing"),
                Arguments.of("Notes: Bridge repeat three times", (Function<SongDisplayable, String>) SongDisplayable::getInfo, "Bridge repeat three times"),
                Arguments.of("Info: Key change on verse 3", (Function<SongDisplayable, String>) SongDisplayable::getInfo, "Key change on verse 3")
        );
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{2}\"")
    @MethodSource("multiPrefixMetadata")
    @DisplayName("Recognises every alternative prefix for CCLI, Copyright and Notes")
    void should_ParseMetadata_When_UsingAlternativePrefixes(String metadataLine,
                                                            Function<SongDisplayable, String> getter,
                                                            String expectedValue) throws IOException {
        SongDisplayable song = parseSingleSong("PrefixSong.txt", metadataLine + "\n\nVerse lyrics");

        assertThat(getter.apply(song)).isEqualTo(expectedValue);
    }

    // =========================================================================
    // Section separation
    // =========================================================================

    @Test
    @DisplayName("Separates lyric sections with a blank line and last section needs no trailing newline")
    void should_SeparateSections_When_DividedByBlankLines() throws IOException {
        SongDisplayable song = parseSingleSong("MultiSectionSong.txt",
                "Verse 1 Line 1\nVerse 1 Line 2\n\nChorus Line 1\nChorus Line 2\n\nVerse 2 Line 1\nVerse 2 Line 2");

        assertThat(plainLyrics(song))
                .contains("Verse 1 Line 2\n\nChorus Line 1")
                .contains("Chorus Line 2\n\nVerse 2 Line 1")
                .contains("Verse 2 Line 2");
    }

    @Test
    @DisplayName("Ignores consecutive blank and whitespace-only lines instead of creating empty sections")
    void should_IgnoreConsecutiveBlankLines_When_MultipleBlankLinesOccur() throws IOException {
        SongDisplayable song = parseSingleSong("WhitespaceSeparatedSong.txt",
                "Verse 1 line 1\n\n\n   \n\t  \n\nVerse 2 line 1\n\n\n\n");

        assertThat(plainLyrics(song))
                .contains("Verse 1 line 1")
                .contains("Verse 2 line 1")
                .doesNotContain("   ")
                .doesNotContain("\t");
    }

    // =========================================================================
    // Chord positioning (chord recognition itself is covered elsewhere)
    // =========================================================================

    @Test
    @DisplayName("Moves chords above the lyrics, aligned to their original position")
    void should_AlignChordsAboveLyrics_When_InlineChordsPresent() throws IOException {
        SongDisplayable song = parseSingleSong("AlignedChordsSong.txt",
                "Amazing [G]grace how [C]sweet the [G]sound");

        assertThat(song.hasChords()).isTrue();
        assertThat(chordLyrics(song))
                .contains("        G         C         G\nAmazing grace how sweet the sound");
        assertThat(plainLyrics(song))
                .doesNotContain("[G]")
                .doesNotContain("[C]")
                .contains("Amazing grace how sweet the sound");
    }

    @Test
    @DisplayName("Pads the lyric with underscores and spaces the chords when two chords collide")
    void should_PadLyricsWithUnderscoresAndSpaceChords_When_ChordsCollide() throws IOException {
        SongDisplayable song = parseSingleSong("CollidingChordsSong.txt", "[C][D]singing praise");

        assertThat(song.hasChords()).isTrue();
        assertThat(chordLyrics(song)).contains("C D\n__singing praise");
    }

    @Test
    @DisplayName("Positions a chord at the end of a line after the last lyric character")
    void should_PositionTrailingChord_When_ChordIsAtTheEndOfLyricLine() throws IOException {
        SongDisplayable song = parseSingleSong("TrailingChordSong.txt",
                "Praise the Lord [G]\nHallelujah");

        assertThat(song.hasChords()).isTrue();
        assertThat(chordLyrics(song)).contains(" ".repeat(16) + "G\nPraise the Lord");
        assertThat(plainLyrics(song))
                .doesNotContain("[G]")
                .contains("Hallelujah");
    }
}
