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
import java.io.FileWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
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
                applicationTemplate.getUIComponent().clear();
                currentFile = null;
                dataFilePath = null;
            }
        } catch (IOException ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Error encountered while attempting to create new file.");
        }
    }

    @Override
    public void handleSaveRequest() { 
        if(dataFilePath == null){
            File workingDirectory = new File(System.getProperty("user.dir"));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle("Save file");
            t.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
            t.setInitialFileName("Untitled");
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                    ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                    ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                    
                }catch (Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show("Error", "Error encountered while attempting to save file.");
                }
            }
        }
        else{
            try{
                ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);

            }
            catch(Exception e){
                System.out.println("No active file found to save");
            }
        }
    }

    @Override
    public void handleLoadRequest() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        FileChooser t = new FileChooser();
        t.setInitialDirectory(workingDirectory);
        t.setTitle("Load file");
        t.getExtensionFilters().add(new ExtensionFilter("Tab-Separated Data File","*.tsd"));
        currentFile = t.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(currentFile != null){
                try{
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                    ConfirmationDialog existingData = (ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                    existingData.show("Clear workspace", "Are you sure you want to load new data? Any unsaved progress will be lost");
                    Option userSelection = existingData.getSelectedOption();
                    if(userSelection == Option.YES) ((AppData)applicationTemplate.getDataComponent()).loadData(dataFilePath);
                    else return;
                    
                }catch (Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show("Invalid input", ex.getMessage() +" \n\nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values\n" 
                        + "• Every data point has a unique name");
                }
            }
    }

    @Override
    public void handleExitRequest() {
        try {
            applicationTemplate.stop();
            System.exit(0);
        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Error encountered while attempting to close the application.");}
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
               
                File workingDirectory = new File(System.getProperty("user.dir"));
                FileChooser t = new FileChooser();
                t.setInitialDirectory(workingDirectory);
                t.setTitle("Save screenshot");
                t.getExtensionFilters().add(new ExtensionFilter("PNG","*.png"));
                t.setInitialFileName("Untitled");
                
                File file = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            }
            
        }
        catch(IOException e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Error encountered while attempting create a screenshot");
        }
        catch(IllegalArgumentException e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Failed to save screenshot because no output specified");
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
            File workingDirectory = new File(System.getProperty("user.dir"));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()));
            t.getExtensionFilters().add(new ExtensionFilter("Tab-Separated Data File","*.tsd"));
            t.setInitialFileName("Untitled");
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    FileWriter writer = new FileWriter(currentFile);
                    writer.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
                    writer.close();
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                }catch (IOException ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show("Error", "Error encountered while attempting to save file.");
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
    
}
