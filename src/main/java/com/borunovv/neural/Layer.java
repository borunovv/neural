package com.borunovv.neural;

import com.borunovv.common.Matrix;
import com.borunovv.common.Vector;
import com.borunovv.neural.activation.IActivationFunction;

import java.util.Random;

/**
 * @author borunovv
 */
public class Layer {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private final Matrix matrix;
    private Vector temp;
    private Vector tempWithBias;
    private final IActivationFunction activationFunction;

    public Layer(int inputs, int neurons, IActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
        this.matrix = new Matrix(inputs + 1, neurons); // +1 - for bias
        this.temp = new Vector(neurons);
        this.tempWithBias = new Vector(inputs + 1);
        randomize();
    }

    public int getOutputSize() {
        return matrix.columns;
    }

    private void randomize() {
        randomize(-1.0, 1.0);
    }

    private void randomize(double min, double max) {
        for (int i = 0; i < matrix.rows; ++i) {
            for (int j = 0; j < matrix.columns; ++j) {
                matrix.data[i][j] = RANDOM.nextDouble() * (max - min) + min; // [min..max)
            }
        }
    }

    public Vector apply(Vector inputs) {
        System.arraycopy(inputs.data, 0, tempWithBias.data, 1, inputs.size);
        tempWithBias.data[0] = 1.0; // bias
        tempWithBias.multiplyTo(matrix, temp);

        // Apply activation func
        for (int i = 0; i < temp.size; i++) {
             temp.data[i] = activationFunction.calc(temp.data[i]);
        }
        return temp;
    }

    public int getWeightsCount() {
        return matrix.rows * matrix.columns;
    }

    public void setWeights(double[] weights, int offset) {
        int index = 0;
        for (int i = 0; i < matrix.rows; ++i) {
            double[] row = matrix.data[i];
            for (int j = 0; j < matrix.columns; ++j) {
                row[j] = weights[offset + index];
                index++;
            }
        }
    }

    @Override
    public String toString() {
        return matrix.toString();
    }
}
