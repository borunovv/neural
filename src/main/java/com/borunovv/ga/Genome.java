package com.borunovv.ga;

import java.util.Arrays;
import java.util.Random;

/**
 * @author borunovv
 */
public class Genome {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private int[] genes;
    private double tag;


    public Genome(int[] genes) {
        this.genes = genes;
    }

    public Genome(int size) {
        this.genes = new int[size];
    }

    public Genome randomize() {
       for (int i = 0; i < genes.length; ++i) {
           genes[i] = RANDOM.nextInt();
       }
       return this;
    }

    public double getTag() {
        return tag;
    }
    public void setTag(double tag) {
        this.tag = tag;
    }

    public int[] genes() {
        return genes;
    }

    public Genome copy() {
        Genome result = new Genome(Arrays.copyOf(genes, genes.length));
        result.tag = tag;
        return result;
    }
}
