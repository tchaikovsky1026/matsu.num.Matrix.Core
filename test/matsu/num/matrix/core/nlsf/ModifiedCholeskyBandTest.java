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

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.GeneralBandMatrix;
import matsu.num.matrix.core.SymmetricBandMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * {@link ModifiedCholeskyBand} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class ModifiedCholeskyBandTest {

    public static final Class<?> TEST_CLASS = ModifiedCholeskyBand.class;

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_対称行列でなければMNSEx() {
            ModifiedCholeskyBand.executor().apply(
                    GeneralBandMatrix.Builder.unit(BandMatrixDimension.symmetric(2, 0)).build());
        }
    }

    public static class 要ピボッティング行列行列での振る舞い検証 {

        private BandMatrix matrix;

        @Before
        public void before_行列の準備() {
            //最初の段が実行できない
            /*
             * 0 2 0 0
             * 2 5 0 0
             * 0 0 1 2
             * 0 0 2 1
             */
            SymmetricBandMatrix.Builder builder = SymmetricBandMatrix.Builder.zero(
                    BandMatrixDimension.symmetric(4, 1));
            builder.setValue(0, 0, 0);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 2, 1);
            builder.setValue(3, 2, 2);
            builder.setValue(3, 3, 1);
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<ModifiedCholeskyBand> cho = ModifiedCholeskyBand.executor().apply(matrix);
            assertThat(cho.isEmpty(), is(true));
        }
    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ4 {

        private BandMatrix matrix;
        private ModifiedCholeskyBand mcb;

        @Before
        public void before_生成() {
            /*
             * 5 1 2 0
             * 1 5 3 4
             * 2 3 5 3
             * 0 4 3 5
             */
            SymmetricBandMatrix.Builder builder =
                    SymmetricBandMatrix.Builder.unit(BandMatrixDimension.symmetric(4, 2));
            builder.setValue(0, 0, 5);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 2, 5);
            builder.setValue(3, 3, 5);
            builder.setValue(1, 0, 1);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, 3);
            builder.setValue(3, 1, 4);
            builder.setValue(3, 2, 3);
            matrix = builder.build();
            mcb = ModifiedCholeskyBand.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcb.determinant(), is(closeTo(95.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcb.logAbsDeterminant(), is(closeTo(Math.log(95.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcb.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcb.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(mcb.inverse() == mcb.inverse(), is(true));
        }

    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ1 {

        private BandMatrix matrix;
        private ModifiedCholeskyBand mcb;

        @Before
        public void before_生成() {
            /*
             * 5
             */
            SymmetricBandMatrix.Builder builder =
                    SymmetricBandMatrix.Builder.unit(BandMatrixDimension.symmetric(1, 2));
            builder.setValue(0, 0, 5);
            matrix = builder.build();
            mcb = ModifiedCholeskyBand.executor().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcb.determinant(), is(closeTo(5.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcb.logAbsDeterminant(), is(closeTo(Math.log(5.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcb.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcb.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

    }

    public static class toString表示 {

        private ModifiedCholeskyBand.Executor executor = ModifiedCholeskyBand.executor();
        private ModifiedCholeskyBand mcb;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            SymmetricBandMatrix.Builder builder =
                    SymmetricBandMatrix.Builder.unit(BandMatrixDimension.symmetric(1, 2));
            builder.setValue(0, 0, 5);
            mcb = executor.apply(builder.build()).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(mcb);
            System.out.println(mcb.target());
            System.out.println(mcb.inverse());
            System.out.println();
        }
    }

}
