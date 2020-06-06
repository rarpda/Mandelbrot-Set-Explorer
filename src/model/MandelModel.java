package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * This class implements the model for the Model-Delegate architecture.
 * @author rarpda
 */
public class MandelModel {

    /*Resolution*/
    private final double xResolution;
    private final double yResolution;
    /**
     * the change support object to help us fire change events at observers
     */
    private PropertyChangeSupport notifier;

    /**
     * the instance of the singleton MandelModel class
     */
    static private MandelModel instance = null;
    final static public String DATA_READY_EVENT = "dataReady";
    MandelbrotStruct currentStruct = new MandelbrotStruct();
    MandelbrotCalculator calculator = new MandelbrotCalculator();

    public MandelbrotStruct getCurrentStruct() {
        return currentStruct;
    }

    public void setCurrentStruct(MandelbrotStruct currentStruct) {

        this.currentStruct = currentStruct;
    }

    /**
     * Function to calculate the remapping for panning (going from x,y domain to real,imaginary)
     **/
    public void calculateMapping(double changeX, double changeY) {
        /*Check difference to know which way to increase*/
        double rangeReal = currentStruct.getMaxReal() - currentStruct.getMinReal();
        double rangeImag = currentStruct.getMaxImag() - currentStruct.getMinImag();
        double changeThisX = changeX * (rangeReal / xResolution);
        double changeThisY = changeY * (rangeImag / yResolution);

        /*Update values */
        currentStruct.setMinImag(currentStruct.getMinImag() + changeThisY);
        currentStruct.setMinReal(currentStruct.getMinReal() + changeThisX);
        currentStruct.setMaxImag(currentStruct.getMaxImag() + changeThisY);
        currentStruct.setMaxReal(currentStruct.getMaxReal() + changeThisX);
        generateMandelbrot(); /*Generate new model */
    }
    /**
     * Function used to calculate remapping for zooming (going from x,y domain to real,imaginary)
     **/
    public void calculateMapping(double newXValue, double newYValue, double squareLength) {
        /*Update map*/
        double rangeReal = currentStruct.getMaxReal() - currentStruct.getMinReal();
        double rangeImag = currentStruct.getMaxImag() - currentStruct.getMinImag();
        double newStartReal = newXValue * (rangeReal / xResolution) + currentStruct.getMinReal();
        double newEndReal = newStartReal + squareLength * rangeReal / xResolution;
        double newEndImag = currentStruct.getMaxImag() - newYValue * (rangeImag / yResolution);
        double newStartImag = newEndImag - squareLength * rangeImag / yResolution;

        /*Update values*/
        currentStruct.setMinImag(newStartImag);
        currentStruct.setMinReal(newStartReal);
        currentStruct.setMaxImag(newEndImag);
        currentStruct.setMaxReal(newEndReal);
        generateMandelbrot(); /*Generate new model*/
    }


    /**
     * Method to return an instance of the Singleton MandelModel.
     */
    public static MandelModel getInstance(int xResolution, int yResolution) {
        if (instance == null) {
            instance = new MandelModel(xResolution, yResolution);
        }
        return instance;
    }

    public void resetModel() {
        currentStruct = new MandelbrotStruct();
        generateMandelbrot();
    }
    /**
     * Function to calculate magnifcation scale.
     * @return the magnification scale.
     * */
    public int getMagnificationValue() {
        double rangeReal = currentStruct.getMaxReal() - currentStruct.getMinReal();
        double rangeImag = currentStruct.getMaxImag() - currentStruct.getMinImag();

        double startingRangeReal = MandelbrotCalculator.INITIAL_MAX_REAL - MandelbrotCalculator.INITIAL_MIN_REAL;
        double startingRangeImage = MandelbrotCalculator.INITIAL_MAX_IMAGINARY - MandelbrotCalculator.INITIAL_MIN_IMAGINARY;

        double currentSize = rangeReal * rangeImag; /*Size of */
        double startingSize = startingRangeReal * startingRangeImage;
        int magnificationScale = (int) ((startingSize / currentSize) * 100.0);
        return magnificationScale;
    }

    /**
     * Constructs a new MandelModel instance.
     * Initialises the StringBuffer and property change support.
     */
    private MandelModel(int xResolution, int yResolution) {
        this.xResolution = xResolution;
        this.yResolution = yResolution;
        notifier = new PropertyChangeSupport(this);
    }

    /*
    * Generate new model based on the inputs.
    * */
    public void generateMandelbrot() {
        int[][] newGrid = calculator.calcMandelbrotSet((int) xResolution, (int) yResolution, currentStruct.getMinReal(), currentStruct.getMaxReal(), currentStruct.getMinImag(), currentStruct.getMaxImag(), currentStruct.getMaxIterations(), currentStruct.getRadiusSquared());

        notifier.firePropertyChange(DATA_READY_EVENT, null, newGrid);
        System.out.println(currentStruct.getMinReal() + " " + currentStruct.getMaxReal());
        System.out.println(currentStruct.getMinImag() + " " + currentStruct.getMaxImag());
    }


    /**
     * Utility method to permit an observer to add themselves as an observer to the model's change support object.
     *
     * @param listener the listener to add
     */
    public void addObserver(PropertyChangeListener listener) {
        notifier.addPropertyChangeListener(listener);
    }

}
