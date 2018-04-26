/**
 *
 * @author David Doo
 */
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
import javafx.scene.text.Font;
import javafx.stage.Stage;

import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import vilij.propertymanager.PropertyManager;

import static java.io.File.separator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
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

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private String scrnshoticonPath;
    private String settingsiconPath;
    private ScatterChart<Number, Number> scatterChart;               // the chart where data will be displayed
    private LineChart<Number, Number> lineChart;
//    private Button displayButton;  
    private Button toggleButton;
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private String storedData;
    private VBox vPane;
    private VBox classificationTypes;
    private VBox clusteringTypes;
    private StackPane charts;
    private Text fileInfo;

    private String dataSource;
    private int numLabels;
    private Set<String> labels;
    private final String NEW_LINE = "\n";
    private final String TAB = "\t";
    private VBox algorithmList;
    private VBox algorithmPane;
    private VBox algorithmTable;
    private ComboBox algorithmTypes;
    private ToggleGroup classificationGroup;
    private ToggleGroup clusteringGroup;
    private ArrayList<ToggleGroup> algorithmGroups;
    private Button runAlgorithm;
    private Stage algorithmConfigWindow;
    private ConfigState currentSettings;
    private ArrayList<Button> configButtons;
    private ArrayList<ConfigState> cachedSettings;
    private TextField clustersField;
    private TextField iterationsField;
    private TextField intervalsField;
    private CheckBox runOption;
    private Toggle algorithmValue;
    private ToggleGroup toggleGroup;
    private NumberAxis xAxis;
    private NumberAxis yAxis;


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
        settingsiconPath = String.join(separator, iconsPath, manager.getPropertyValue(SETTINGS_ICON.name()));
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

            clearCachedSettings();
            algorithmTable.getChildren().remove(algorithmTypes);
            algorithmPane.getChildren().remove(runAlgorithm);
            vPane.getChildren().remove(algorithmPane);
            vPane.setVisible(false);
            applicationTemplate.getActionComponent().handleNewRequest();

            if (!((AppActions) applicationTemplate.getActionComponent()).getFlag()) {
//                toggleButton.setVisible(true);
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));

                vPane.getChildren().add(toggleButton);
                vPane.setVisible(true);
            }

        });

        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> {

            applicationTemplate.getActionComponent().handleLoadRequest();

            //Don't continue if data in file is invalid
            if (!((AppActions) applicationTemplate.getActionComponent()).getFlag()) {
                vPane.setVisible(false);
                algorithmPane.getChildren().remove(runAlgorithm);
                runAlgorithm.setVisible(false);
                vPane.getChildren().remove(fileInfo);
                algorithmList.getChildren().clear();
                resetToggleOptions();
                clearCachedSettings();
                algorithmTable.getChildren().remove(algorithmTypes);
                vPane.getChildren().remove(algorithmPane);

                vPane.getChildren().remove(toggleButton);
                getTextArea().setDisable(true);
                getSaveButton().setDisable(true);

                setFileMetaData();
                vPane.getChildren().add(fileInfo);
                if (!algorithmPane.getChildren().contains(algorithmTable)) {
                    algorithmPane.getChildren().add(algorithmTable);
                }

                initializeAlgorithmTypes();
                algorithmTable.getChildren().add(algorithmTypes);
                algorithmPane.getChildren().add(runAlgorithm);
                runAlgorithm.setVisible(true);
                vPane.getChildren().add(algorithmPane);

                vPane.setVisible(true);
            }
            return;
        });

        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
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
        scatterChart.getData().clear();
        lineChart.getData().clear();
        fileInfo.setText("");
        numLabels = 0;
        currentSettings = null;
        runAlgorithm.setDisable(true);
        runAlgorithm.setStyle(null);
        runAlgorithm.setText("Run");

    }

    private void layout() {
        GridPane pane = new GridPane();

        pane.prefWidthProperty().bind(appPane.widthProperty());
        pane.setPadding(new Insets(10, 10, 10, 10));

        vPane = new VBox(10);
        vPane.setSpacing(10);
        vPane.setPrefHeight(applicationTemplate.getUIComponent().getPrimaryScene().getHeight());
        Label boxTitle = new Label(applicationTemplate.manager.getPropertyValue(TEXTBOX_TITLE.name()));
        boxTitle.setFont(Font.font(applicationTemplate.manager.getPropertyValue(FONT.name()), 18));

        textArea = new TextArea();
        textArea.setPrefWidth(300);
        textArea.setPrefHeight(150);
//        displayButton = new Button(applicationTemplate.manager.getPropertyValue(DISPLAY_NAME.name()));
//        tickBox = new CheckBox(applicationTemplate.manager.getPropertyValue(READ_ONLY.name()));
        toggleButton = new Button(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));

