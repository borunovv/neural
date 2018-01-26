/**
 * 
 */
package com.borunovv.neural.activation;

/**
 * @author borunovv
 * @date 27-01-2017
 */
public class FastHiperbolicTangentActivationFunc implements IActivationFunction {
    private double factor = 1.0;

    private static final int CACHE_SIZE = 10000;
    private static final double CACHE_STEP = 0.001;
    private static final double CACHE_STEP_INV = 1. / CACHE_STEP;

    private double cache[] = new double[CACHE_SIZE];

    public FastHiperbolicTangentActivationFunc(double factor) {
        this.factor = factor;
        initCache();
    }

    private void initCache() {
        for (int i = 0; i < CACHE_SIZE; ++i) {
            double x = i * CACHE_STEP;
            cache[i] = Math.tanh(x / factor);
        }
    }

    @Override
    public double calc(double x) {
        boolean negative = (x < 0.0);
        if (negative) x = 0.0 - x;

        double dIndex = x * CACHE_STEP_INV;
        int index1 = (int) dIndex;
        if (index1 >= CACHE_SIZE - 1) {
            return negative ? -1.0 : 1.0;
        }

        int index2 = index1 + 1;
        double percent = dIndex - index1;
        double result = cache[index1] + (cache[index2] - cache[index1]) * percent;
        return negative ? 0.0 - result : result;
    }
}
