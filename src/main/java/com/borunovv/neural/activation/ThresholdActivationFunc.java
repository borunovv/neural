/**
 * 
 */
package com.borunovv.neural.activation;

/**
 * @author borunovv
 * @date 27-01-2017
 */
public class ThresholdActivationFunc implements IActivationFunction {
    private double threshold = 0;


    public ThresholdActivationFunc() {
    }

    public ThresholdActivationFunc(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public double calc(double x) {
        return x < threshold ?
                0.0 :
                1.0;
    }
}
