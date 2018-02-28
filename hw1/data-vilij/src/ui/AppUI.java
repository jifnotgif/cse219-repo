package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;

import static java.io.File.separator;
import static settings.AppPropertyTypes.SCREENSHOT_ICON;
import static settings.AppPropertyTypes.SCREENSHOT_TOOLTIP;
import vilij.components.DataComponent;
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
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private String                       storedData;
    
    public ScatterChart<Number, Number> getChart() { return chart; }
    
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
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        ((AppData)applicationTemplate.getDataComponent()).clear();
        chart.getData().clear();
        hasNewText = false;
    }

    private void layout() {
        GridPane pane = new GridPane();
        

        pane.prefWidthProperty().bind(appPane.widthProperty());
        pane.setPadding(new Insets(10,10,10,10));
        
        
        VBox vPane = new VBox(10);
        vPane.setSpacing(10);
 
        Label boxTitle = new Label("Data File");
        boxTitle.setFont(Font.font("Arial", 18));
        
        
        textArea = new TextArea();
        textArea.setPrefWidth(300);
        textArea.setPrefHeight(150);


        displayButton = new Button("Display");
        
        vPane.getChildren().add(boxTitle);
        vPane.getChildren().add(textArea);
        vPane.getChildren().add(displayButton);
        
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new ScatterChart<Number, Number>(xAxis, yAxis);
        chart.setTitle("Data Visualization");
        chart.setMaxHeight((2*appPane.getHeight())/3);
        this.getPrimaryScene().getStylesheets().add(getClass().getClassLoader().getResource("css/chart.css").toExternalForm());
        
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
    
    public String getUserText(){
        return textArea.getText();
    }
    
    public Button getSaveButton(){
        return saveButton;
    }
    
    private void setWorkspaceActions() {
        textArea.textProperty().addListener(e -> {
            if(getUserText().equals(storedData)) hasNewText = false;
            else hasNewText = true;
            
            if(!getUserText().equals("")){
                newButton.setDisable(false);
                saveButton.setDisable(false);
            }
            else{
                newButton.setDisable(true);
                saveButton.setDisable(true);
            }

        });
        
        displayButton.setOnAction((ActionEvent e) -> {
           storedData = getUserText();
           try {
                    ((AppData)applicationTemplate.getDataComponent()).clear();
                    chart.getData().clear();
                    ((AppData)applicationTemplate.getDataComponent()).loadData(storedData);
                    if(hasNewText){
                        ((AppData)applicationTemplate.getDataComponent()).displayData();
                    }
               }
            
           catch(Exception ex) {
               System.out.println(ex+""); // Error should show up in GUI, not console
           }
        });
        
        /*
        add a tick box for read-only data
        event: grey out text area and make it uneditable
        */
        
    }
}
