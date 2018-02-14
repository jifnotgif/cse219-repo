package actions;

import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import vilij.components.ConfirmationDialog;
import vilij.components.ConfirmationDialog.Option;
import vilij.components.Dialog;

import java.io.IOException;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;


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

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        try {
            promptToSave();
        } catch (IOException ex) {
        }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        try {
            applicationTemplate.stop();
            System.exit(0);
        } catch (Exception ex) {
        }
        
        
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
        s.show("Save File", "Are you sure you want to start a new file? You will lose any unsaved progress!");
        Option userSelection = s.getSelectedOption();
        if(userSelection == Option.YES){
            FileChooser t = new FileChooser();
            t.setTitle("Save file");
            t.getExtensionFilters().add(new ExtensionFilter("Tab-Separated Data File (.tsd)","*.tsd"));
            t.setInitialFileName("Untitled.tsd");
            t.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            return true;
        }
        else if(userSelection == Option.NO){
            return true;
        }
        
        return false;
    }
}
