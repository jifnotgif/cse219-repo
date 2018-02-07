package ui;

import actions.AppActions;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

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
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    
    
    public ScatterChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
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
        // TODO for homework 1
        /* clear previous data points */
    }

    private void layout() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        VBox vPane = new VBox(10);
        
        Label boxTitle = new Label("Data File");
        boxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        vPane.getChildren().add(boxTitle);
        
        
        textArea = new TextArea();
        vPane.getChildren().add(textArea);
        
        
        displayButton = new Button("Display");
        
        vPane.getChildren().add(displayButton);
        
        vPane.setSpacing(10);
        grid.add(vPane,0,0);
        
        
        NumberAxis xAxis = new NumberAxis(0, 110, 10);
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        chart = new ScatterChart<Number, Number>(xAxis, yAxis);
        chart.setTitle("Data Visualization");
        grid.add(chart, 1, 0);
        
        displayButton.setOnAction(e -> {
            try{
                ObservableList dataArray = textArea.getParagraphs();
                ArrayList labels = new ArrayList();
                ArrayList points = new ArrayList();
                for(int i = 0; i < dataArray.size(); i++){
                    String line = dataArray.get(i).toString();
                    String id = line.substring(0, line.indexOf("\t"));
                    String label = line.substring(line.indexOf("\t"), line.lastIndexOf("\t")).trim();
                    String coords = line.substring(line.lastIndexOf("\t")).trim();
                    Integer xPoint = Integer.parseInt(coords.substring(0,coords.indexOf(",")));
                    Integer yPoint = Integer.parseInt(coords.substring(coords.indexOf(",")+1));
                    labels.add(label);
                    points.add(new ScatterChart.Data<Integer,Integer>(xPoint, yPoint));
            
                }
                
                System.out.println("ORIGINAL LISTS=========");
                System.out.println(labels);
                System.out.println(points);
                System.out.println("==============");
                while(!labels.isEmpty()){
                    ScatterChart.Series<Number, Number> series = new ScatterChart.Series<Number, Number>();
                    String initial = (String) labels.get(0);
                    series.setName(initial);
                    int i =0;
                    while(i<labels.size()){
                        if(initial.equals(labels.get(i))){
                            series.getData().add((Data)points.get(i));
                            points.remove(i);
                            System.out.println(i + " is removed");
                            labels.remove(i);
                            i--;
                        }
                        i++;
                    }
                    System.out.println(labels);
                    System.out.println(points);
                    System.out.println();
                    chart.getData().add(series);
                    
                }
                
            }
            catch(Throwable t){
                Alert error = new Alert(AlertType.ERROR);
                error.setHeaderText("Invalid input");
                error.setContentText("Inputs must be:\n\t be in the format '@(name) (label) (x,y)' \n\t each paramater separated by tabs\n\t each data entry separated by line ");
                error.show();
            }
            
        });
        
        appPane.getChildren().add(grid);
        
        
        
    }
    private void setWorkspaceActions() {
        // TODO for homework 1
    }
}
