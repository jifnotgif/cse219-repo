/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.base;
import algorithms.base.Algorithm;
import data.DataSet;
import java.util.List;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 * An abstract class for classification algorithms. The output
 * for these algorithms is a straight line, as described in
 * Appendix C of the software requirements specification
 * (SRS). The {@link #output} is defined with extensibility
 * in mind.
 *
 * @author Ritwik Banerjee
 */
public abstract class Classifier implements Algorithm {

    /**
     * See Appendix C of the SRS. Defining the output as a
     * list instead of a triple allows for future extension
     * into polynomial curves instead of just straight lines.
     * See 3.4.4 of the SRS.
     */
    protected List<Integer> output;

    public Classifier(){}
    public Classifier(ConfigState s, DataSet d, ApplicationTemplate template){}
    public List<Integer> getOutput() { return output; }
    
}