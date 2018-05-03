/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;


import static java.lang.Thread.sleep;
import algorithms.Clusterer;
import data.DataSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;
/**
 *
 * @author David
 */
public class RandomClusterer extends Clusterer{
    
    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    ApplicationTemplate applicationTemplate;

    private DataSet dataset;
//    private XYChart<Number, Number> chart;

    private final AtomicInteger counter;
    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean algorithmActiveState;
    private final AtomicBoolean tocontinue;
    
    public RandomClusterer(ConfigState settings, DataSet dataset, ApplicationTemplate ui){
        super(settings);
        this.dataset = dataset;
//        this.chart = chart;
        this.applicationTemplate = ui;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(0);
        this.algorithmActiveState = new AtomicBoolean(false);   
    }
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

    public int getIterationCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.addAndGet(updateInterval);
    }

    
    public boolean isAlgorithmActive() {
        return algorithmActiveState.get();
    }
    
    
    @Override
    public void run(){
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
        algorithmActiveState.set(true);
        if (tocontinue()) {
            for (int i = 1; i <= maxIterations; i++) {
                
                
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            
        
        
        } else {
            for (int i = 0; i <= updateInterval; i++) {
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
            
    
}
