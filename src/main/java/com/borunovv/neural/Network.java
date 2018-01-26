package com.borunovv.neural;

import com.borunovv.common.Assert;
import com.borunovv.common.Vector;
import com.borunovv.neural.activation.IActivationFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author borunovv
 */
public class Network {

    private final List<Layer> layers = new ArrayList<>();
    private final int inputSize;

    public Network(int inputSize) {
        this.inputSize = inputSize;
    }

    public Network addLayer(int neurons, IActivationFunction activationFunction) {
        int inputsForNewLayer = layers.isEmpty() ?
                inputSize :
                layers.get(layers.size()- 1).getOutputSize();


        layers.add(new Layer (inputsForNewLayer, neurons, activationFunction));
        return this;
    }

    public Vector solve(Vector input) {
        Assert.isTrue(input.size == inputSize, "Bad input vector size");
        Assert.isTrue(!layers.isEmpty(), "Network not initialized: no layers added");

        Vector result = input;
        for (Layer layer : layers) {
            result = layer.apply(result);
        }
        return result;
    }

    public int getWeightsCount() {
        int count = 0;
        for (Layer layer : layers) {
            count += layer.getWeightsCount();
        }
        return count;
    }

    public void setWeights(double[] weights) {
        int offset = 0;
        for (Layer layer : layers) {
            layer.setWeights(weights, offset);
            offset += layer.getWeightsCount();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Network layers:\n");
        int layerIndex = 0;
        for (Layer layer : layers) {
            sb.append("Layer #").append(layerIndex).append("\n").append(layer).append("\n");
            layerIndex++;
        }
        return sb.toString();
    }
}