//        Region region = new Region();
//        HBox.setHgrow(region, Priority.ALWAYS);
        vPane.getChildren().add(toggleButton);

        fileInfo = new Text();
        fileInfo.setWrappingWidth(300);

        algorithmPane = new VBox(10);
        algorithmTable = new VBox(10);

        Label listHeader = new Label(applicationTemplate.manager.getPropertyValue(ALGORITHM_TYPES_TITLE.name()));
        listHeader.setId(applicationTemplate.manager.getPropertyValue(ALGORITHM_LIST_ID.name()));

        initializeAlgorithmTypes();
        algorithmTable.getChildren().addAll(listHeader, algorithmTypes);

        pane.setAlignment(Pos.BOTTOM_CENTER);
        vPane.getChildren().addAll(boxTitle, textArea);

        vPane.setVisible(false);

        algorithmList = new VBox(10);

        classificationTypes = new VBox(10);
        clusteringTypes = new VBox(10);

        algorithmGroups = new ArrayList<>();
        configButtons = new ArrayList<>();
        cachedSettings = new ArrayList<>();

        Image settingsImage = new Image(getClass().getResourceAsStream(settingsiconPath));
        // set each radiobutton's toggle group to select only one at a time
        Label classificationTypeTitle = new Label(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()));
        classificationTypeTitle.setId(applicationTemplate.manager.getPropertyValue(ALGORITHM_TITLE_ID.name()));

        Button classificationSettings1 = new Button();
        classificationSettings1.getStyleClass().add(applicationTemplate.manager.getPropertyValue(SETTINGS_CSS_CLASS.name()));
        classificationSettings1.setId(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()));
        classificationSettings1.setGraphic(new ImageView(settingsImage));
        configButtons.add(classificationSettings1);
        HBox classificationOption1 = new HBox();
        classificationOption1.setAlignment(Pos.CENTER_LEFT);
        RadioButton classificationType1 = new RadioButton(applicationTemplate.manager.getPropertyValue(CLASSIFICATION_OPTION_ONE.name()));
        classificationType1.setId("Random Classification");
        //add algo settings

        classificationGroup = new ToggleGroup();
        classificationType1.setToggleGroup(classificationGroup);
        classificationOption1.getChildren().add(classificationType1);
        classificationOption1.getChildren().add(classificationSettings1);

        algorithmGroups.add(classificationGroup);

        classificationTypes.getChildren().addAll(classificationTypeTitle, classificationOption1);

        Label clusteringTypeTitle = new Label(applicationTemplate.manager.getPropertyValue(CLUSTERING.name()));
        clusteringTypeTitle.setId(applicationTemplate.manager.getPropertyValue(ALGORITHM_TITLE_ID.name()));

        Button clusteringSettings1 = new Button();
        clusteringSettings1.getStyleClass().add(applicationTemplate.manager.getPropertyValue(SETTINGS_CSS_CLASS.name()));
        clusteringSettings1.setId(applicationTemplate.manager.getPropertyValue(CLUSTERING_ID.name()));
        clusteringSettings1.setGraphic(new ImageView(settingsImage));
        configButtons.add(clusteringSettings1);
        HBox clusteringOption1 = new HBox();
        clusteringOption1.setAlignment(Pos.CENTER_LEFT);
        RadioButton clusteringType1 = new RadioButton(applicationTemplate.manager.getPropertyValue(CLUSTERING_OPTION_ONE.name()));
        clusteringType1.setId("Random Clustering");

        clusteringGroup = new ToggleGroup();
        clusteringType1.setToggleGroup(clusteringGroup);
        clusteringOption1.getChildren().add(clusteringType1);
        clusteringOption1.getChildren().add(clusteringSettings1);

        algorithmGroups.add(clusteringGroup);
        clusteringTypes.getChildren().addAll(clusteringTypeTitle, clusteringOption1);

        algorithmPane.getChildren().addAll(algorithmTable, algorithmList);
        vPane.getChildren().add(algorithmPane);

        runAlgorithm = new Button(applicationTemplate.manager.getPropertyValue(RUN_BUTTON_NAME.name()));
        runAlgorithm.setDisable(true);
        runAlgorithm.setId("active");
        this.getPrimaryScene().getStylesheets().add(getClass().getClassLoader().getResource(applicationTemplate.manager.getPropertyValue(CSS_PATH.name())).toExternalForm());

        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_TITLE.name()));
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.getXAxis().setVisible(false);
        lineChart.getYAxis().setVisible(false);
        lineChart.setAnimated(false);
