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
 * {@link UnitMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class UnitMatrixTest {

    public static final Class<?> TEST_CLASS = UnitMatrix.class;

    public static class 成分の評価に関する {

        private UnitMatrix matrix;

        @Before
        public void before_次元3の単位行列の作成() {
            matrix = UnitMatrix.matrixOf(MatrixDimension.square(3));
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            matrix.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_0_3は範囲外() {
            matrix.valueAt(0, 3);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_3_0は範囲外() {
            matrix.valueAt(3, 0);
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

        private UnitMatrix matrix;

        @Before
        public void before_次元3の単位行列の作成() {
            matrix = UnitMatrix.matrixOf(MatrixDimension.square(3));
        }

        @Test
        public void test_成功パターン() {
            double[] expected = { 1, 2, 3 };
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(expected);

            Vector result = matrix
                    .operate(builder.build());
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_失敗パターン() {
            double[] expected = { 1, 2, 3, 4 };

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(expected);
            matrix.operate(builder.build());
        }
    }

    public static class toString表示 {

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(UnitMatrix.matrixOf(MatrixDimension.square(3)));
            System.out.println();
        }
    }

}
