package com.borunovv.ga;

import com.borunovv.ga.crossover.FastSmartCrossover;
import com.borunovv.ga.mutation.SimpleMutation;
import org.junit.Test;

import java.util.Arrays;

public class GASolverTest {

    @Test
    public void init() throws Exception {
        int paramsCount = 100;
        int iterations = 1000;

        GASolver solver = new GASolver()
                .withParamsCount(paramsCount)
                .withCrossover(new FastSmartCrossover())
                .withMutation(new SimpleMutation(0.01))
                .withFitnessFunction(this::fitness)
                .withInitialPopulation(1000, 50)
                .init();

        long start = System.currentTimeMillis();
        for (int i = 1; i <= iterations; ++i) {
            solver.doIteration();

            if (i % (iterations / 100) == 0) {
                double[] best = solver.getBest();
                System.out.println((i * 100 / iterations) + "%: best fitness: " + fitness(best));
            }
        }
        long delta = System.currentTimeMillis() - start;

        double[] best = solver.getBest();

        System.out.println("Time: " + delta + " ms");
        System.out.println("Best fitness: " + fitness(best));
        System.out.println("Best result: " + Arrays.toString(best));
    }

    private double fitness(double[] params) {
        double res = 0.0f;
        for (int i = 0; i < params.length; i++) {
            double value = params[i] * 2 - 1; // [-1..1]
            res += value * value;
        }
        return 0 - res;
    }
}