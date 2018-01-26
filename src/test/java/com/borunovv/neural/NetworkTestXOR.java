package com.borunovv.neural;

import com.borunovv.common.Vector;
import com.borunovv.ga.GASolver;
import com.borunovv.ga.IFitnessFunction;
import com.borunovv.ga.crossover.OnePointCrossover;
import com.borunovv.ga.mutation.SimpleMutation;
import com.borunovv.neural.activation.ThresholdActivationFunc;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class NetworkTestXOR {

    @Test
    public void solveXOR_GA() throws Exception {
        Network net = new Network(2)
                .addLayer(2, new ThresholdActivationFunc())  // Скрытый слой
                .addLayer(1, new ThresholdActivationFunc()); // Выходной слой

        // Обучающая выборка:
        // 0 XOR 0 = 0
        // 0 XOR 1 = 1
        // 1 XOR 0 = 1
        // 1 XOR 1 = 0
        List<Pair<Vector, Double>> trainSet = new ArrayList<>();
        trainSet.add(new Pair<>(Vector.of(0.0, 0.0), 0.0));
        trainSet.add(new Pair<>(Vector.of(0.0, 1.0), 1.0));
        trainSet.add(new Pair<>(Vector.of(1.0, 0.0), 1.0));
        trainSet.add(new Pair<>(Vector.of(1.0, 1.0), 0.0));

        // Настраиваем ГА (у него кол-во параметров == кол-ву весов в НС)
        GASolver solver = new GASolver()
                .withParamsCount(net.getWeightsCount())
                .withMutation(new SimpleMutation(0.3))
                .withCrossover(new OnePointCrossover())
                .withInitialPopulation(500, 50)
                .withFitnessFunction(getFitnessFunction(net, trainSet))
                .init();

        for (int i = 1; i <= 300; ++i) {
            solver.doIteration();
        }

        net.setWeights(solver.getBest());

        testXOR(net);

        // Выведем найденные веса для интереса.
        System.out.println("\n" + net);
    }

    private IFitnessFunction getFitnessFunction(final Network n,
                                                final List<Pair<Vector, Double>> trainSet) {
        return new IFitnessFunction() {
            @Override
            public double calculate(double[] newWeights) {
                n.setWeights(newWeights);

                double errors = 0.0;
                for (Pair<Vector, Double> train : trainSet) {
                    double fromNet = n.solve(train.getKey()).data[0];
                    double expected = train.getValue();
                    double delta = (expected - fromNet);
                    errors += (delta * delta);
                }

                return 0 - errors;
            }
        };
    }

    private void testXOR(Network n) {
        System.out.println("Network results for XOR function:");
        for (int i = 0; i <= 1; ++i) {
            for (int j = 0; j <= 1; ++j) {
                double result = n.solve(Vector.of(i, j)).data[0];
                System.out.println("(" + i + ", " + j + ") => " + result);
            }
        }
    }
}
