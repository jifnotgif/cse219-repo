package actions;

import java.io.File;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import vilij.components.ConfirmationDialog;
import vilij.components.ConfirmationDialog.Option;
import vilij.components.Dialog;

import java.io.IOException;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import dataprocessors.AppData;
import java.io.FileWriter;;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import ui.AppUI;
import vilij.components.ErrorDialog;
import static settings.AppPropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;
    File currentFile;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        try {
            if(dataFilePath == null){
                promptToSave();
            }else{
                ((AppUI)applicationTemplate.getUIComponent()).clear();
                ((AppData)applicationTemplate.getDataComponent()).resetData();
                ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setText("");
                currentFile = null;
                dataFilePath = null;
            }
        } catch (IOException ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(NEW_FILE_ERROR.name()));
        }
    }

    @Override
    public void handleSaveRequest() { 
        if(dataFilePath == null){
            File workingDirectory = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name())));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_DIALOG_TITLE.name()));
            t.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
            t.setInitialFileName(applicationTemplate.manager.getPropertyValue(DEFAULT_FILE_NAME.name()));
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                    ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                    ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                    
                }catch (Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_FILE_ERROR.name()));
                }
            }
        }
        else{
            try{
                //assumed that user hit display button before saving. fix bug
                if(((AppUI)applicationTemplate.getUIComponent()).getStoredData() == null) throw new NullPointerException(applicationTemplate.manager.getPropertyValue(READ_DATA_FAIL.name()));
                ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);

            }
            catch(NullPointerException e){
                ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), e.getMessage() );
            }
        }
    }

    @Override
    public void handleLoadRequest() {
        File workingDirectory = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name())));
        FileChooser t = new FileChooser();
        t.setInitialDirectory(workingDirectory);
        t.setTitle(applicationTemplate.manager.getPropertyValue(LOAD_DIALOG_TITLE.name()));
        t.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
        currentFile = t.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(currentFile != null){
                try{
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                    ConfirmationDialog existingData = (ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                    existingData.show(applicationTemplate.manager.getPropertyValue(CLEAR_INTERFACE_TITLE.name()), applicationTemplate.manager.getPropertyValue(CLEAR_INTERFACE_DESC.name()));
                    Option userSelection = existingData.getSelectedOption();
                    if(userSelection == Option.YES) {
                        ((AppUI)applicationTemplate.getUIComponent()).clear();
                        ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setText("");
                        ((AppData)applicationTemplate.getDataComponent()).loadData(dataFilePath);
                    }
                    else return;
                    
                }catch (Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), ex.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
                }
            }
    }

    @Override
    public void handleExitRequest() {
        try {
            ConfirmationDialog exitRequest = (ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            exitRequest.show(applicationTemplate.manager.getPropertyValue(EXIT_TITLE.name()), applicationTemplate.manager.getPropertyValue(EXIT_WHILE_RUNNING_WARNING.name()));
            Option userSelection = exitRequest.getSelectedOption();
            if(userSelection == Option.YES) {
                applicationTemplate.stop();
                System.exit(0);
            }
            
        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(EXIT_APP_ERROR.name()));
    
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        try{
            if(!(((AppData)applicationTemplate.getDataComponent()).getProcessor().isChartEmpty())){
                LineChart chart = ((AppUI)applicationTemplate.getUIComponent()).getChart();
                WritableImage image = chart.snapshot(new SnapshotParameters(), null);
               
                File workingDirectory = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name())));
                FileChooser t = new FileChooser();
                t.setInitialDirectory(workingDirectory);
                t.setTitle(applicationTemplate.manager.getPropertyValue(SCREENSHOT_DIALOG_TITLE.name()));
                t.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(IMAGE_FILE_TYPE_NAME.name()), applicationTemplate.manager.getPropertyValue(SCREENSHOT_FILE_EXT.name())));
                t.setInitialFileName(applicationTemplate.manager.getPropertyValue(DEFAULT_FILE_NAME.name()));
                
                File file = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), applicationTemplate.manager.getPropertyValue(IMAGE_FILE_TYPE_PARAM.name()), file);
            }
            
        }
        catch(IOException e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SCREENSHOT_FILE_ERROR.name()));
        }
        catch(IllegalArgumentException e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SCREENSHOT_OUTPUT_ERROR.name()));
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        ConfirmationDialog save = (ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        save.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        Option userSelection = save.getSelectedOption();
        if(userSelection == Option.YES){
            File workingDirectory = new File(System.getProperty(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name())));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
            t.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
            t.setInitialFileName(applicationTemplate.manager.getPropertyValue(DEFAULT_FILE_NAME.name()));
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    FileWriter writer = new FileWriter(currentFile);
                    writer.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
                    writer.close();
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                }catch (IOException ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_FILE_ERROR.name()));
                }
            }
            applicationTemplate.getUIComponent().clear();
            return true;
        }
        else if(userSelection == Option.NO){
            ((AppUI)applicationTemplate.getUIComponent()).clear();
            return true;
        }
        else{
            return false;
        }
    }
    
    public File getCurrentFile(){
        return currentFile;
    }
    
    public Path getDataPath(){
        return dataFilePath;
    }  
}