//        lineChart.getStylesheets().addAll(getClass().getResource("chart.css").toExternalForm());
        lineChart.setId("line");

        scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_TITLE.name()));
        scatterChart.setMaxHeight((2 * appPane.getHeight()) / 3);
        scatterChart.setLegendVisible(false);
        scatterChart.setAnimated(false);
        scatterChart.getXAxis().setVisible(true);
        scatterChart.getYAxis().setVisible(true);
        scatterChart.setId("scatter");
        
        final RowConstraints row = new RowConstraints();
        row.setPrefHeight(scatterChart.getMaxHeight());
        final ColumnConstraints textColumn = new ColumnConstraints();
        final ColumnConstraints chartColumn = new ColumnConstraints();
        chartColumn.setHgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row);
        pane.getColumnConstraints().addAll(textColumn, chartColumn);

//        scatterChart.prefWidthProperty().bind(lineChart.widthProperty());
//        lineChart.minWidthProperty().bind(scatterChart.widthProperty());
//        lineChart.maxWidthProperty().bind(scatterChart.widthProperty());

        pane.add(vPane, 0, 0);
        charts = new StackPane();
        charts.getChildren().addAll(lineChart, scatterChart);
        charts.setAlignment(Pos.TOP_LEFT);
        pane.add(charts, 1, 0);

        appPane.getChildren().add(pane);
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public VBox getVBox() {
        return vPane;
    }

    public String getStoredData() {
        return storedData;
    }

    public LineChart getLineChart() {
        return lineChart;
    }

    public void setStoredData(String input) {
        storedData = input;
    }

    private void setWorkspaceActions() {
        newButton.setDisable(false);

        textArea.textProperty().addListener(e -> {

            if (!textArea.getText().isEmpty()) {
//                newButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
//                newButton.setDisable(true);
                saveButton.setDisable(true);
            }

            if (textArea.getText().equals(storedData)) {
                hasNewText = false;
                saveButton.setDisable(true);
            } else {
                hasNewText = true;
            }

        });

        toggleButton.setOnAction(e -> {
            textArea.setDisable(!textArea.disableProperty().get());
            vPane.getChildren().remove(fileInfo);
            if (textArea.disableProperty().get()) {
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(EDIT_BUTTON_NAME.name()));
                if (!processData()) {
                    return;
                }
                vPane.getChildren().add(fileInfo);
                initializeAlgorithmTypes();
                algorithmTable.getChildren().add(algorithmTypes);
                algorithmPane.getChildren().add(runAlgorithm);
                vPane.getChildren().add(algorithmPane);
//                algorithmTable.getChildren().add(algorithmTypes);

            } else {
                if (!algorithmPane.getChildren().contains(algorithmTable)) {
                    algorithmPane.getChildren().add(algorithmTable);
                }
                algorithmPane.getChildren().remove(runAlgorithm);
                runAlgorithm.setDisable(true);
                algorithmList.getChildren().clear();
                resetToggleOptions();
                toggleOff();

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
        lineChart.getData().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                if (lineChart.getData().isEmpty()) {
                    scrnshotButton.setDisable(true);
                } 

            }
        });

        for (Object b : configButtons) {
            ((Button) b).setOnAction(handler -> {
                algorithmConfigWindow = new Stage();
                algorithmConfigWindow.initModality(Modality.APPLICATION_MODAL);
                algorithmConfigWindow.setTitle(applicationTemplate.manager.getPropertyValue(ALGO_SETTINGS_TITLE.name()));
                VBox mainPane = new VBox(20);
                mainPane.setPadding(new Insets(10));
                Label paneTitle = new Label(applicationTemplate.manager.getPropertyValue(ALGO_SETTINGS_TITLE.name()));
                GridPane content = new GridPane();
                content.setHgap(20);
                content.setVgap(50);
                content.setPadding(new Insets(10));
                Label iterationsTitle = new Label(applicationTemplate.manager.getPropertyValue(ITERATIONS_TITLE.name()));
                content.add(iterationsTitle, 0, 0);
                Label intervalsTitle = new Label(applicationTemplate.manager.getPropertyValue(INTERVALS_TITLE.name()));
                content.add(intervalsTitle, 0, 1);

                if (((Button) b).getId() != null && ((Button) b).getId().equals(applicationTemplate.manager.getPropertyValue(CLUSTERING_ID.name()))) {

                    Label numClustersTitle = new Label(applicationTemplate.manager.getPropertyValue(LABEL_COUNT_INFO.name()));
                    content.add(numClustersTitle, 0, 2);

                    clustersField = new TextField();
                    clustersField.setPrefWidth(65);
                    clustersField.setPromptText("1");
                    clustersField.setFocusTraversable(false);
                    content.add(clustersField, 1, 2);
                    if (!cachedSettings.isEmpty() && cachedSettings.get(configButtons.indexOf(((Button) b))) != null && ((Button) b).equals(cachedSettings.get(configButtons.indexOf(((Button) b))).getBtn())) {
                        clustersField.setText("" + cachedSettings.get(configButtons.indexOf(((Button) b))).getLabels());
                    }
                }

                Label runTitle = new Label("Continuous Run? ");
                content.add(runTitle, 0, 3);

                iterationsField = new TextField();
                iterationsField.setPrefWidth(65);
                iterationsField.setPromptText("1000");
                iterationsField.setFocusTraversable(false);
                content.add(iterationsField, 1, 0);
                if (!cachedSettings.isEmpty() && cachedSettings.get(configButtons.indexOf(((Button) b))) != null && ((Button) b).equals(cachedSettings.get(configButtons.indexOf(((Button) b))).getBtn())) {
                    iterationsField.setText("" + cachedSettings.get(configButtons.indexOf(((Button) b))).getIterations());
                }
                intervalsField = new TextField();
                intervalsField.setPrefWidth(65);
                intervalsField.setPromptText("5");
                intervalsField.setFocusTraversable(false);
                content.add(intervalsField, 1, 1);

                runOption = new CheckBox();
                content.add(runOption, 1, 3);

                if (!cachedSettings.isEmpty() && cachedSettings.get(configButtons.indexOf(((Button) b))) != null && ((Button) b).equals(cachedSettings.get(configButtons.indexOf(((Button) b))).getBtn())) {
                    iterationsField.setText("" + cachedSettings.get(configButtons.indexOf(((Button) b))).getIterations());
                    intervalsField.setText("" + cachedSettings.get(configButtons.indexOf(((Button) b))).getIntervals());
                    runOption.setSelected(cachedSettings.get(configButtons.indexOf(((Button) b))).isContinuousState());
                }

                currentSettings = new ConfigState();
                currentSettings.setBtn((Button) b);
                mainPane.getChildren().addAll(paneTitle, content);

                Scene subscene = new Scene(mainPane);
                algorithmConfigWindow.setScene(subscene);
                algorithmConfigWindow.showAndWait();
                //Saving the user's configuration settings
                Integer clusters;
                Integer intervals;
                Integer iterations;

                if (clustersField == null) {
                    clusters = 0;
                } else if (clustersField.textProperty().get().equals("") || Integer.parseInt(clustersField.textProperty().get()) <= 0) {
                    clusters = 1;
                } else {
                    clusters = Integer.parseInt(clustersField.textProperty().get());
                }
                if (intervalsField.textProperty().get().equals("") || Integer.parseInt(intervalsField.textProperty().get()) <= 0) {
                    intervals = 5;
                } else {
                    intervals = Integer.parseInt(intervalsField.textProperty().get());
                }
                if (iterationsField.textProperty().get().equals("") || Integer.parseInt(iterationsField.textProperty().get()) <= 0) {
                    iterations = 1000;
                } else {
                    iterations = Integer.parseInt(iterationsField.textProperty().get());
                }

                boolean continuousState = runOption.isSelected();
                currentSettings.setIntervals(intervals);
                currentSettings.setIterations(iterations);
                currentSettings.setLabels(clusters);
                currentSettings.setContinuousState(continuousState);
                if (cachedSettings.size() < configButtons.size()) {
                    for (int i = 0; i < configButtons.size(); i++) {
                        cachedSettings.add(null);
                    }
                }
                cachedSettings.set(configButtons.indexOf((Button) b), currentSettings);
                if (algorithmValue != null) {
                    runAlgorithm.setDisable(false);
                }
            });
        }

        for (Object group : getToggleGroups()) {
            ((ToggleGroup) group).selectedToggleProperty().addListener(listener -> {
                toggleGroup = ((ToggleGroup) group);
                algorithmValue = ((ToggleGroup) group).selectedToggleProperty().getValue();
                if (algorithmValue != null && currentSettings != null) {
                    runAlgorithm.setDisable(false);
                }
            });
        }
