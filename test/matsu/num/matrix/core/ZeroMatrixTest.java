/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link ZeroMatrix} インターフェースの実装に関するテスト.
 * 実質的には, {@link ZeroMatrixImpl} のテストである.
 */
@RunWith(Enclosed.class)
final class ZeroMatrixTest {

    public static final Class<?> TEST_CLASS = ZeroMatrix.class;

    public static class 成分の評価に関する {

        private ZeroMatrix matrix;

        @Before
        public void before_次元2_3の行列の作成() {
            matrix = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
        }

        @Test
        public void test_成分の検証() {
            int rows = matrix.matrixDimension().rowAsIntValue();
            int columns = matrix.matrixDimension().columnAsIntValue();

            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            matrix.valueAt(j, k), is(0d));
                }
            }
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_0_3は範囲外() {
            matrix.valueAt(0, 3);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_2_0は範囲外() {
            matrix.valueAt(2, 0);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_0_m1は範囲外() {
            matrix.valueAt(0, -1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_m1_0は範囲外() {
            matrix.valueAt(-1, 0);
        }

    }

    public static class 行列ベクトル積に関する {

        private ZeroMatrix matrix;

        @Before
        public void before_次元2_3の行列の作成() {
            matrix = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
        }

        @Test
        public void test_成功パターン_operate() {
            int rows = matrix.matrixDimension().rowAsIntValue();
            int columns = matrix.matrixDimension().columnAsIntValue();

            double[] right = new double[columns];
            Arrays.fill(right, 1d);

            double[] expected = new double[rows];

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(right.length));
            builder.setEntryValue(right);
            Vector result = matrix.operate(builder.build());
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_失敗パターン_operate() {
            int len = 1 + matrix.matrixDimension().columnAsIntValue();
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(len)).build();
            matrix.operate(right);
        }

        @Test
        public void test_成功パターン_operateTranspose() {
            int rows = matrix.matrixDimension().rowAsIntValue();
            int columns = matrix.matrixDimension().columnAsIntValue();

            double[] right = new double[rows];
            Arrays.fill(right, 1d);

            double[] expected = new double[columns];

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(right.length));
            builder.setEntryValue(right);
            Vector result = matrix.operateTranspose(builder.build());
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_失敗パターン_operateTranspose() {
            int len = 1 + matrix.matrixDimension().rowAsIntValue();
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(len)).build();
            matrix.operateTranspose(right);
        }
    }

    public static class transposeに関する推奨実装規約のテスト {

        private ZeroMatrix matrix;

        @Before
        public void before_サイズ3_2() {
            matrix = ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2));
        }

        @Test
        public void test_複数回の呼び出しで同一のインスタンス() {
            assertThat(matrix.transpose() == matrix.transpose(), is(true));
        }

        @Test
        public void test_転置の転置は自身のインスタンス() {
            assertThat(matrix.transpose().transpose() == matrix, is(true));
        }
    }

    public static class toString表示 {

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3)));
            System.out.println();
        }
    }

}
