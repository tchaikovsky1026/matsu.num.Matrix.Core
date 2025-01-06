/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link Block2OrderSymmetricDiagonalMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class Block2OrderSymmetricDiagonalMatrixTest {

    public static class ビルダに関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形次元は生成できないMFMEx() {
            Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.rectangle(3, 2));
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_副対角1が埋まっているときに副対角0は代入できないIAEx() {
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(4));
            builder.setSubDiagonal(1, 1);
            builder.setSubDiagonal(0, 1);

        }

        @Test(expected = IllegalArgumentException.class)
        public void test_副対角n_m3が埋まっているときに副対角n_m2は代入できないIAEx() {
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(4));
            builder.setSubDiagonal(1, 1);
            builder.setSubDiagonal(2, 1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_副対角n_m1は行列外IIEx() {
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(4));
            builder.setSubDiagonal(3, 1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_次元1の場合_副対角n_m1は行列外IIEx() {
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(1));
            builder.setSubDiagonal(0, 1);
        }
    }

    @RunWith(Enclosed.class)
    public static class 行列式の計算に関する {

        public static class 次元6_パターン1 {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * 10 0 0 0 0 0
                 * 0 11 2 0 0 0
                 * 0 2 12 0 0 0
                 * 0 0 0 13 0 0
                 * 0 0 0 0 14 5
                 * 0 0 0 0 5 15
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, 10);
                builder.setDiagonal(1, 11);
                builder.setDiagonal(2, 12);
                builder.setDiagonal(3, 13);
                builder.setDiagonal(4, 14);
                builder.setDiagonal(5, 15);
                builder.setSubDiagonal(1, 2);
                builder.setSubDiagonal(4, 5);
                matrix = builder.build();
            }

            @Test
            public void test_行列式は3078400d() {
                assertThat(matrix.determinant(), is(closeTo(3078400d, 1E-5)));
            }

            @Test
            public void test_log行列式はlog_3078400d() {
                assertThat(matrix.logAbsDeterminant(), is(closeTo(Math.log(3078400d), 1E-10)));
            }

            @Test
            public void test_符号は正() {
                assertThat(matrix.signOfDeterminant(), is(1));
            }
        }

        public static class 次元6_パターン2 {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * -13 3 0 0 0 0
                 * 3 14 0 0 0 0
                 * 0 0 -15 0 0 0
                 * 0 0 0 16 2 0
                 * 0 0 0 2 17 0
                 * 0 0 0 0 0 -18
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, -13);
                builder.setDiagonal(1, 14);
                builder.setDiagonal(2, -15);
                builder.setDiagonal(3, 16);
                builder.setDiagonal(4, 17);
                builder.setDiagonal(5, -18);
                builder.setSubDiagonal(0, 3);
                builder.setSubDiagonal(3, 2);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm13820760d() {
                assertThat(matrix.determinant(), is(closeTo(-13820760d, 1E-5)));
            }

            @Test
            public void test_log行列式はlog_13820760d() {
                assertThat(matrix.logAbsDeterminant(), is(closeTo(Math.log(13820760d), 1E-10)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元1 {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * 2
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(1));
                builder.setDiagonal(0, -2);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm2d() {
                assertThat(matrix.determinant(), is(-2d));
            }

            @Test
            public void test_log行列式はlog_2d() {
                assertThat(matrix.logAbsDeterminant(), is(Math.log(2d)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元6_超大パターン {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * -13p 3p 0 0 0 0
                 * 3p 14p 0 0 0 0
                 * 0 0 -15p 0 0 0
                 * 0 0 0 16m 2m 0
                 * 0 0 0 2m 17m 0
                 * 0 0 0 0 0 -18m
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, -13E90);
                builder.setDiagonal(1, 14E90);
                builder.setDiagonal(2, -15E90);
                builder.setDiagonal(3, 16E45);
                builder.setDiagonal(4, 17E45);
                builder.setDiagonal(5, -18E-90);
                builder.setSubDiagonal(0, 3E90);
                builder.setSubDiagonal(3, 2E45);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm13820760_E270_d() {
                double expected = -13820760E+270d;
                assertThat(matrix.determinant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_log行列式はlog_13820760_E270_d() {
                double expected = Math.log(13820760E+270d);
                assertThat(matrix.logAbsDeterminant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元6_超小パターン {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * -13p 3p 0 0 0 0
                 * 3p 14p 0 0 0 0
                 * 0 0 -15p 0 0 0
                 * 0 0 0 16m 2m 0
                 * 0 0 0 2m 17m 0
                 * 0 0 0 0 0 -18m
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, -13E-90);
                builder.setDiagonal(1, 14E-90);
                builder.setDiagonal(2, -15E-90);
                builder.setDiagonal(3, 16E-45);
                builder.setDiagonal(4, 17E-45);
                builder.setDiagonal(5, -18E+90);
                builder.setSubDiagonal(0, 3E-90);
                builder.setSubDiagonal(3, 2E-45);
                matrix = builder.build();
            }

            @Test
            public void test_行列式はm13820760_Em270_d() {
                double expected = -13820760E-270d;
                assertThat(matrix.determinant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_log行列式はlog_13820760_Em270_d() {
                double expected = Math.log(13820760E-270d);
                assertThat(matrix.logAbsDeterminant(), is(closeTo(expected, Math.abs(expected) * 1E-12)));
            }

            @Test
            public void test_符号は負() {
                assertThat(matrix.signOfDeterminant(), is(-1));
            }
        }

        public static class 次元6_特異_パターン1 {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * -13 3 0 0 0 0
                 * 3 14 0 0 0 0
                 * 0 0 0 0 0 0
                 * 0 0 0 16 2 0
                 * 0 0 0 2 17 0
                 * 0 0 0 0 0 -18
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, -13);
                builder.setDiagonal(1, 14);
                builder.setDiagonal(2, 0);
                builder.setDiagonal(3, 16);
                builder.setDiagonal(4, 17);
                builder.setDiagonal(5, -18);
                builder.setSubDiagonal(0, 3);
                builder.setSubDiagonal(3, 2);
                matrix = builder.build();
            }

            @Test
            public void test_行列式は0d() {
                assertThat(matrix.determinant(), is(0d));
            }

            @Test
            public void test_log行列式はmInf() {
                assertThat(matrix.logAbsDeterminant(), is(Double.NEGATIVE_INFINITY));
            }

            @Test
            public void test_符号は0() {
                assertThat(matrix.signOfDeterminant(), is(0));
            }
        }

        public static class 次元6_特異_パターン2 {

            private Block2OrderSymmetricDiagonalMatrix matrix;

            @Before
            public void before() {
                /*
                 * 1 3 0 0 0 0
                 * 3 9 0 0 0 0
                 * 0 0 15 0 0 0
                 * 0 0 0 16 2 0
                 * 0 0 0 2 17 0
                 * 0 0 0 0 0 -18
                 */
                Block2OrderSymmetricDiagonalMatrix.Builder builder =
                        Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
                builder.setDiagonal(0, 1);
                builder.setDiagonal(1, 9);
                builder.setDiagonal(2, 15);
                builder.setDiagonal(3, 16);
                builder.setDiagonal(4, 17);
                builder.setDiagonal(5, -18);
                builder.setSubDiagonal(0, 3);
                builder.setSubDiagonal(3, 2);
                matrix = builder.build();
            }

            @Test
            public void test_行列式は0d() {
                assertThat(matrix.determinant(), is(0d));
            }

            @Test
            public void test_log行列式はmInf() {
                assertThat(matrix.logAbsDeterminant(), is(Double.NEGATIVE_INFINITY));
            }

            @Test
            public void test_符号は0() {
                assertThat(matrix.signOfDeterminant(), is(0));
            }
        }

    }

    public static class 行列逆行列ベクトル積 {

        private Block2OrderSymmetricDiagonalMatrix matrix;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(6));
            builder.setEntryValue(new double[] { 1, 2, 3, 4, 5, 6 });
            right = builder.build();
        }

        @Before
        public void before_次元6のブロック行列を生成() {
            /*
             * 2 2 0 0 0 0
             * 2 1 0 0 0 0
             * 0 0 2 0 0 0
             * 0 0 0 1 0 0
             * 0 0 0 0 3 2
             * 0 0 0 0 2 2
             */
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(6));
            builder.setDiagonal(0, 2);
            builder.setDiagonal(1, 1);
            builder.setDiagonal(2, 2);
            builder.setDiagonal(3, 1);
            builder.setDiagonal(4, 3);
            builder.setDiagonal(5, 2);
            builder.setSubDiagonal(0, 2);
            builder.setSubDiagonal(4, 2);
            matrix = builder.build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(matrix.entryNormMax(), is(3.0));
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 6, 4, 6, 4, 27, 22 };
            assertThat(Arrays.equals(matrix.operate(right).entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 1.5, -1, 1.5, 4, -1, 4 };

            //逆行列の逆行列は自分自身
            //注意：このテストは実装の詳細に依存している
            assertThat(matrix.inverse().get().inverse().get(), is(matrix));

            //逆行列の演算テスト
            assertThat(matrix.inverse().get().operate(right).entryAsArray(), is(expected));
        }

        @Test
        public void test_逆行列の行列式の検証() {

            //行列式の評価
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
            assertThat(matrix.inverse().get().inverse(), is(matrix.inverse().get().inverse()));

        }
    }

    public static class 副対角の退化_サイズ1 {

        private Block2OrderSymmetricDiagonalMatrix matrix;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            right = builder.build();
        }

        @Before
        public void before() {
            /*
             * 2
             */
            Block2OrderSymmetricDiagonalMatrix.Builder builder =
                    Block2OrderSymmetricDiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(1));
            builder.setDiagonal(0, 2);
            matrix = builder.build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(matrix.entryNormMax(), is(2.0));
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 6 };
            Vector result = matrix.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 1.5 };
            Vector result = matrix.inverse().get().operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }
}
