/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.scene.control.Button;

/**
 *
 * @author David
 */
public class ConfigState {
    
    private Button btn;
    private int iterations;
    private int intervals;
    private int labels;
    private boolean continuousState;
    
    public ConfigState(Button btn, int iterations, int intervals, int labels, boolean continuousState){
        this.btn = btn;
        this.iterations = iterations;
        this.intervals = intervals;
        this.labels = labels;
        this.continuousState = continuousState;
    }
    public ConfigState(Button btn, int iterations, int intervals, boolean continuousState){
        this.btn = btn;
        this.iterations = iterations;
        this.intervals = intervals;
        this.continuousState = continuousState;
        this.labels = 0;
    }
    public ConfigState(){
        this.btn = null;
        this.iterations = 0;
        this.intervals = 0;
        this.continuousState = false;
        this.labels = 0;
    }
    public Button getBtn() {
        return btn;
    }

    public void setBtn(Button btn) {
        this.btn = btn;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getIntervals() {
        return intervals;
    }

    public void setIntervals(int intervals) {
        this.intervals = intervals;
    }

    public int getLabels() {
        return labels;
    }

    public void setLabels(int labels) {
        this.labels = labels;
    }

    public boolean isContinuousState() {
        return continuousState;
    }

    public void setContinuousState(boolean continuousState) {
        this.continuousState = continuousState;
    }
    
    
}
