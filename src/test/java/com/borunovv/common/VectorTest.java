package com.borunovv.common;

import org.junit.Test;

public class VectorTest {
    @Test
    public void multiplyTo() throws Exception {
        Vector v = new Vector(new double[]{1,2,3});
        Matrix m = new Matrix(3, 4);
        m.data[0][0] = 1;
        m.data[0][1] = 2;
        m.data[0][2] = 3;
        m.data[0][3] = 4;

        m.data[1][0] = 5;
        m.data[1][1] = 6;
        m.data[1][2] = 7;
        m.data[1][3] = 8;

        m.data[2][0] = 9;
        m.data[2][1] = 10;
        m.data[2][2] = 11;
        m.data[2][3] = 12;

        Vector r = v.multiplyTo(m);
    }

}