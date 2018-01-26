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
import java.util.Random;

public class NetworkTest {

    @Test
    public void solve() throws Exception {
        // Обучающая выборка:
        List<Pair<Vector, Double>> trainSet = generate(1000);
        List<Pair<Vector, Double>> validationSet = generate(100);

        // Настроим сетку
        int inputCount = trainSet.get(0).getKey().size;
        Network net = new Network(inputCount)
                .addLayer(3, new ThresholdActivationFunc()) // Слой 1 (скрытый)
                .addLayer(1, new ThresholdActivationFunc()); // Слой 2 (выходной)


        // Настраиваем ГА (у него кол-во параметров == кол-ву весов в НС)
        GASolver solver = new GASolver()
                .withParamsCount(net.getWeightsCount()) // Все веса в диапазоне [-1..1]
                .withMutation(new SimpleMutation(0.5))
                .withCrossover(new OnePointCrossover())
                .withInitialPopulation(1000, 50)
                .withFitnessFunction(getFitnessFunction(net, trainSet))
                .init();

        for (int i = 1; i <= 100; ++i) {
            solver.doIteration();
            net.setWeights(solver.getBest());

            double accuracyByTrainSet = getAccuracy(net, trainSet);
            double accuracyByValidationSet = getAccuracy(net, validationSet);

            System.out.println(String.format("Iteration #%d"
                            + ",  Accuracy by train set: %.2f%%"
                            + ",  Accuracy by validation set: %.2f%%",
                    i, accuracyByTrainSet, accuracyByValidationSet));
        }

        // Выведем найденные веса для интереса.
        System.out.println("\n" + net);
    }

    private double getAccuracy(Network n, List<Pair<Vector, Double>> validationSet) {
        int errors = 0;
        for (Pair<Vector, Double> item : validationSet) {
            double fromNet = n.solve(item.getKey()).data[0];
            double expected = item.getValue();
            if (Math.abs(fromNet - expected) > 0.1) {
                errors++;
            }
        }
        double errorPercent = (((double) errors) * 100.0) / validationSet.size();
        return 100.0 - errorPercent;
    }

    private List<Pair<Vector, Double>> generate(int count) {
        List<Pair<Vector, Double>> trainSet = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < count; ++i) {
            double x = random.nextDouble() * 2.0 - 1.0; // -1..1
            double y = random.nextDouble() * 2.0 - 1.0; // -1..1
            double z = random.nextDouble() * 2.0 - 1.0; // -1..1
            double value = (x > 0 && y > 0 && z > 0) ? 1.0 : 0.0;
            trainSet.add(new Pair<>(Vector.of(x, y, z), value));
        }
        return trainSet;
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
}
