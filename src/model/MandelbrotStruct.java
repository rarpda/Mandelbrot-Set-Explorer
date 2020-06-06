package model;

/**
 * Structure for all required variable inputs
 *
 * @author rarpda
 */
public class MandelbrotStruct {


    public MandelbrotStruct() {
    }

    /**
     * Function to copy values of two structures..
     */
    public MandelbrotStruct(MandelbrotStruct original) {

        this.minReal = original.minReal;
        this.maxReal = original.maxReal;
        this.minImaginary = original.minImaginary;
        this.maxImaginary = original.maxImaginary;
        this.maxIterations = original.maxIterations;
        this.radiusSquared = original.radiusSquared;
    }


    private double minReal = MandelbrotCalculator.INITIAL_MIN_REAL;
    private double maxReal = MandelbrotCalculator.INITIAL_MAX_REAL;
    private double minImaginary = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
    private double maxImaginary = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
    private int maxIterations = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;
    private double radiusSquared = MandelbrotCalculator.DEFAULT_RADIUS_SQUARED;

    /*Getters and Setters*/
    public double getMinReal() {
        return minReal;
    }

    public void setMinReal(double minReal) {
        this.minReal = minReal;
    }

    public double getMaxReal() {
        return maxReal;
    }

    public void setMaxReal(double maxReal) {
        this.maxReal = maxReal;
    }

    public double getMinImag() {
        return minImaginary;
    }

    public void setMinImag(double minImag) {
        this.minImaginary = minImag;
    }

    public double getMaxImag() {
        return maxImaginary;
    }

    public void setMaxImag(double maxImag) {
        this.maxImaginary = maxImag;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public double getRadiusSquared() {
        return radiusSquared;
    }

    public void setRadiusSquared(double radiusSquared) {
        this.radiusSquared = radiusSquared;
    }
}
