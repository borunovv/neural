/**
 * 
 */
package com.borunovv.neural.activation;

/**
 * @author borunovv
 * @date 27-01-2017
 */
public class HiperbolicTangentActivationFunc implements IActivationFunction {
    private double factor = 1.0;

    public HiperbolicTangentActivationFunc(double factor) {
        this.factor = factor;
    }

    @Override
    public double calc(double x) {
        return  Math.tanh(x / factor);
    }
}
