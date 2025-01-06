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
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.SymmetricMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * {@link Cholesky} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class CholeskyTest {

    public static final Class<?> TEST_CLASS = Cholesky.class;

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_非対称行列は使用できないMNSEx() {
            Cholesky.executor().apply(
                    GeneralMatrix.Builder.zero(MatrixDimension.square(3)).build());
        }
    }

    public static class 非正定値行列での振る舞い検証 {

        private EntryReadableMatrix matrix;

        @Before
        public void before_行列の準備() {
            //非正定値行列である
            /*
             * -1 2 2 -1
             * 2 5 -1 0
             * 2 -1 5 1
             * -1 0 1 3
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(4));
            builder.setValue(0, 0, -1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, -1);
            builder.setValue(2, 2, 5);
            builder.setValue(3, 0, -1);
            builder.setValue(3, 1, 0);
            builder.setValue(3, 2, 1);
            builder.setValue(3, 3, 3);
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<Cholesky> cho = Cholesky.executor().apply(matrix);
            assertThat(cho.isEmpty(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4 {

        private EntryReadableMatrix matrix;
        private Cholesky cho;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 3 2 2 -1
             * 2 5 -1 0
             * 2 -1 5 1
             * -1 0 1 3
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(4));
            builder.setValue(0, 0, 3);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, -1);
            builder.setValue(2, 2, 5);
            builder.setValue(3, 0, -1);
            builder.setValue(3, 1, 0);
            builder.setValue(3, 2, 1);
            builder.setValue(3, 3, 3);
            matrix = builder.build();
            cho = Cholesky.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(cho.determinant(), is(closeTo(13.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数の計算() {
            assertThat(cho.logAbsDeterminant(), is(closeTo(Math.log(13.0), 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(cho.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列はSymmetricである() {
            assertThat(cho.inverse() instanceof Symmetric, is(true));
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.inverse() == cho.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private EntryReadableMatrix matrix;
        private Cholesky cho;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            matrix = builder.build();
            cho = Cholesky.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(cho.determinant(), is(closeTo(2.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数の計算() {
            assertThat(cho.logAbsDeterminant(), is(closeTo(Math.log(2.0), 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(cho.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }

    public static class 行列の非対称平方根に関するテスト {

        private EntryReadableMatrix matrix;
        private Cholesky cho;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 3 2 2 -1
             * 2 5 -1 0
             * 2 -1 5 1
             * -1 0 1 3
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(4));
            builder.setValue(0, 0, 3);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, -1);
            builder.setValue(2, 2, 5);
            builder.setValue(3, 0, -1);
            builder.setValue(3, 1, 0);
            builder.setValue(3, 2, 1);
            builder.setValue(3, 3, 3);
            matrix = builder.build();
            cho = Cholesky.executor().apply(matrix).get();
        }

        @Test
        public void test_非対称平方根の検証() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(v).minus(
                        cho.asymmSqrt().operate(cho.asymmSqrt().operateTranspose(v)));
                assertThat(res.normMax(), is(lessThan(matrix.operate(v).normMax() * 1E-12)));
            }
        }

        @Test
        public void test_平方根生成の実装に関する() {

            //平方根の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.inverseAsymmSqrt(), is(cho.inverseAsymmSqrt()));
        }

        @Test
        public void test_非対称平方根の逆行列の検証() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = cho.inverse().operate(v).minus(
                        cho.inverseAsymmSqrt().operateTranspose(cho.inverseAsymmSqrt().operate(v)));
                assertThat(res.normMax(), is(lessThan(cho.inverse().operate(v).normMax() * 1E-12)));
            }
        }

        @Test
        public void test_平方根の逆行列生成の実装に関する() {

            //平方根の逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.inverseAsymmSqrt() == cho.inverseAsymmSqrt(), is(true));
        }
    }

    public static class toString表示 {

        private Cholesky.Executor executor = Cholesky.executor();
        private Cholesky cho;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            EntryReadableMatrix em = builder.build();
            cho = executor.apply(em).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(cho);
            System.out.println(cho.target());
            System.out.println(cho.inverse());
            System.out.println(cho.asymmSqrt());
            System.out.println(cho.inverseAsymmSqrt());
            System.out.println();
        }
    }
}
