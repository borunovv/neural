package com.borunovv.titanic;

import com.borunovv.common.CSVUtils;
import com.borunovv.common.ResourceUtils;
import com.borunovv.ga.GASolver;
import com.borunovv.ga.IFitnessFunction;
import com.borunovv.ga.crossover.OnePointCrossover;
import com.borunovv.ga.mutation.SimpleMutation;
import com.borunovv.neural.Network;
import com.borunovv.neural.activation.FermiActivationFunc;
import com.borunovv.neural.activation.ThresholdActivationFunc;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author borunovv
 */
public class TitanicSolver {

    private static final int TRAIN_SET_PERCENT = 90;


    public void solve(String trainCsvResource, String testCsvResource, String outputCsvFile) throws IOException {
        try (Reader trainReader = ResourceUtils.getFile(trainCsvResource)) {
            try (Reader testReader = ResourceUtils.getFile(testCsvResource)) {
                try (Writer outputWriter = new FileWriter(outputCsvFile)) {
                    solve(trainReader, testReader, outputWriter);
                }
            }
        }
    }

    public void solve(Reader trainCsv, Reader testCsv, Writer outputCsv) throws IOException {
        List<Map<String, String>> maps = readCSV(trainCsv);
        //printDistinctValues(maps);

        List<Sample> allTrainSamples = readSamples(maps);
        saveToCSV(allTrainSamples, "train_vec.csv");

        int trainSetSize = allTrainSamples.size() * TRAIN_SET_PERCENT / 100;

        // Перемешаем обучающую выборку, чтобы потом уже разделить на обучающую+контрольную в случайном порядке.
        Collections.shuffle(allTrainSamples, new Random(System.currentTimeMillis()));

        List<Sample> trainSet = allTrainSamples.subList(0, trainSetSize);
        List<Sample> validationSet = allTrainSamples.subList(trainSetSize, allTrainSamples.size());
        List<Sample> testSet = readSamples(testCsv);
        saveToCSV(testSet, "test_vec.csv");

        final int inputSize = trainSet.get(0).getInputSize();

        // Настроим нейросеть
        Network net = new Network(inputSize)
                .addLayer(inputSize, new ThresholdActivationFunc()) // Скрытый слой
                //.addLayer(10, new ThresholdActivationFunc()) // Скрытый слой
                .addLayer(1, new FermiActivationFunc()); // Выходной слой (1 нейрон)

        // Настроим Генетический поиск.
        int paramsCount = net.getWeightsCount();
        System.out.println("Network total weights: " + paramsCount);
        GASolver solver = new GASolver()
                .withParamsCount(paramsCount)
                .withMutation(new SimpleMutation(0.5))
                .withCrossover(new OnePointCrossover())
                .withInitialPopulation(3000, 50)
                .withFitnessFunction(getFitnessFunction(net, trainSet))
                .init();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000; ++i) {
            solver.doIteration();
            net.setWeights(solver.getBest());
            double trainAccuracy = calcAccuracy(net, trainSet);
            double validationAccuracy = calcAccuracy(net, validationSet);

            System.out.println(String.format("Iteration #%d"
                            + ", accuracy on train set: %.2f%%"
                            + ", accuracy on validation set: %.2f%%",
                    i, trainAccuracy, validationAccuracy));
        }

        long delta = System.currentTimeMillis() - start;
        System.out.println("Train time: " + delta + " ms");

