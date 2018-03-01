package dataprocessors;

import actions.AppActions;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        try{
            String data = new String(Files.readAllBytes(dataFilePath));
            processor.processString(data);
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setText(data);
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Invalid data", e.getMessage() +" \nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values" );
           
        }
    }

    public void loadData(String dataString) throws Exception {
        try{
            processor.processString(dataString);
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Invalid input", e.getMessage() +" \nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values" );
           
        }
        
       
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
            processor.processString(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
//            FileWriter writer = new FileWriter(((AppActions)applicationTemplate.getActionComponent()).getCurrentFile());
            FileWriter writer = new FileWriter(dataFilePath.toFile());
            writer.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            writer.close();
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Invalid data", e.getMessage() +" \nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values" );
           
        }
        
    }

    @Override
    public void clear() {
        processor.clear();
    }

    
    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
    
    public TSDProcessor getProcessor(){
        return processor;
    }

}
