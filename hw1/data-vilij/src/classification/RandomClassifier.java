package classification;

import algorithms.Classifier;
import data.DataSet;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import static settings.AppPropertyTypes.CSS_PATH;
import ui.AppUI;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    ApplicationTemplate applicationTemplate;

    private DataSet dataset;
    private XYChart<Number, Number> chart;

    private final AtomicInteger counter;
    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private List<Integer> output;
    private XYChart.Series<Number, Number> algorithmLine;
    private AtomicBoolean algorithmActiveState;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset, XYChart<Number, Number> chart, ApplicationTemplate ui, ConfigState settings) {
        this.dataset = dataset;
        this.chart = chart;
        this.applicationTemplate = ui;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(1);
        this.algorithmActiveState = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        algorithmActiveState.set(true);
        if (tocontinue()) {
            for (int i = 1; i <= maxIterations && tocontinue(); i++) {
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                if (yCoefficient == 0) {
                    yCoefficient = 1;
                }
                int constant = new Double(RAND.nextDouble() * 100).intValue();
                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
//                    System.out.println(output);
                if (i % updateInterval == 0) {
                    System.out.printf("Iteration number %d: ", i); //
                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("disabled");
                        calculateLineOutput(chart);
                    });

                }
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    System.out.printf("Iteration number %d: ", i);
                    flush();

                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                    });
                    algorithmActiveState.set(false);
                    break;
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            for (int i = 1; i <=  updateInterval; i++) {
                counter.getAndIncrement();
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                if (yCoefficient == 0) {
                    yCoefficient = 1;
                }
                int constant = new Double(RAND.nextDouble() * 100).intValue();

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
//                    System.out.println(output);
                if (counter.get() % updateInterval == 0) {
                    System.out.printf("Iteration number %d: ", counter.get()); 
                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("busy");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Resume");
                        calculateLineOutput(chart);
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                    });

//                        //update gui then set to true again
                }
                if (counter.get() > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    System.out.printf("Iteration number %d: ", counter.get());
                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Run");
                        calculateLineOutput(chart);
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                    });
                    algorithmActiveState.set(false);
                    counter.set(1);
                    break;
                }
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);

    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));

    }

    public int getIterationCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.addAndGet(updateInterval);
    }

    private void calculateLineOutput(XYChart<Number, Number> chart) {
        if (algorithmLine != null) {
            chart.getData().remove(algorithmLine);
        }
        algorithmLine = new XYChart.Series<>();
        algorithmLine.setName("Classification Line");

        int x1 = (int) dataset.getLocations().values().stream().mapToDouble(Point2D::getY).min().orElse(0.0);
        int x2 = (int) dataset.getLocations().values().stream().mapToDouble(Point2D::getY).max().orElse(0.0);
        if (output == null) {
            return;
        }
        int y1 = (-output.get(2) - output.get(0) * x1) / output.get(1);
        int y2 = (-output.get(2) - output.get(0) * x2) / output.get(1);

        algorithmLine.getData().add(new XYChart.Data<>(x1, y1));
        algorithmLine.getData().add(new XYChart.Data<>(x2, y2));
        chart.getData().add(algorithmLine);

        algorithmLine.getNode().setId("averageY");
//        for(XYChart.Data<Number,Number> x : algorithmLine.getData()){
//            x.getNode().setVisible(false);
//        }
    }
    
    public boolean isAlgorithmActive(){
        return algorithmActiveState.get();
    }
}
