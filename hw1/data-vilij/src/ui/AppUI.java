package ui;

import actions.AppActions;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import vilij.propertymanager.PropertyManager;

import static java.io.File.separator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.chart.LineChart;
import javafx.scene.text.Text;
import static settings.AppPropertyTypes.*;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;
    
    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private String                       scrnshoticonPath;
    private LineChart<Number, Number>    chart;               // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
//    private CheckBox                     tickBox;
    private Button                       toggleButton;
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private String                       storedData;
    private VBox                         vPane;
    private Text                         fileInfo;
    private String                       dataSource;
    
    private final String                 NEW_LINE = "\n";
    
    public LineChart<Number, Number> getChart() { return chart; }
    
    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }
    
    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                            manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                            manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(separator, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        super.setToolBar(applicationTemplate);
        scrnshotButton = setToolbarButton(scrnshoticonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        super.toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
            vPane.getChildren().remove(toggleButton);
            vPane.getChildren().remove(fileInfo);
            vPane.setVisible(false);
            applicationTemplate.getActionComponent().handleNewRequest();
            if(!((AppActions)applicationTemplate.getActionComponent()).getFlag()){
//                toggleButton.setVisible(true);
            vPane.getChildren().add(toggleButton);
            vPane.setVisible(true);
            }
            
        });
        
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> {
            vPane.setVisible(false);
            vPane.getChildren().remove(fileInfo);
            applicationTemplate.getActionComponent().handleLoadRequest();
            
            //Don't continue if data in file is invalid
            if(!((AppActions)applicationTemplate.getActionComponent()).getFlag()){
                vPane.getChildren().remove(toggleButton);
                getTextArea().setDisable(true);
                getSaveButton().setDisable(true);

                fileInfo.setText("Number of instances: " + ((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumInstances() +
                        "\nNumber of labels: " + ((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumLabels() +
                        "\nLabel names:\n\t• " + String.join("\n\t• ",((AppData)applicationTemplate.getDataComponent()).getProcessor().getLabels()) +
                        "\nSource: " + dataSource);
                vPane.getChildren().add(fileInfo);

                vPane.setVisible(true);
                }
            });
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
        
    }

    @Override
    public void clear() {
        storedData = "";
        chart.getData().clear();
        fileInfo.setText("");
    }

    private void layout() {
        GridPane pane = new GridPane();
        

        pane.prefWidthProperty().bind(appPane.widthProperty());
        pane.setPadding(new Insets(10,10,10,10));
        
        
        vPane = new VBox(10);
        vPane.setSpacing(10);
 
        Label boxTitle = new Label(applicationTemplate.manager.getPropertyValue(TEXTBOX_TITLE.name()));
        boxTitle.setFont(Font.font(applicationTemplate.manager.getPropertyValue(FONT.name()), 18));
        
        
        textArea = new TextArea();
        textArea.setPrefWidth(300);
        textArea.setPrefHeight(225);

        HBox hPane = new HBox();
        hPane.setSpacing(10);
        
//        displayButton = new Button(applicationTemplate.manager.getPropertyValue(DISPLAY_NAME.name()));
//        tickBox = new CheckBox(applicationTemplate.manager.getPropertyValue(READ_ONLY.name()));
        toggleButton = new Button(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));
        
//        Region region = new Region();
//        HBox.setHgrow(region, Priority.ALWAYS);

        hPane.getChildren().add(toggleButton);
        
        fileInfo = new Text();
        fileInfo.setWrappingWidth(300);
        
        pane.setAlignment(Pos.BOTTOM_CENTER);
        vPane.getChildren().add(boxTitle);
        vPane.getChildren().add(textArea);
//        vPane.getChildren().add(hPane);
//        vPane.getChildren().add(fileInfo);
        
        // Add algorithm selection
        
        vPane.setVisible(false);
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_TITLE.name()));
        chart.setMaxHeight((2*appPane.getHeight())/3);
        this.getPrimaryScene().getStylesheets().add(getClass().getClassLoader().getResource(applicationTemplate.manager.getPropertyValue(CSS_PATH.name())).toExternalForm());
        
        final RowConstraints row = new RowConstraints();
        row.setPrefHeight(chart.getMaxHeight());
        final ColumnConstraints textColumn = new ColumnConstraints();
        final ColumnConstraints chartColumn = new ColumnConstraints();
        chartColumn.setHgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row);
        pane.getColumnConstraints().addAll(textColumn, chartColumn);
        
        pane.add(vPane, 0, 0);
        pane.add(chart, 1, 0);
        appPane.getChildren().add(pane);
    }
    
    public TextArea getTextArea(){
        return textArea;
    }
    
    public Button getSaveButton(){
        return saveButton;
    }
    
    public VBox getVBox(){
        return vPane;
    }
    
    public String getStoredData(){
        return storedData;
    }
    
    public void setStoredData(String input){
        storedData = input;
    }
    
    private void setWorkspaceActions() {
        newButton.setDisable(false);
        
        textArea.textProperty().addListener(e -> {
            
            
            if(!textArea.getText().isEmpty()){
//                newButton.setDisable(false);
                saveButton.setDisable(false);
            }
            else{
//                newButton.setDisable(true);
                saveButton.setDisable(true);
            }
            
            if(textArea.getText().equals(storedData)) {
                hasNewText = false;
                saveButton.setDisable(true);
            }
            else hasNewText = true;
            
        });
        
        toggleButton.setOnAction(e -> {
            textArea.setDisable(!textArea.disableProperty().get());
            vPane.getChildren().remove(fileInfo);
            if(textArea.disableProperty().get()){
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(EDIT_BUTTON_NAME.name()));
                
                try {
                    clear();
                    ((AppData)applicationTemplate.getDataComponent()).clear();
                    String currentText = textArea.getText();
                    ArrayList<String> newDataEntries = new ArrayList<>(Arrays.asList(currentText.split(NEW_LINE)));
                    
                    ArrayList<String> fileDataEntries = ((AppData)applicationTemplate.getDataComponent()).getFileData();
                    
                    if(fileDataEntries != null){
                        for(String j : fileDataEntries ){
                            if(!newDataEntries.contains(j)){
                                newDataEntries.add(j);
                            }
                        }
                    }
                    
                    if(((AppActions)applicationTemplate.getActionComponent()).getDataPath() != null){
                        storedData = String.join(NEW_LINE, newDataEntries);
                    }
                    else storedData = textArea.getText();
                    
                    if(dataSource == null) dataSource = "";
                    ((AppData)applicationTemplate.getDataComponent()).loadData(storedData);
                    fileInfo.setText("Number of instances: " + ((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumInstances() +
                        "\nNumber of labels: " + ((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumLabels() +
                        "\nLabel names:\n\t• " + String.join("\n\t• ",((AppData)applicationTemplate.getDataComponent()).getProcessor().getLabels()) +
                        "\nSource: " + dataSource);
                    vPane.getChildren().add(fileInfo);
                    if(hasNewText){
                        ((AppData)applicationTemplate.getDataComponent()).displayData();
                        
                    }
                }
                catch(InvalidDataNameException | ArrayIndexOutOfBoundsException error){
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), error.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
                    textArea.setDisable(!textArea.disableProperty().get());
                    toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));
                }
                catch(Exception ex) {
                    ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(DATA_DISPLAY_FAIL_TITLE.name()), ex.getMessage());
                    textArea.setDisable(!textArea.disableProperty().get());
                    toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));
                }
            }
            else{
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));
            }
            
        });
