package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s" + NAME_ERROR_MSG , name));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private final String         NEW_LINE = "\n";
    private final String         NAME_SYMBOL ="@";
    private final String         TAB = "\t";
    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean containsDuplicates = new AtomicBoolean(false);
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        AtomicInteger lineCount    = new AtomicInteger(1);
        
        ArrayList<Integer> linesWithError = new ArrayList<>();
        ArrayList<String> duplicateNames = new ArrayList<>();
        
        StringBuilder errorMessage = new StringBuilder();
        
        Stream.of(tsdString.split(NEW_LINE))
              .map(line -> Arrays.asList(line.split(TAB)))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      if(duplicateNames.contains(name)){
                          linesWithError.add((Integer)lineCount.get());
                          errorMessage.setLength(0);
                          errorMessage.append(list.get(0));
                          containsDuplicates.set(true);
                      }
                      duplicateNames.add(name);
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      linesWithError.add((Integer)lineCount.get());
                      errorMessage.setLength(0);
                      // if all data points with error are wanted, reate a new arraylist of names and append to message instead of list.get(0)
                      errorMessage.append(list.get(0)).append("'.\nError on line(s): ").append(linesWithError.toString().substring(1,linesWithError.toString().length()-1)).append(NEW_LINE);
                      hadAnError.set(true);
                  }
                  lineCount.incrementAndGet();
              });
        if(containsDuplicates.get()){
            errorMessage.append("'.\nDuplicate found on line(s): ").append(linesWithError.toString().substring(1,linesWithError.toString().length()-1)).append(NEW_LINE);
            hadAnError.set(true);
            
        }
        if (errorMessage.length() > 0)
            throw new InvalidDataNameException(errorMessage.toString());
        
    }

    public boolean isChartEmpty(){
        return dataLabels.isEmpty();
    }
    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        AtomicInteger counter = new AtomicInteger(0);
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
            for(XYChart.Data<Number,Number> d : series.getData()){
                dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                    if(counter.get() < series.getData().size()) {
                        Tooltip.install(series.getData().get(counter.get()).getNode(), new Tooltip(entry.getKey()));
                        series.getData().get(counter.getAndIncrement()).getNode().setCursor(Cursor.HAND);
                    }
                });
            counter.set(0);
            }
        }
        calculateAverageLine(chart);
    }
    
    private void calculateAverageLine(XYChart<Number,Number> chart){
        XYChart.Series<Number,Number> averageLine = new XYChart.Series<>();
        averageLine.setName("Average Y-value");
        double y = dataPoints.values().stream().mapToDouble(Point2D::getY).reduce(0, (a,b) -> a+b)/dataPoints.size();
        double x1 = dataPoints.values().stream().mapToDouble(Point2D::getX).min().orElse(0.0);
        double x2 = dataPoints.values().stream().mapToDouble(Point2D::getX).max().orElse(0.0);
        if (x1 == x2) {
            x1 = -10.0;
            x2 = 10.0;
        }
        averageLine.getData().add(new XYChart.Data<>(x1,y));
        averageLine.getData().add(new XYChart.Data<>(x2,y));
        chart.getData().add(averageLine);
        averageLine.getNode().setId("averageY");
        for(XYChart.Data<Number,Number> x : averageLine.getData()){
            x.getNode().setVisible(false);
        }
    }
    
    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith(NAME_SYMBOL))
            throw new InvalidDataNameException(name);
        return name;
    }
    
}
