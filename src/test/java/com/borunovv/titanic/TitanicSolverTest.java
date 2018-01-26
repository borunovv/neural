package com.borunovv.titanic;

import org.junit.Test;

public class TitanicSolverTest {

    @Test
    public void testTitanic() throws Exception {
        String trainCsvResource = "/titanic/train.csv";
        String testCsvResource = "/titanic/test.csv";
        String outputCsvFile = "result.csv";

        new TitanicSolver().solve(trainCsvResource, testCsvResource, outputCsvFile);
    }
}