//        while (algorithmConfigWindow != null) {
//            algorithmConfigWindow.setOnCloseRequest(value -> {
//
//            });
//        }

        runAlgorithm.setOnAction(e -> {
            lineChart.getData().clear();
            scatterChart.getData().clear();
            ((AppData) applicationTemplate.getDataComponent()).displayData();
            if (((RadioButton) (toggleGroup).getSelectedToggle()).getId().equals("Random Classification")) {
//                            runAlgorithm.setDisable(true);
                ((AppData) applicationTemplate.getDataComponent()).getProcessor().runClassificationAlgorithm(currentSettings, this.getLineChart());
//                            runAlgorithm.setDisable(false);
            }
            if (((RadioButton) (toggleGroup).getSelectedToggle()).getId().equals("Random Clustering")) {
                System.out.println("do nothing");
                //get settings
                //run randomclustering algorithm
            }
        });

    }

    public void setFilePath(String path) {
        dataSource = path;
    }

    public void setFileMetaData() {
        numLabels = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getNumLabels();
        labels = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getLabels();
        fileInfo.setText(applicationTemplate.manager.getPropertyValue(INSTANCE_COUNT_INFO.name()) + ((AppData) applicationTemplate.getDataComponent()).getProcessor().getNumInstances() + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(LABEL_COUNT_INFO.name()) + numLabels + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(LABEL_NAME_INFO.name()) + NEW_LINE + TAB + "• " + String.join(NEW_LINE + TAB + "• ", labels) + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(SOURCE_INFO.name()) + dataSource);
    }

    public boolean processData() {
        try {
            clear();
//            String currentText = textArea.getText();
//            ArrayList<String> newDataEntries = new ArrayList<>(Arrays.asList(currentText.split(NEW_LINE)));
//            ArrayList<String> fileDataEntries = ((AppData) applicationTemplate.getDataComponent()).getFileData();
//
//            if (fileDataEntries != null) {
//                for (String j : fileDataEntries) {
//                    if (!newDataEntries.contains(j)) {
//                        newDataEntries.add(j);
//                    }
//                }
//            }
//
//            if (((AppActions) applicationTemplate.getActionComponent()).getDataPath() != null) {
//                storedData = String.join(NEW_LINE, newDataEntries);
//            } else {
//                storedData = textArea.getText();
//            }
//
//            if (dataSource == null) {
//                dataSource = "";
//            }

            ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());

            setFileMetaData();
            return true;
        } catch (InvalidDataNameException | ArrayIndexOutOfBoundsException error) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), error.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
            textArea.setDisable(!textArea.disableProperty().get());
            toggleOff();
            return false;
        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DATA_DISPLAY_FAIL_TITLE.name()), ex.getMessage());