//        displayButton.setOnAction(e -> {
//            try {
//                    clear();
//                    String currentText = textArea.getText();
//                    ArrayList<String> newDataEntries = new ArrayList<>(Arrays.asList(currentText.split(NEW_LINE)));
//                    
//                    ArrayList<String> fileDataEntries = ((AppData)applicationTemplate.getDataComponent()).getFileData();
//                    
//                    if(fileDataEntries != null){
//                        for(String j : fileDataEntries ){
//                            if(!newDataEntries.contains(j)){
//                                newDataEntries.add(j);
//                            }
//                        }
//                    }
//                    
//                    if(((AppActions)applicationTemplate.getActionComponent()).getDataPath() != null){
//                        storedData = String.join(NEW_LINE, newDataEntries);
//                    }
//                    else storedData = textArea.getText();
//                    
//                    
//                    ((AppData)applicationTemplate.getDataComponent()).loadData(storedData);
//                    
//                    if(hasNewText){
//                        ((AppData)applicationTemplate.getDataComponent()).displayData();
//                    }
//               }
//            catch(InvalidDataNameException | ArrayIndexOutOfBoundsException error){
//                ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
//                    err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), error.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
//            }
//            catch(Exception ex) {
//                ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
//                err.show(applicationTemplate.manager.getPropertyValue(DATA_DISPLAY_FAIL_TITLE.name()), ex.getMessage());
//           }
//        });

        // Toggle button for done and edit
//        tickBox.selectedProperty().addListener( e ->{
//            if(tickBox.isSelected()){
//                textArea.setDisable(true);
//            }
//            else{
//                textArea.setDisable(false);
//            }
//            
//        });
        
            
    
        chart.getData().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                if(!chart.getData().isEmpty()){
                    scrnshotButton.setDisable(false);
                }
                else{
                    scrnshotButton.setDisable(true);
                }  
                
            }
        });
    }
    
    public void setFilePath(String path){
        dataSource = path;
    }
}
