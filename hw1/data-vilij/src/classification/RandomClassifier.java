package classification;

import algorithms.Classifier;
import data.DataSet;

import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
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
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int yMinBound;
    private int yMaxBound;

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
        this.counter = new AtomicInteger(0);
        this.algorithmActiveState = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
        algorithmActiveState.set(true);
        if (tocontinue()) {
            for (int i = 1; i <= maxIterations; i++) {
                counter.getAndIncrement();
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
//                    System.out.println(output);
//                    System.out.printf("Iteration number %d: ", i); //
                if (i % updateInterval == 0) {
//                    System.out.printf("Iteration number %d: ", i); //
//                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("disabled");
                        calculateLineOutput();

                    });
                }

                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
//                    System.out.printf("Iteration number %d: ", i);
//                    flush();

                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        calculateLineOutput();
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                    });
                    algorithmActiveState.set(false);
                    counter.set(0);
                    break;
                }
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            for (int i = 0; i <= updateInterval; i++) {
                counter.getAndIncrement();
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
//                    System.out.println(output);
                if (counter.get() % updateInterval == 0) {
//                    System.out.printf("Iteration number %d: ", counter.get());
//                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("busy");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Resume");
                        calculateLineOutput();
                    });
//                        //update gui then set to true again
                }
                if (counter.get() > maxIterations * .6 && RAND.nextDouble() < 0.05) {
//                    System.out.printf("Iteration number %d: ", counter.get());
//                    flush();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Run");
                        calculateLineOutput();
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

                    });
                    algorithmActiveState.set(false);
                    counter.set(0);
                    break;
                }

                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(false);
        
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

    private void calculateLineOutput() {
        if (algorithmLine != null) {
            chart.getData().remove(algorithmLine);
        }
        
//        ((AppUI)applicationTemplate.getUIComponent()).getYAxis().setTickUnit(10);
//        if (output == null) {
//            return;
//        }
        
        algorithmLine = new XYChart.Series<>();
        algorithmLine.setName("Classification Line");

        x1 = dataset.getMinX();
        x2 = dataset.getMaxX();
        yMinBound = dataset.getMinY() -5;
        yMaxBound = dataset.getMaxY()+ 5;
        
        y1 = (int) ((-output.get(2) - output.get(0) * x1) / output.get(1));
        y2 = (int) ((-output.get(2) - output.get(0) * x2) / output.get(1));
        
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setLowerBound(x1 - 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setUpperBound(x2 + 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setForceZeroInRange(false);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setAutoRanging(false);
//        double yLowerBound = dataset.getLocations().values().stream().mapToDouble(Point2D::getY).min().orElse(0.0);

        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setLowerBound(yMinBound);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setUpperBound(yMaxBound);
//        ((AppUI)applicationTemplate.getUIComponent()).getYAxis().setForceZeroInRange(false);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setAutoRanging(false);

        algorithmLine.getData().add(new XYChart.Data<>(x1, y1));
        algorithmLine.getData().add(new XYChart.Data<>(x2, y2));
        chart.getData().add(algorithmLine);

        algorithmLine.getNode().setId("algorithm");
        
//        for(XYChart.Data<Number,Number> x : algorithmLine.getData()){
//            x.getNode().setVisible(false);
//        }
    }

    public boolean isAlgorithmActive() {
        return algorithmActiveState.get();
    }
    
}