        solveTestSetAndSaveToFile(outputCsv, testSet, net);
    }

    private void saveToCSV(List<Sample> samples, String fileName) {
        try (Writer outputWriter = new BufferedWriter(new FileWriter(fileName))) {
            for (Sample sample : samples) {
                outputWriter.write("" + sample.id);
                for (int i = 0; i < sample.input.data.length; ++i) {
                    outputWriter.write(", ");
                    outputWriter.write(String.format("%.5f", sample.input.data[i]).replace(',', '.'));
                }
                for (int i = 0; i < sample.output.data.length; ++i) {
                    outputWriter.write(", ");
                    outputWriter.write(String.format("%.5f", sample.output.data[i]).replace(',', '.'));
                }

                outputWriter.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void solveTestSetAndSaveToFile(Writer outputCsv, List<Sample> testSet, Network net) throws IOException {
        outputCsv.write("PassengerId,Survived\n");
        for (Sample sample : testSet) {
            double fromNet = net.solve(sample.input).data[0];
            fromNet = fromNet < 0.5 ? 0 : 1.0;
            int survived = (int) fromNet;
            outputCsv.write(sample.id + "," + survived + "\n");
        }
    }

    private double calcAccuracy(Network net, List<Sample> samples) {
        int correctAnswers = 0;
        for (Sample sample : samples) {
            double fromNet = net.solve(sample.input).data[0];
            fromNet = fromNet < 0.5 ? 0 : 1.0;
            double expected = sample.output.data[0];

            int fromNetInt = (int) fromNet;
            int expectedInt = (int) expected;

            if (fromNetInt == expectedInt) {
                correctAnswers++;
            }
        }

        return 100.0 * correctAnswers / samples.size();
    }

    private IFitnessFunction getFitnessFunction(final Network n,
                                                final List<Sample> trainSet) {
        return new IFitnessFunction() {
            @Override
            public double calculate(double[] newWeights) {
                n.setWeights(newWeights);

                double errors = 0.0;
                for (Sample sample : trainSet) {
                    double fromNet = n.solve(sample.input).data[0];
                    double expected = sample.output.data[0];
                    double delta = (expected - fromNet);
                    errors += (delta * delta);
                }

                return 0 - errors;
            }
        };
    }

    private List<Map<String, String>> readCSV(Reader reader) throws IOException {
        return CSVUtils.parseToMaps(reader);
    }

    private List<Sample> readSamples(List<Map<String, String>> maps) {
        return maps.stream()
                .map(this::toSample)
                .collect(Collectors.toList());
    }

    private List<Sample> readSamples(Reader reader) throws IOException {
        return CSVUtils.parseToMaps(reader).stream()
                .map(this::toSample)
                .collect(Collectors.toList());
    }

    private Set<String> distinctValues(List<Map<String, String>> maps, String key) {
        Set<String> res = new HashSet<>();
        for (Map<String, String> map : maps) {
            if (map.containsKey(key)) {
                res.add(map.get(key));
            }
        }
        return res;
    }

    private Map<String, Set<String>> allDistinctValues(List<Map<String, String>> maps) {
        Map<String, Set<String>> result = new HashMap<>();
        Set<String> keys = maps.get(0).keySet();
        for (String key : keys) {
            result.put(key, distinctValues(maps, key));
        }
        return result;
    }

    private void printDistinctValues(List<Map<String, String>> maps) {
        Map<String, Set<String>> distinct = allDistinctValues(maps);
        for (String key : distinct.keySet()) {
            System.out.println("Values for key '" + key + "':");
            Set<String> values = distinct.get(key);
            for (String value : values) {
                System.out.println("  [" + value + "]");
            }
        }
    }


    // PassengerId,Survived,Pclass,Name,Sex,Age,SibSp,Parch,Ticket,Fare,Cabin,Embarked
    // 1,0,3,"Braund, Mr. Owen Harris",male,22,1,0,A/5 21171,7.25,,S
    // 32,1,1,"Spencer, Mrs. William Augustus (Marie Eugenie)",female,,1,0,PC 17569,146.5208,B78,C
    private Sample toSample(Map<String, String> item) {
        int id = toInt(item.get("PassengerId"));

        int pClass = toInt(item.get("Pclass")); // 1,2,3
        String name = item.get("Name");
        int sex = item.get("Sex").equalsIgnoreCase("male") ? 1 : 2;// 1 - male, 2 - female
        double age = toDouble(item.get("Age")) / 100.0; // normalize
        boolean ageNotSet = age == 0.0;
        int sibSp = toInt(item.get("SibSp")); //0-8
        int parch = toInt(item.get("Parch")); // 0-6
        String ticket = item.get("Ticket");
        boolean ticketIsNumber = isNumber(ticket);
        double fare = toDouble(item.get("Fare")) / 513.0; // norm
        String cabin = item.get("Cabin");
        boolean cabinPresent = !cabin.isEmpty();
        char cabinFirstLetter = cabinPresent ?
                cabin.charAt(0) :
                0;
        int embarked = item.get("Embarked").equalsIgnoreCase("Q") ? 1 :
                item.get("Embarked").equalsIgnoreCase("S") ? 2 :
                        item.get("Embarked").equalsIgnoreCase("C") ? 3 :
                                0;

        Sample sample = new Sample(id, 40, 1);
        sample.input.data[0] = bool2Double(pClass == 1);
        sample.input.data[1] = bool2Double(pClass == 2);
        sample.input.data[2] = bool2Double(pClass == 3);

        sample.input.data[3] = bool2Double(sex == 1);
        sample.input.data[4] = bool2Double(sex == 2);

        sample.input.data[5] = clamp(age);

        // 0-8
        sample.input.data[6] = bool2Double(sibSp == 0);
        sample.input.data[7] = bool2Double(sibSp == 1);
        sample.input.data[8] = bool2Double(sibSp == 2);
        sample.input.data[9] = bool2Double(sibSp == 3);
        sample.input.data[10] = bool2Double(sibSp == 4);
        sample.input.data[11] = bool2Double(sibSp == 5);
        sample.input.data[12] = bool2Double(sibSp == 6);
        sample.input.data[13] = bool2Double(sibSp == 7);
        sample.input.data[14] = bool2Double(sibSp == 8);
        sample.input.data[15] = bool2Double(sibSp > 8);

        // 0-6
        sample.input.data[16] = bool2Double(parch == 0);
        sample.input.data[17] = bool2Double(parch == 1);
        sample.input.data[18] = bool2Double(parch == 2);
        sample.input.data[19] = bool2Double(parch == 3);
        sample.input.data[20] = bool2Double(parch == 4);
        sample.input.data[21] = bool2Double(parch == 5);
        sample.input.data[22] = bool2Double(parch == 6);
        sample.input.data[23] = bool2Double(parch > 6);

        sample.input.data[24] = clamp(fare);

        sample.input.data[25] = bool2Double(cabinFirstLetter == 0);
        sample.input.data[26] = bool2Double(cabinFirstLetter == 'A');
        sample.input.data[27] = bool2Double(cabinFirstLetter == 'B');
        sample.input.data[28] = bool2Double(cabinFirstLetter == 'C');
        sample.input.data[29] = bool2Double(cabinFirstLetter == 'D');
        sample.input.data[30] = bool2Double(cabinFirstLetter == 'E');
        sample.input.data[31] = bool2Double(cabinFirstLetter == 'F');
        sample.input.data[32] = bool2Double(cabinFirstLetter == 'G');
        sample.input.data[33] = bool2Double(cabinFirstLetter == 'T');

        sample.input.data[34] = bool2Double(embarked == 0);
        sample.input.data[35] = bool2Double(embarked == 1);
        sample.input.data[36] = bool2Double(embarked == 2);
        sample.input.data[37] = bool2Double(embarked == 3);

        sample.input.data[38] = bool2Double(ticketIsNumber);
        sample.input.data[39] = bool2Double(ageNotSet);


//        sample.input.data[38] = clamp(age * age);
//        sample.input.data[39] = clamp(sibSp * sibSp);
//        sample.input.data[40] = clamp(parch * parch);
//        sample.input.data[41] = clamp(fare * fare);


        sample.output.data[0] = item.containsKey("Survived") ?
                toInt(item.get("Survived")) :
                0;

        return sample;
    }

    private boolean isNumber(String ticket) {
        try {
            Integer.parseInt(ticket);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    private double clamp(double v) {
        return v < 0.0 ?
                0.0 :
                v > 1.0 ?
                        1.0 :
                        v;
    }

    private double bool2Double(boolean val) {
        return val ? 1.0 : 0.0;
    }

    private int toInt(String val) {
        return val == null || val.isEmpty() ?
                0 :
                Integer.parseInt(val);
    }

    private double toDouble(String val) {
        return val == null || val.isEmpty() ?
                0 :
                Double.parseDouble(val);
    }
}