//                    textArea.setDisable(!textArea.disableProperty().get());
            toggleOff();
            return false;
        }
    }

    private void initializeAlgorithmTypes() {
        algorithmTypes = new ComboBox();
        algorithmTypes.getItems().addAll(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()), applicationTemplate.manager.getPropertyValue(CLUSTERING.name()));

        algorithmTypes.setCellFactory(l -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item);

                        if (item.equals(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()))) {
                            SimpleIntegerProperty labels = new SimpleIntegerProperty(numLabels);
                            disableProperty().bind(new BooleanBinding() {
                                {
                                    bind(labels);
                                }

                                @Override
                                protected boolean computeValue() {
                                    return labels.get() != 2;
                                }
                            });
                        }
                    } else {
                        setText(null);
                    }
                }
            };

            return cell;
        });

        algorithmTypes.getSelectionModel().selectedItemProperty().addListener(listener -> {
            if (algorithmTypes.getSelectionModel().getSelectedItem() == null) {
                return;
            }
            if (algorithmTypes.getSelectionModel().getSelectedItem().equals(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()))) {
                algorithmList.getChildren().add(classificationTypes);
            }
            if (algorithmTypes.getSelectionModel().getSelectedItem().equals(applicationTemplate.manager.getPropertyValue(CLUSTERING.name()))) {
                algorithmList.getChildren().add(clusteringTypes);
            }
        });

        algorithmTypes.setOnAction(listener -> {
            algorithmPane.getChildren().remove(algorithmTable);
        });

    }

    private void toggleOff() {
        toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));
        algorithmTable.getChildren().remove(algorithmTypes);

        vPane.getChildren().remove(algorithmPane);
    }

    private void resetToggleOptions() {
        for (Object group : getToggleGroups()) {
            ((ToggleGroup) group).selectToggle(null);
        }
    }

    private ArrayList<ToggleGroup> getToggleGroups() {
        return algorithmGroups;
    }

    private void clearCachedSettings() {
        cachedSettings.clear();
    }

    public ScatterChart<Number, Number> getChart() {
        return scatterChart;
    }

    public StackPane getChartPane() {
        return charts;
    }

    public Button getRunButton() {
        return runAlgorithm;
    }
    
    public Button getScreenshotButton(){
        return scrnshotButton;
    }
    
    public NumberAxis getXAxis(){
        return xAxis;
    }
    
    public NumberAxis getYAxis(){
        return yAxis;
    }
    
}
