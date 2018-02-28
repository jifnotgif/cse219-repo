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
import ui.AppUI;
import vilij.components.ErrorDialog;

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
            }
        } catch (IOException ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Error while attempting to create new file.");
        }
    }

    @Override
    public void handleSaveRequest() { 
        if(dataFilePath == null){
            File workingDirectory = new File(System.getProperty("user.dir"));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle("Save file");
            t.getExtensionFilters().add(new ExtensionFilter("Tab-Separated Data File","*.tsd"));
            t.setInitialFileName("Untitled");
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                    try{
                    ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                    }
                    catch(Exception e){
                        System.out.println("hi");
                        return;
                    }
                    //If there is error in data, don't continue... ONLY way I can think of atm is to change method signature of saveData() to return boolean
                    // current error: saves data regardless of error
                    FileWriter writer = new FileWriter(currentFile);
                    writer.write(((AppUI)applicationTemplate.getUIComponent()).getUserText());
                    writer.close();
                    ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                }catch (Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show("Error", "Error while attempting to save file.");
                }
            }
        }
        else{
            try{
                ((AppData)applicationTemplate.getDataComponent()).saveData(dataFilePath);
                FileWriter writer = new FileWriter(currentFile);
                writer.write(((AppUI)applicationTemplate.getUIComponent()).getUserText());
                writer.close();
                ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);

            }
            catch(Exception e){
                System.out.println("No active file found to save");
            }
        }
    }

    @Override
    public void handleLoadRequest() {
        /*
        select a file from filechooser
        if file is tsd format, continue
        if file isnt valid tsd, then throw error
        validate data using AppData#loadData(path)
        
        */
    }

    @Override
    public void handleExitRequest() {
        try {
            applicationTemplate.stop();
            System.exit(0);
        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Error", "Error while attempting to close the application.");}
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
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
        ConfirmationDialog s = (ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        s.show("Save Current Work", "Would you like to save your current work?");
        Option userSelection = s.getSelectedOption();
        if(userSelection == Option.YES){
            File workingDirectory = new File(System.getProperty("user.dir"));
            FileChooser t = new FileChooser();
            t.setInitialDirectory(workingDirectory);
            t.setTitle("Save file");
            t.getExtensionFilters().add(new ExtensionFilter("Tab-Separated Data File","*.tsd"));
            t.setInitialFileName("Untitled");
            currentFile = t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if(currentFile != null){
                try{
                    FileWriter writer = new FileWriter(currentFile);
                    writer.write(((AppUI)applicationTemplate.getUIComponent()).getUserText());
                    writer.close();
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                }catch (IOException ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show("Error", "Error while attempting to save file.");
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
}
