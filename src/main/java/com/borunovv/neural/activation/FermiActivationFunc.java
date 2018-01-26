/**
 * 
 */
package com.borunovv.neural.activation;

/**
 * @author borunovv
 * @date 27-01-2017
 */
public class FermiActivationFunc implements IActivationFunction {
    private double factor = 1.0;

    public FermiActivationFunc() {
    }

    public FermiActivationFunc(double factor) {
        this.factor = factor;
    }

    @Override
    public double calc(double x) {
        return 1.0 / (1.0 + Math.exp(-x / factor));
    }
}
