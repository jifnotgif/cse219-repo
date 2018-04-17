package classification;

import algorithms.Classifier;
import data.DataSet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import ui.ConfigState;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    
    private final AtomicInteger counter;
    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private List<Integer> output;

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

    public RandomClassifier(DataSet dataset, ConfigState settings) {
        this.dataset = dataset;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(1);
    }

    @Override
    public void run() {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
            if(tocontinue()){
                for (int i = counter.get(); i <= maxIterations && tocontinue(); i++) {
                    counter.getAndIncrement();
                    int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                    int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                    if(yCoefficient == 0) yCoefficient = 1;
                    int constant = new Double(RAND.nextDouble() * 100).intValue();

                    // this is the real output of the classifier
                    output = Arrays.asList(xCoefficient, yCoefficient, constant);
                    
                    // everything below is just for internal viewing of how the output is changing
                    // in the final project, such changes will be dynamically visible in the UI
                    System.out.println(output);
                    if (i % updateInterval == 0) {
                        System.out.printf("Iteration number %d: ", i); //
                        flush();
                        
//                        //update gui then set to true again
                        
                    }
                    if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                        System.out.printf("Iteration number %d: ", i);
                        flush();
                        break;
                    }
                }
            }
            else{
            for (int i = counter.get(); i <= maxIterations/updateInterval; i++) {
                    counter.getAndIncrement();
                    int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                    int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                    if(yCoefficient == 0) yCoefficient = 1;
                    int constant = new Double(RAND.nextDouble() * 100).intValue();

                    // this is the real output of the classifier
                    output = Arrays.asList(xCoefficient, yCoefficient, constant);
                    
                    // everything below is just for internal viewing of how the output is changing
                    // in the final project, such changes will be dynamically visible in the UI
                    System.out.println(output);
                    if (i % updateInterval == 0) {
                        System.out.printf("Iteration number %d: ", i); //
                        flush();
                        break;
                        
//                        //update gui then set to true again
                        
                    }
                    if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                        System.out.printf("Iteration number %d: ", i);
                        flush();
                        break;
                    }
                }
            }
               
//            }
//        });

    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
        
    }
    
    public List<Integer> getOutput(){
        return output;
    }
    
    public int getIterationCounter(){
        return counter.get();
    }
    
    public void incrementCounter(){
        counter.addAndGet(updateInterval);
    }

}
