package com.borunovv.neural.activation;

import org.junit.Test;

public class HiperbolicTangentActivationFuncTest {

    @Test
    public void calc() throws Exception {
        double a = 0;

        HiperbolicTangentActivationFunc func = new HiperbolicTangentActivationFunc(1.0);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            a += func.calc(0.123456789 * i);
        }
        long delta = System.currentTimeMillis() - start;
        System.out.println("Common: " + delta + " ms");

        FastHiperbolicTangentActivationFunc fastFunc = new FastHiperbolicTangentActivationFunc(1.0);
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            a += fastFunc.calc(0.123456789 * i);
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Fast: " + delta + " ms");
    }

}