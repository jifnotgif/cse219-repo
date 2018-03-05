package dataprocessors;

import java.io.FileWriter;
import java.nio.file.Files;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.TextArea;
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
    private ArrayList<String>   dataEntries;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        try{
            AtomicInteger index = new AtomicInteger(0);
            TextArea textbox = ((AppUI)applicationTemplate.getUIComponent()).getTextArea();
            String data = new String(Files.readAllBytes(dataFilePath));
            processor.processString(data);
            
            int len = data.split("\n").length;
            dataEntries = new ArrayList<>(Arrays.asList(data.split("\n")));
            if(len >10){
                String output = new String();
                for(int i =0; i< 10; i++){
                    output += dataEntries.get(0)+"\n";
                    dataEntries.remove(0);
                }
                ErrorDialog manyLines = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                manyLines.show("Loading data", "Loaded data consists of "+ len + " lines. Showing only the first 10 in the text area.");
                textbox.setText(output);
            }
            else{
                textbox.setText(data);
            }
            textbox.textProperty().addListener(e ->{
                if(textbox.getText().isEmpty() && dataEntries.isEmpty()) return;
                if(textbox.getText().split("\n").length < 10 && index.get() < dataEntries.size()){
                    textbox.appendText(dataEntries.get(index.getAndIncrement())+ "\n");
                    dataEntries.remove(0);   
                }
                index.set(0);
            });
            
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Invalid data", e.getMessage() +" \n\nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values\n" 
                        + "• Every data point has a unique name" );
           
        }
    }

    public void loadData(String dataString) throws Exception {
        try{
            processor.processString(dataString);
        }
        catch(Exception e){
            throw e;
           
        }
        
       
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
            String dataString =((AppUI)applicationTemplate.getUIComponent()).getStoredData();
            if(dataString == null) {
                dataString = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText();
            }
            processor.processString(dataString);
            FileWriter writer = new FileWriter(dataFilePath.toFile());
            writer.write(dataString);
            writer.close();
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show("Invalid data", e.getMessage() +" \nData points are in the following format:\n"
                        + "@name label x,y\n\n"+
                        "• Make sure each section is separated by a tab\n"
                        + "• Each data value is stored in a single line\n"
                        + "• Make sure there are no empty or incomplete data values\n\n"
                        + "• Every data point has a unique name");
           
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

    public ArrayList<String> getFileData(){
        return dataEntries;
    }
    
    public void resetData(){
        dataEntries.clear();
    }
}
