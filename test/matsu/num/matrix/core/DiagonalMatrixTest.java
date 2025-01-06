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
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link DiagonalMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class DiagonalMatrixTest {

    public static final Class<?> TEST_CLASS = DiagonalMatrix.class;

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形サイズはMFMEx() {
            DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.rectangle(4, 3));
        }
    }

    public static class 成分の評価に関する {

        private DiagonalMatrix matrix;

        @Before
        public void before_次元3_成分1_2_3の対角行列の作成() {
            DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
            builder.setValue(0, 1);
            builder.setValue(1, 2);
            builder.setValue(2, 3);
            matrix = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 0, 2, 0 }, { 0, 0, 3 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            matrix.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_成分ノルムは3() {
            //遅延初期化の可能性があるので複数回実行
            assertThat(matrix.entryNormMax(), is(3d));
            assertThat(matrix.entryNormMax(), is(3d));
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

        private DiagonalMatrix matrix;

        @Before
        public void before_次元3_成分1_2_3の対角行列の作成() {
            DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
            builder.setValue(0, 1);
            builder.setValue(1, 2);
            builder.setValue(2, 3);
            matrix = builder.build();
        }

        @Test
        public void test_成功パターン() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            Vector right = builder.build();

            double[] expected = { 1, 4, 9 };
            Vector result = matrix.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_失敗パターン() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 2, 3, 4 });
            Vector right = builder.build();

            matrix.operate(right);
        }

    }

    public static class 逆行列の生成に関する {

        private DiagonalMatrix matrix;

        @Before
        public void before_次元3_成分1_2_3の対角行列() {
            DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
            builder.setValue(0, 1);
            builder.setValue(1, 2);
            builder.setValue(2, 3);
            matrix = builder.build();
        }

        @Test
        public void test_逆行列のベクトル行列積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 9, 6, 3 });
            Vector right = builder.build();

            double[] expected = { 9, 3, 1 };

            assertThat(
                    matrix.inverse().get().operate(right).entryAsArray(), is(expected));
            assertThat(matrix.inverse().get().operate(right).entryAsArray(), is(expected));

        }

        @Test
        public void test_逆行列の行列式() {
            assertThat(matrix.inverse().get().logAbsDeterminant(), is(closeTo(-matrix.logAbsDeterminant(), 1E-10)));
            assertThat(matrix.inverse().get().signOfDeterminant(), is(matrix.signOfDeterminant()));
            assertThat(matrix.inverse().get().determinant(), is(closeTo(1 / matrix.determinant(), 1E-10)));
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の逆行列は自分自身
            assertThat(matrix.inverse().get().inverse().get(), is(matrix));

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(matrix.inverse() == matrix.inverse(), is(true));
            //逆行列の逆行列の複数回の呼び出しは同一インスタンスを返す.
            assertThat(matrix.inverse().get().inverse() == matrix.inverse().get().inverse(), is(true));

        }

        @Test(expected = NoSuchElementException.class)
        public void test_次元3_成分1_0_3の対角行列は生成できない() {
            DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
            builder.setValue(0, 1);
            builder.setValue(1, 0);
            builder.setValue(2, 3);
            DiagonalMatrix matrix = builder.build();

            matrix.inverse().orElseThrow();
        }
    }

    @RunWith(Enclosed.class)
    public static class 行列式に関する {

        public static class 次元3_パターン1 {

            private DiagonalMatrix matrix;

            @Before
            public void before_次元3_成分1_2_m3の対角行列の作成() {
                DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
                builder.setValue(0, 1);
                builder.setValue(1, 2);
                builder.setValue(2, -3);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm6() {
                assertThat(matrix.determinant(), is(-6d));
            }

            @Test
            public void test_LogAbs行列式はlog6() {
                assertThat(matrix.logAbsDeterminant(), is(Math.log(6d)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元10_超大パターン {

            private DiagonalMatrix matrix;

            @Before
            public void before_次元10の超大要素の対角行列の作成() {
                DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(10));
                builder.setValue(0, 1E90);
                builder.setValue(1, 2E90);
                builder.setValue(2, -3E90);
                builder.setValue(3, -4E90);
                builder.setValue(4, 5E90);
                builder.setValue(5, 6E-90);
                builder.setValue(6, -7);
                builder.setValue(7, 8);
                builder.setValue(8, 9E-90);
                builder.setValue(9, 10);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm3628800_E270_d() {
                double expected = -3628800E+270d;
                assertThat(matrix.determinant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_LogAbs行列式はlog_3628800_E270_d() {
                double expected = Math.log(3628800E+270d);
                assertThat(matrix.logAbsDeterminant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元10_超小パターン {

            private DiagonalMatrix matrix;

            @Before
            public void before_次元10の超大要素の対角行列の作成() {
                DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(10));
                builder.setValue(0, 1);
                builder.setValue(1, 2);
                builder.setValue(2, -3);
                builder.setValue(3, -4E90);
                builder.setValue(4, 5E90);
                builder.setValue(5, 6E-90);
                builder.setValue(6, -7E-90);
                builder.setValue(7, 8E-90);
                builder.setValue(8, 9E-90);
                builder.setValue(9, 10E-90);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm3628800_Em270_d() {
                double expected = -3628800E-270d;
                assertThat(matrix.determinant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_LogAbs行列式はlog_3628800d() {
                double expected = Math.log(3628800E-270d);
                assertThat(matrix.logAbsDeterminant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元3_特異 {

            private DiagonalMatrix matrix;

            @Before
            public void before_次元3_成分1_0_m3の対角行列の作成() {
                DiagonalMatrix.Builder builder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(3));
                builder.setValue(0, 1);
                builder.setValue(1, 0);
                builder.setValue(2, -3);
                matrix = builder.build();
            }

            @Test
            public void test_行列式は0() {
                assertThat(matrix.determinant(), is(0d));
            }

            @Test
            public void test_LogAbs行列式はmInf() {
                assertThat(matrix.logAbsDeterminant(), is(Double.NEGATIVE_INFINITY));
            }

            @Test
            public void test_符号は0() {
                assertThat(matrix.signOfDeterminant(), is(0));
            }
        }
    }

    public static class toString表示 {

        private DiagonalMatrix dm;

        @Before
        public void before() {
            DiagonalMatrix.Builder builder =
                    DiagonalMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.setValue(0, 3);
            dm = builder.build();
        }

        @Test
        public void test_toString() {

            System.out.println(TEST_CLASS.getName());
            System.out.println(dm);
            System.out.println(dm.inverse().get());
            System.out.println();
        }
    }
}
