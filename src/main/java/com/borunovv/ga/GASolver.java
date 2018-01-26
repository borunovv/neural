/**
 * 
 */
package com.borunovv.ga;

import com.borunovv.common.Assert;
import com.borunovv.ga.crossover.ICrossover;
import com.borunovv.ga.mutation.IMutation;

import java.util.*;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public class GASolver {

    private static Random RANDOM = new Random(System.currentTimeMillis());

    private static final double NORM_KOEFF = 1.0 / Integer.MAX_VALUE;
    private static final int UN_MUTATABLE_PERCENT = 5;

    private double[] readOnlyResult;

    private int paramsCount;

    private ICrossover crossover;
    private IMutation mutation;
    private IFitnessFunction fitnessFunction;
    private int populationSize;
    private int crossoverPercent;

    private ArrayList<Genome> genomes;
    private Genome best;

    public GASolver withParamsCount(int count) {
        this.paramsCount = count;
        return this;
    }

    public GASolver withFitnessFunction(IFitnessFunction func) {
        Assert.notNull(func, "Fitness function is null");
        this.fitnessFunction = func;
        return this;
    }

    public GASolver withCrossover(ICrossover crossover) {
        this.crossover = crossover;
        return this;
    }

    public GASolver withMutation(IMutation mutation) {
        this.mutation = mutation;
        return this;
    }

    public GASolver withInitialPopulation(int populationSize, int crossoverPercent) {
        Assert.isTrue(populationSize > 0, "bad populationSize: " + populationSize);
        Assert.isTrue(crossoverPercent > 0 && crossoverPercent <= 50, "bad crossoverPercent: " + crossoverPercent);
        this.populationSize = populationSize;
        this.crossoverPercent = crossoverPercent;
        return this;
    }

    public GASolver init() {
        Assert.notNull(fitnessFunction, "fitnessFunction is null");
        Assert.notNull(crossover, "crossover is null");
        Assert.notNull(mutation, "mutation is null");
        Assert.isTrue(populationSize > 0, "Bad populationSize size: " + populationSize);
        Assert.isTrue(paramsCount > 0, "No params count set");

        readOnlyResult = new double[paramsCount];
        genomes = new ArrayList<>();
        for (int i = 0; i < populationSize; ++i) {
            genomes.add(new Genome(paramsCount).randomize());
        }

        calcFitnessFunction(genomes);
        return this;
    }

    public void doIteration() {
        Assert.isTrue(!genomes.isEmpty(), "Call init() first");
        sort(genomes);
        determineBestGenome();
        doCrossover(genomes);
        doMutation(genomes);
    }

    private void determineBestGenome() {
        Genome curBest = genomes.get(0);
        if (best == null || (best.getTag() < curBest.getTag())) {
            best = curBest.copy();
        }
    }

    public double[] getBest() {
        return toResult(best);
    }

    private void doMutation(ArrayList<Genome> genomes) {
        int leaveUnMutatedCount = RANDOM.nextInt(genomes.size() * UN_MUTATABLE_PERCENT / 100) + 1;
        for (int i = leaveUnMutatedCount; i < genomes.size(); ++i) {
            Genome genome = genomes.get(i);
            if (mutation.mutate(genome)) {
                calcFitnessFunction(genome);
            }
        }
    }

    private void doCrossover(ArrayList<Genome> sortedGenomes) {
        int itemsToAdd = Math.max(1, crossoverPercent * populationSize / 100);
        itemsToAdd = Math.min(itemsToAdd, populationSize / 2);
        int maxIndex = sortedGenomes.size() - 1 - itemsToAdd;

        int nextChild = maxIndex + 1;
        for (int i = 0; i < itemsToAdd; ++i) {
            if (nextChild >= populationSize - 1) {
                break;
            }

            int firstIndex = getRandomWeightedIndex(maxIndex);
            int secondIndex = getRandomWeightedIndex(maxIndex);
            if (firstIndex == secondIndex) {
                continue;
            }
            Genome first = sortedGenomes.get(firstIndex);
            Genome second = sortedGenomes.get(secondIndex);
            Genome child1 = sortedGenomes.get(nextChild++);
            Genome child2 = sortedGenomes.get(nextChild++);
            crossover.merge(first, second, child1);
            crossover.merge(second, first, child2);
            calcFitnessFunction(child1);
            calcFitnessFunction(child2);
        }
    }

    private int getRandomWeightedIndex(int maxIndex) {
        int total = maxIndex * (maxIndex + 1); // sum 1+2+3+...+maxIndex
        int point = RANDOM.nextInt(total); // [0...total-1]

        int sum = 0;
        for (int i = 0; i <= maxIndex; ++i) {
            sum += i;
            if (sum >= point) {
                return maxIndex - i;
            }
        }
        return 0;
    }

    private void calcFitnessFunction(List<Genome> genomes) {
        for (Genome genome : genomes) {
            calcFitnessFunction(genome);
        }
    }

    private void calcFitnessFunction(Genome genome) {
        double[] result = toResult(genome);
        double value = fitnessFunction.calculate(result);
        genome.setTag(value);
    }

    private void sort(List<Genome> genomes) {
        Collections.sort(genomes, (o1, o2) -> Double.compare(o2.getTag(), o1.getTag()));
    }

    private double[] toResult(Genome genome) {
        int[] genes = genome.genes();

        for (int i = 0; i < paramsCount; ++i) {
            double value = genes[i];
            value *= NORM_KOEFF; // [-1..1]
            readOnlyResult[i] = value;
        }

        return readOnlyResult;
    }
}

