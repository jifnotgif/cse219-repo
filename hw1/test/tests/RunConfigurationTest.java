/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author David
 */
public class RunConfigurationTest {

    private ConfigState settings;
    private Integer clusters;
    private Integer updateInterval;
    private Integer maxIterations;
    private boolean iscontinuous;
    
    private int[] listofclusters;
    private int[] listofintervals;
    private int[] listofiterations;
    
    public RunConfigurationTest() {
    }
    
    @Before
    public void setUpClass() {
        settings = new ConfigState();
        
        listofclusters = new int[] {-1, 0, 2, 3, 4, 5};
        listofintervals = new int[] {-1, 0, 1, 5, 100, 200};
        listofiterations = new int[] {-1, 0, 100, 5000, 5001};
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void setRunConfigValues_pass(){
        setConfigValues(3,5,100, true);
        Assert.assertEquals(new ConfigState(null, listofiterations[2],listofintervals[3] , listofclusters[3], new AtomicBoolean(true)), settings);
        setConfigValues(3,100,5000, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[3],listofintervals[4] , listofclusters[3], new AtomicBoolean(false)), settings);   
    }
    
    @Test
    public void setRunConfigValues_modified(){
        setConfigValues(-1,200,100, true);
        Assert.assertEquals(new ConfigState(null, listofiterations[2],listofintervals[2] , listofclusters[2], new AtomicBoolean(true)), settings);
        setConfigValues(4,1,100, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[2], listofintervals[2], listofclusters[4], new AtomicBoolean(false)), settings);
        setConfigValues(2, 0, 0, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[2], listofintervals[2], listofclusters[2], new AtomicBoolean(false)), settings);      
    }
    

    private void setConfigValues(int clustersInput, int updateIntervalInput, int maxIterInput, boolean iscontinuous){
        if(clustersInput < 2) clusters = 2;
        else if(clustersInput > 4) clusters = 4;
        else clusters = clustersInput;
        settings.setLabels(clusters);
        
        if(maxIterInput <= 0 || maxIterInput > 5000) maxIterations = 100;
        else maxIterations = maxIterInput;
        settings.setIterations(maxIterations);
        
        if(updateIntervalInput <=0 || updateIntervalInput > maxIterations) updateInterval = 1;
        else updateInterval = updateIntervalInput;
        settings.setIntervals(updateInterval);
        
        this.iscontinuous = iscontinuous;
        settings.setContinuousState(iscontinuous);
    }
}
