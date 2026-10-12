
package org.quelea.data;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import org.mockito.MockedConstruction;
import java.util.ArrayList;


import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScheduleSaverTest {

    @BeforeAll
    static void inicializarPropriedades() {
        QueleaProperties.init(System.getProperty("user.home"));
    }

    @BeforeAll
    static void iniciarJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // O JavaFX já foi inicializado.
        }
    }

    @AfterAll
    static void encerrarJavaFX() {
        Platform.exit();
    }

    @Test
    void deveSalvarAgendaExistenteComSucesso() throws InterruptedException {
        QueleaApp app = mock(QueleaApp.class, RETURNS_DEEP_STUBS);
        Schedule schedule = mock(Schedule.class);

        try (MockedStatic<QueleaApp> appMock = mockStatic(QueleaApp.class)) {
            appMock.when(QueleaApp::get).thenReturn(app);

            when(app.getMainWindow()
                    .getMainPanel()
                    .getSchedulePanel()
                    .getScheduleList()
                    .getSchedule()).thenReturn(schedule);

            when(schedule.getFile()).thenReturn(new File("agenda.quelea"));
            when(schedule.writeToFile()).thenReturn(true);

            SaveCallback callback = mock(SaveCallback.class);
            new ScheduleSaver().saveSchedule(false, callback);

            verify(callback, timeout(3000)).saved(true);
            verify(schedule).writeToFile();
        }
    }

    @Test
    void deveInformarFalhaAoSalvarAgenda() {
        QueleaApp app = mock(QueleaApp.class, RETURNS_DEEP_STUBS);
        Schedule schedule = mock(Schedule.class);

        try (MockedStatic<QueleaApp> appMock = mockStatic(QueleaApp.class)) {
            appMock.when(QueleaApp::get).thenReturn(app);

            when(app.getMainWindow()
                    .getMainPanel()
                    .getSchedulePanel()
                    .getScheduleList()
                    .getSchedule()).thenReturn(schedule);

            when(schedule.getFile()).thenReturn(new File("agenda.quelea"));
            when(schedule.writeToFile()).thenReturn(false);

            SaveCallback callback = mock(SaveCallback.class);
            new ScheduleSaver().saveSchedule(false, callback);

            verify(callback, timeout(3000)).saved(false);
            verify(schedule).writeToFile();
        }
    }
	
    @Test
    void deveSalvarAgendaSemCallback() throws InterruptedException {
        QueleaApp app = mock(QueleaApp.class, RETURNS_DEEP_STUBS);
        Schedule schedule = mock(Schedule.class);

        try (MockedStatic<QueleaApp> appMock = mockStatic(QueleaApp.class)) {
            appMock.when(QueleaApp::get).thenReturn(app);

            when(app.getMainWindow()
                    .getMainPanel()
                    .getSchedulePanel()
                    .getScheduleList()
                    .getSchedule()).thenReturn(schedule);

            when(schedule.getFile()).thenReturn(new File("agenda.quelea"));
            when(schedule.writeToFile()).thenReturn(true);

            new ScheduleSaver().saveSchedule(false, null);

            verify(schedule, timeout(3000)).writeToFile();
        }
    }
        
    @Test
    void deveFinalizarPainelDeStatusAposSalvar() {
        QueleaApp app = mock(QueleaApp.class, RETURNS_DEEP_STUBS);
        Schedule schedule = mock(Schedule.class);
        org.quelea.windows.main.StatusPanel statusPanel =
                mock(org.quelea.windows.main.StatusPanel.class);

        try (MockedStatic<QueleaApp> appMock = mockStatic(QueleaApp.class)) {
            appMock.when(QueleaApp::get).thenReturn(app);

            when(app.getMainWindow()
                    .getMainPanel()
                    .getSchedulePanel()
                    .getScheduleList()
                    .getSchedule()).thenReturn(schedule);

            when(app.getStatusGroup().addPanel(anyString()))
                    .thenReturn(statusPanel);

            when(schedule.getFile()).thenReturn(new File("agenda.quelea"));
            when(schedule.writeToFile()).thenReturn(true);

            new ScheduleSaver().saveSchedule(false, null);

            verify(statusPanel, timeout(3000)).done();
            verify(schedule).writeToFile();
        }
    }

    
    @Test
    void deveAssociarArquivoSelecionadoAAgenda() {
        QueleaApp app = mock(QueleaApp.class, RETURNS_DEEP_STUBS);
        Schedule schedule = mock(Schedule.class);

        File arquivoEscolhido = new File(
                System.getProperty("java.io.tmpdir"),
                "agenda-teste"
        );

        try (MockedStatic<QueleaApp> appMock = mockStatic(QueleaApp.class);
            MockedConstruction<FileChooser> chooserMock =
                    mockConstruction(FileChooser.class, (chooser, context) -> {
                        when(chooser.getExtensionFilters()).thenReturn(
                                FXCollections.observableArrayList()
                        );
                        when(chooser.showSaveDialog(any()))
                                .thenReturn(arquivoEscolhido);
                    })) {

            appMock.when(QueleaApp::get).thenReturn(app);

            when(app.getMainWindow()
                    .getMainPanel()
                    .getSchedulePanel()
                    .getScheduleList()
                    .getSchedule()).thenReturn(schedule);

            when(schedule.getFile()).thenReturn(null);
            when(schedule.writeToFile()).thenReturn(true);

            new ScheduleSaver().saveSchedule(false, null);

            verify(schedule, timeout(3000)).setFile(
                    new File(arquivoEscolhido.getAbsoluteFile() + ".qsch")
            );
        }
    }

}
