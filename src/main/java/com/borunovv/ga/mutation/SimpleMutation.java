/**
 * 
 */
package com.borunovv.ga.mutation;

import com.borunovv.ga.Genome;

import java.util.Random;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public class SimpleMutation implements IMutation {

    private static Random random = new Random(System.currentTimeMillis());

    private double probability;

    public SimpleMutation(double probability) {
        this.probability = probability;
    }

    @Override
    public boolean mutate(Genome genome) {
        boolean somethingChanged = false;
        int[] data = genome.genes();
        for (int i = 0; i < data.length; ++i) {
            if (random.nextDouble() < probability) {
                int bitIndex = random.nextInt(32);
                int mask = 1 << bitIndex;
                data[i] ^= mask; // Инверсия бита в позиции offsetInGene
                somethingChanged = true;
            }
        }
        return somethingChanged;
    }

    private boolean needMutate() {
        return random.nextDouble() < probability;
    }
}
