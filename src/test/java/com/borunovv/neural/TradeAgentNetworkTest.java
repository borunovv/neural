package com.borunovv.neural;

import com.borunovv.common.Assert;
import com.borunovv.common.CSVUtils;
import com.borunovv.common.Vector;
import com.borunovv.ga.GASolver;
import com.borunovv.ga.IFitnessFunction;
import com.borunovv.ga.crossover.OnePointCrossover;
import com.borunovv.ga.mutation.SimpleMutation;
import com.borunovv.neural.activation.FastHiperbolicTangentActivationFunc;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TradeAgentNetworkTest {

    private static final String CSV_FILE_BTC = "C:\\temp\\Cripto\\USDT_BTC 5-Minute.csv";
    private static final String CSV_FILE_ETH = "C:\\temp\\Cripto\\USDT_ETH 5-Minute.csv";
    private static final String CSV_FILE_DASH = "C:\\temp\\Cripto\\USDT_DASH 5-Minute.csv";

    private static final String CSV_FILE = CSV_FILE_DASH;

    private static final int GA_POPULATION = 1000;
    private static final int GA_ITERATIONS = 100;
    private static final int WINDOW_SIZE = 12 * 2; // у нас шаг сэмплов в файле 5 минут, значит окно = 12 * 2 * 5 = 120 мин = 2 часа

    private static final int TRAIN_TAIL_DAYS = 10; // Учимся на предыдущих 3 днях
    private static final double ORDER_FEE = 0.01;

    private static final String START_DATE = "2017-09-01";
    private static final String END_DATE = "2018-01-01";

    private Data DATA = null;

    @Test
    public void solve() throws Exception {
        loadData();

        //DATA = DATA.addMirror();

        final int DAYS_COUNT = DATA.daysCount() - TRAIN_TAIL_DAYS;

        System.out.println("Start processing.");
        System.out.println("START_DATE   : " + START_DATE);
        System.out.println("DAYS_COUNT   : " + DAYS_COUNT);
        System.out.println("GA_POPULATION: " + GA_POPULATION);
        System.out.println("GA_ITERATIONS: " + GA_ITERATIONS);
        System.out.println("WINDOW_SIZE  : " + WINDOW_SIZE + " (" + WINDOW_SIZE * 5 + " min)");
        System.out.println("TRAIN_TAIL   : " + TRAIN_TAIL_DAYS + " days");
        System.out.println();

        double totalProfitPercent = 1.0;
        for (int i = 0; i < DAYS_COUNT; i++) {
            int day = TRAIN_TAIL_DAYS + i;
//          double[] trainSet = generate(300);
//          double[] testSet = generate(100);
            double[] trainSet = DATA.getInterval(day - TRAIN_TAIL_DAYS, TRAIN_TAIL_DAYS);
            double[] testSet = DATA.getInterval(day, 1);
            double avgDayPrice = calcAvg(testSet);

            long startTime = System.currentTimeMillis();
            double dayProfitPercent = solveForOneDay(trainSet, testSet, WINDOW_SIZE);
            long deltaTime = System.currentTimeMillis() - startTime;

            totalProfitPercent *= (1.0 + dayProfitPercent);

            String msg = String.format("%d\t%.2f\t%.2f\t%.2f\t%d sec",
                    i,
                    avgDayPrice,
                    dayProfitPercent * 100,
                    (totalProfitPercent - 1.0) * 100,
                    (int) (deltaTime / 1000));
            System.out.println(msg);
        }
    }

    private void loadData() {
        String beginDate = toString(minusDay(date(START_DATE), TRAIN_TAIL_DAYS));
        System.out.println("Loading data");
        System.out.println("CSV_FILE       : " + CSV_FILE);
        System.out.println("DATA BEGIN_DATE: " + beginDate);
        System.out.println("DATA END_DATE  : " + END_DATE);

        this.DATA = loadData(CSV_FILE, beginDate, END_DATE);
    }

    private double solveForOneDay(double[] trainSet, double[] testSet, int windowSize) throws Exception {
        // Обучающая выборка:
        double initialBalance = 1000.0; // Начальный баланс баксов на счете.
        double minOrderPrice = 10;      // Не менее 10 баксов на сделку.

        // Настроим сетку
        Network net = new Network(windowSize)
                .addLayer(windowSize, new FastHiperbolicTangentActivationFunc(1.0)) // Слой 1 (скрытый)
                .addLayer(windowSize / 2, new FastHiperbolicTangentActivationFunc(1.0)) // Слой 1 (скрытый)
                .addLayer(windowSize / 4, new FastHiperbolicTangentActivationFunc(1.0)) // Слой 1 (скрытый)
                .addLayer(1, new FastHiperbolicTangentActivationFunc(1.0)); // Слой 2 (выходной). Важно, чтобы на выходе было [-1..1]

        // Настраиваем ГА (у него кол-во параметров == кол-ву весов в НС)
        GASolver solver = new GASolver()
                .withParamsCount(net.getWeightsCount()) // Все веса в диапазоне [-1..1]
                .withMutation(new SimpleMutation(0.5))
                .withCrossover(new OnePointCrossover())
                .withInitialPopulation(GA_POPULATION, 50)
                .withFitnessFunction(getFitnessFunction(net, trainSet, windowSize, initialBalance, minOrderPrice))
                .init();

        for (int i = 1; i <= GA_ITERATIONS; ++i) {
            solver.doIteration();
        }

        net.setWeights(solver.getBest());
        double trainProfit = calcProfit(net, trainSet, windowSize, initialBalance, minOrderPrice, false);
        //System.out.println(String.format("  Train set (week ago): best absolute profit: %.2f", trainProfit));

        double testProfit = calcProfit(net, testSet, windowSize, initialBalance, minOrderPrice, false);
        //System.out.println(String.format("  Test set (current day): best absolute profit: %.2f", testProfit));

        return testProfit / initialBalance;
    }

    private double calcAvg(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum / values.length;
    }


    private double[] generate(int count) {
        double[] priceData = new double[count];
        for (int i = 0; i < count; ++i) {
            double time = i * 0.1 * Math.PI;
            double x = (Math.sin(time) * 0.5 + 0.5) * 2000 + 15000; // 15000..17000
            priceData[i] = x;
        }
        return priceData;
    }

    private IFitnessFunction getFitnessFunction(final Network n,
                                                final double[] priceData,
                                                final int windowSize,
                                                final double initialBalance,
                                                final double minOrderPrice) {
        return new IFitnessFunction() {
            @Override
            public double calculate(double[] newWeights) {
                n.setWeights(newWeights);
                return calcProfit(n, priceData, windowSize, initialBalance, minOrderPrice, false);
            }
        };
    }
    private static Random random = new Random(System.currentTimeMillis());

    private double calcProfit(final Network n,
                              final double[] priceData,
                              final int windowSize,
                              final double initialBalance,
                              final double minOrderPrice,
                              final boolean verbose) {
        double balance = initialBalance;
        double coinAmount = 0;
        if (verbose) {
            for (int i = 0; i < windowSize - 1; ++i) {
                double curPrice = priceData[i];
                System.out.println(String.format("Time: %d, Price: %.2f WAIT WINDOW FILL", i, curPrice));
            }
        }
        Vector window = new Vector(windowSize);
        for (int i = 0; i <= priceData.length - windowSize; ++i) {
            System.arraycopy(priceData, i, window.data, 0, windowSize);

            double curPrice = window.data[windowSize - 1];
            double action = n.solve(window).data[0]; // random.nextDouble();
            double percent = Math.abs(action);
            if (verbose) {
                System.out.print(String.format("Time: %d, Price: %.2f", i + windowSize - 1, curPrice));
            }
            // Ответ сети интерпретируем так:
            // x < 0.1 - надо продавать,
            // x > 0.1 - надо покупать.
            // x in [-0.1 .. 0.1] - ничего не делаем.
            // abs(x) - процент продажи/покупки от кол-ва монет/бюджета.
            if (action < -0.1) {
                // Продаем
                if (coinAmount > 0) {
                    double coinsToSell = percent * coinAmount;
                    double profitBeforeFee = coinsToSell * curPrice;
                    double fee = ORDER_FEE * profitBeforeFee;
                    double profitAfterFee = profitBeforeFee - fee;
                    if (profitAfterFee >= minOrderPrice) {
                        coinAmount -= coinsToSell;
                        coinAmount = Math.max(0, coinAmount);
                        balance += profitAfterFee;
                        if (verbose) {
                            System.out.print(String.format(" SELL: %.2f%% of coins (%.3f coins). Profit: %.2f, Fee: %.2f, Balance: %.2f, Coins: %.3f",
                                    percent * 100, coinsToSell, profitAfterFee, fee, balance, coinAmount));
                        }
                    }
                }
            } else if (action > 0.1) {
                // Покупаем
                if (balance <= 0 && coinAmount <= 0) {
                    if (verbose) {
                        System.out.print(" We are bankrupt here");
                    }
                    break; // Все потратили - банкрот.
                }
                double balanceToSpendBeforeFee = percent * balance;
                double fee = ORDER_FEE * balanceToSpendBeforeFee;
                double balanceToSpendAfterFee = balanceToSpendBeforeFee + fee;
                if (balanceToSpendAfterFee >= minOrderPrice) {
                    double coinsBoughtCount = balanceToSpendBeforeFee / curPrice;
                    coinAmount += coinsBoughtCount;
                    balance -= balanceToSpendAfterFee;
                    balance = Math.max(0, balance);
                    if (verbose) {
                        System.out.print(String.format(" BUY: %.2f%% of balance (%.2f). Coins to buy: %.3f, Fee: %.2f, Balance: %.3f, Coins: %.3f",
                                percent * 100, balanceToSpendAfterFee, coinsBoughtCount, fee, balance, coinAmount));
                    }
                }
            }
            if (verbose) {
                System.out.println();
            }
        }

        if (coinAmount > 0) {
            double lastPrice = priceData[priceData.length - 1];

            double profitBeforeFee = coinAmount * lastPrice;
            double fee = ORDER_FEE * profitBeforeFee;
            double profitAfterFee = profitBeforeFee - fee;

            balance += profitAfterFee;

            if (verbose) {
                System.out.println(String.format("Final SELL: %.3f coins. Profit: %.2f, Fee: %.2f, Balance: %.2f",
                        coinAmount, coinAmount * lastPrice, fee, balance));
            }
        }

        return balance - initialBalance;
    }

    private Data loadData(String fileName, String dateFrom, String dateTo) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try (Reader reader = new FileReader(fileName)) {
            List<Map<String, String>> items = readCSV(reader);

            Date fromDate = format.parse(dateFrom);
            Date toDate = format.parse(dateTo);
            long unixTimeStampFrom = fromDate.getTime() / 1000;
            long unixTimeStampTo = toDate.getTime() / 1000;

            List<Double> values = new ArrayList<>();
            long prevTimeStamp = 0;
            long timeStampDeltaSeconds = 0;
            for (Map<String, String> item : items) {
                long timestamp = Long.parseLong(item.get("date"));
                if (timestamp >= unixTimeStampFrom && timestamp <= unixTimeStampTo) {
                    values.add(Double.parseDouble(item.get("weightedAverage")));
                }
                if (prevTimeStamp > 0) {
                    timeStampDeltaSeconds = timestamp - prevTimeStamp;
                }
                prevTimeStamp = timestamp;
            }
            double[] result = new double[values.size()];
            int i = 0;
            for (Double value : values) {
                result[i++] = value;
            }

            return new Data(unixTimeStampFrom, result, (int) (timeStampDeltaSeconds / 60));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, String>> readCSV(Reader reader) throws IOException {
        return CSVUtils.parseToMaps(reader);
    }

//    private double[] readCSV(String fileName, String dateFrom, String dateTo) {
//        initCache(fileName);
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            Date fromDate = format.parse(dateFrom);
//            Date toDate = format.parse(dateTo);
//            long unixTimeStampFrom = fromDate.getTime() / 1000;
//            long unixTimeStampTo = toDate.getTime() / 1000;
//
//            List<Map<String, String>> items = cache.get(fileName);
//            List<Double> values = new ArrayList<>();
//            for (Map<String, String> item : items) {
//                long timestamp = Long.parseLong(item.get("date"));
//                if (timestamp >= unixTimeStampFrom && timestamp <= unixTimeStampTo) {
//                    values.add(Double.parseDouble(item.get("weightedAverage")));
//                }
//            }
//            double[] result = new double[values.size()];
//            int i = 0;
//            for (Double value : values) {
//                result[i++] = value;
//            }
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private Date date(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String toString(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private Date plusDay(Date d, int count) {
        return new Date(d.getTime() + (long) count * 1000 * 60 * 60 * 24);
    }

    private Date minusDay(Date d, int count) {
        return new Date(d.getTime() - (long) count * 1000 * 60 * 60 * 24);
    }


    private static class Data {
        public final long startTimeStamp;
        public final double[] data;
        public final int stepMinutes;

        public Data(long startTimeStamp, double[] data, int stepMinutes) {
            this.startTimeStamp = startTimeStamp;
            this.data = data;
            this.stepMinutes = stepMinutes;
        }

        public int daysCount() {
            return data.length * stepMinutes / 60 / 24;
        }

        public int getDayStartIndex(int dayIndex) {
            return dayIndex * 24 * 60 / stepMinutes;
        }

        public double[] getInterval(int fromDay, int daysCount) {
            Assert.isTrue(daysCount > 0, "Bad days count: " + daysCount);

            int startIndex = getDayStartIndex(fromDay);
            int endIndex = getDayStartIndex(fromDay + daysCount);

            Assert.isTrue(startIndex < data.length && endIndex < data.length, "Not enough data");
            int resultSize = endIndex - startIndex;
            double[] result = new double[resultSize];
            System.arraycopy(data, startIndex, result, 0, resultSize);
            return result;
        }

        public Data addMirror() {
            double[] resultData = new double[data.length * 2];
            System.arraycopy(data, 0, resultData, 0, data.length);
            for (int i = 0; i < data.length; ++i) {
                int invIndex = data.length - i - 1;
                resultData[i + data.length] = data[invIndex];
            }
            return new Data(startTimeStamp, resultData, stepMinutes);
        }
    }
}
