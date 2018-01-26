package com.borunovv.common;

/**
 * @author borunovv
 */
public class Matrix {
    public final int columns;
    public final int rows;
    public final double[][] data;

    public Matrix(int rows, int columns) {
        this.columns = columns;
        this.rows = rows;
        this.data = new double[rows][columns];
    }

    public double[] row(int row) {
        return data[row];
    }

    public Matrix multiplyTo(Matrix right) {
        Assert.isTrue(this.columns == right.rows, "Can't multiply matrices (A's column count != B's row count)");
        int resultColumns = right.columns;
        int resultRows = this.rows;
        Matrix result = new Matrix(resultRows, resultColumns);

        for (int j = 0; j < resultRows; ++j) {
            double[] leftRow = data[j];
            double[] resultRow = result.data[j];
            for (int i = 0; i < resultColumns; ++i) {
                double sum = 0.0;
                for (int k = 0; k < columns; ++k) {
                    sum += leftRow[k] * right.data[k][i];
                }
                resultRow[i] = sum;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < columns; ++c) {
                if (c > 0) {
                    sb.append(",\t");
                }
                sb.append(String.format("%.5f", data[r][c]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
