/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link LUPivoting} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class LUPivotingTest {
    public static final Class<?> TEST_CLASS = LUPivoting.class;

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は使用できないMFMEx() {
            LUPivoting.executor().apply(
                    GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 2)).build());
        }
    }

    public static class 特異行列での振る舞い検証 {

        private EntryReadableMatrix matrix;

        @Before
        public void before_行列の準備() {
            //特異行列である
            double[][] entry = {
                    { 0, 1, 0, 0 },
                    { 1, 0, 0, 0 },
                    { 2, 6, 1, 2 },
                    { -1, 0, 2, 4 }
            };
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.square(4));
            for (int j = 0; j < entry.length; j++) {
                for (int k = 0; k < entry[j].length; k++) {
                    builder.setValue(j, k, entry[j][k]);
                }
            }
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<? extends LUTypeSolver> lup = LUPivoting.executor().apply(matrix);
            assertThat(lup.isEmpty(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4 {

        private EntryReadableMatrix matrix;
        private LUPivoting lup;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 1 2 3 4
             * 2 5 9 3
             * 2 6 3 1
             * -1 0 1 1
             */
            double[][] entry = {
                    { 1, 2, 3, 4 },
                    { 2, 5, 9, 3 },
                    { 2, 6, 3, 1 },
                    { -1, 0, 1, 1 }
            };
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.square(4));
            for (int j = 0; j < entry.length; j++) {
                for (int k = 0; k < entry[j].length; k++) {
                    builder.setValue(j, k, entry[j][k]);
                }
            }
            matrix = builder.build();
            lup = LUPivoting.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lup.determinant(), is(closeTo(-129.0, 1E-10)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(lup.logAbsDeterminant(), is(closeTo(Math.log(129), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(lup.signOfDeterminant(), is(-1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(lup.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operateTranspose(lup.inverse().operateTranspose(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(lup.inverse() == lup.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private EntryReadableMatrix matrix;
        private LUPivoting lup;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            matrix = builder.build();
            lup = LUPivoting.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lup.determinant(), is(2.0));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(lup.logAbsDeterminant(), is(Math.log(2)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(lup.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(lup.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operateTranspose(lup.inverse().operateTranspose(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }

    public static class toString表示 {

        private LUPivoting.Executor executor = LUPivoting.executor();
        private LUPivoting lup;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            lup = executor.apply(builder.build()).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(lup);
            System.out.println(lup.target());
            System.out.println(lup.inverse());
            System.out.println();
        }
    }

}
