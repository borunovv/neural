package com.borunovv.common;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MatrixTest {

    @Test
    public void multiplyMatrices() throws Exception {
        Matrix m1 = new Matrix(3,2);
        m1.data[0][0] = 1; m1.data[0][1] = 2;
        m1.data[1][0] = 3; m1.data[1][1] = 4;
        m1.data[2][0] = 5; m1.data[2][1] = 6;
        Matrix m2 = new Matrix(2,4);
        m2.data[0][0] = 7; m2.data[0][1] = 8; m2.data[0][2] = 9;m2.data[0][3] = 10;
        m2.data[1][0] = 11; m2.data[1][1] = 12; m2.data[1][2] = 13;m2.data[1][3] = 14;

        Matrix m3 = m1.multiplyTo(m2);
        assertEquals(29.0, m3.data[0][0]);
        assertEquals(32.0, m3.data[0][1]);
        assertEquals(35.0, m3.data[0][2]);
        assertEquals(38.0, m3.data[0][3]);

        assertEquals(65.0, m3.data[1][0]);
        assertEquals(72.0, m3.data[1][1]);
        assertEquals(79.0, m3.data[1][2]);
        assertEquals(86.0, m3.data[1][3]);

        assertEquals(101.0, m3.data[2][0]);
        assertEquals(112.0, m3.data[2][1]);
        assertEquals(123.0, m3.data[2][2]);
        assertEquals(134.0, m3.data[2][3]);

        System.out.println(m3);
    }

    @Test
    public void testMultiplyVectorToMatrix() throws Exception {
        Matrix m = new Matrix(3,2);
        m.data[0][0] = 1; m.data[0][1] = 2;
        m.data[1][0] = 3; m.data[1][1] = 4;
        m.data[2][0] = 5; m.data[2][1] = 6;

        Vector v = Vector.of(1,2,3);
        Vector r = v.multiplyTo(m);

        System.out.println(r);

        assertEquals(2, r.size);
        assertEquals(22.0, r.data[0]);
        assertEquals(28.0, r.data[1]);
    }
}