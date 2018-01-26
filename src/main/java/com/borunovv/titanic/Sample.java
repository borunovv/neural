package com.borunovv.titanic;

import com.borunovv.common.Vector;

/**
 * @author borunovv
 */
public class Sample {
    public final int id;
    public final Vector input;
    public final Vector output;

    public Sample(int id, int inputSize, int outputSize) {
        this.id = id;
        this.input = new Vector (inputSize);
        this.output = new Vector(outputSize);
    }

    public Sample(int id, double[] input, double[] output) {
        this.id = id;
        this.input = new Vector(input);
        this.output = new Vector(output);
    }

    public int getInputSize() {
        return input.size;
    }

    public int getOutputSize() {
        return output.size;
    }
}
