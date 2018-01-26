package com.borunovv.common;

import java.util.Arrays;

/**
 * @author borunovv
 */
public class Vector {
    public final int size;
    public final double[] data;

    public Vector(int size) {
        this.size = size;
        this.data = new double[size];
    }

    public Vector(double[] values) {
        this.size = values.length;
        this.data = values;
    }

    public static Vector of(double... values) {
        return new Vector(values);
    }

    public Vector multiplyTo(Matrix m) {
        Assert.isTrue(size == m.rows, "Can't multiply vector to matrix: vector size != m.rows");
        Vector result = new Vector(m.columns);
        multiplyTo(m, result);
        return result;
    }

    public void multiplyTo(Matrix m, Vector result) {
        Assert.isTrue(size == m.rows, "Can't multiply vector to matrix: vector size != m.rows");
        Assert.isTrue(result.size == m.columns, "Can't multiply vector to matrix: result vector size != m.columns");

//        double sum;
//        for (int i = 0; i < m.columns; ++i) {
//            sum = 0.0;
//            for (int j = 0; j < size; ++j) {
//                sum += data[j] * m.data[j][i];
//            }
//            result.data[i] = sum;
//        }

        Arrays.fill(result.data, 0.0);
        double[] row;
        double temp;
        for (int j = 0; j < size; ++j) {
            row = m.data[j];
            temp = data[j];
            for (int i = 0; i < m.columns; ++i) {
                result.data[i] += temp * row[i];
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < size; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("%.5f", data[i]));
        }
        sb.append("}");

        return sb.toString();
    }
}
