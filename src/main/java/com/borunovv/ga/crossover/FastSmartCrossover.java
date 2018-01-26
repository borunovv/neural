/**
 * 
 */
package com.borunovv.ga.crossover;

import com.borunovv.ga.Genome;

import java.util.Random;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public class FastSmartCrossover implements ICrossover {

    private static Random random = new Random(System.currentTimeMillis());
    private static int[] masks = new int[32];
    private double preferDominatingGenomePercent = 0.5;

    static {
        int mask = 0xFFFFFFFF;
        for (int i = 0; i < 32; ++i) {
            masks[i] = mask;
            mask = mask & (~(1 << i));
        }
    }

    public FastSmartCrossover() {
    }

    public FastSmartCrossover(double preferDominatingGenomePercent) {
        this.preferDominatingGenomePercent = preferDominatingGenomePercent;
    }

    @Override
    public void merge(Genome first, Genome second, Genome result) {
        int[] firstData = first.genes();
        int[] secondData = second.genes();
        int[] resultData = result.genes();

        // Доминирующая особь (более приспособленная)
        int[] masterData = first.getTag() > second.getTag() ?
                firstData :
                secondData;

        int[] slaveData = first.getTag() > second.getTag() ?
                secondData :
                firstData;

        for (int i = 0; i < firstData.length; ++i) {
            int bit = random.nextInt(32);
            int mask = masks[bit];

            if (random.nextDouble() > preferDominatingGenomePercent) {
                resultData[i] = (masterData[i] & mask) | (slaveData[i] & ~mask);
            } else {
                resultData[i] = masterData[i];
            }
        }
    }
}
