/**
 * 
 */
package com.borunovv.ga.crossover;

import com.borunovv.common.Assert;
import com.borunovv.ga.Genome;

import java.util.Random;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public class OnePointCrossover implements ICrossover {

    private static Random random = new Random(System.currentTimeMillis());
    private static int[] masks = new int[32];

    static {
        int mask = 0xFFFFFFFF;
        for (int i = 0; i < 32; ++i) {
            masks[i] = mask;
            mask = mask & (~(1 << i));
        }
    }

    @Override
    public void merge(Genome first, Genome second, Genome result) {
        int[] firstData = first.genes();
        int[] secondData = second.genes();
        int[] resultData = result.genes();

        Assert.isTrue(firstData.length == secondData.length, "Marshalled data lengths are different");

        int separationGeneIndex = random.nextInt(firstData.length);
        int offsetInGene = random.nextInt(33);
        if (offsetInGene == 32) {
            separationGeneIndex--;
            offsetInGene = 0;
        }

        System.arraycopy(firstData, 0, resultData, 0, firstData.length);

        if (separationGeneIndex < firstData.length - 1) {
            System.arraycopy(secondData, separationGeneIndex + 1,
                    resultData, separationGeneIndex + 1,
                    firstData.length - separationGeneIndex - 1);
        }

        if (offsetInGene > 0) {
            int firstValue = firstData[separationGeneIndex];
            int secondValue = secondData[separationGeneIndex];
            int mask = masks[offsetInGene];
            int mixedValue = (firstValue & mask) | (secondValue & ~mask);
            firstData[separationGeneIndex] = mixedValue;
        }
    }
}
