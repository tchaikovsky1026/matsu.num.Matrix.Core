package matsu.num.matrix.base.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.GeneralBandMatrix;
import matsu.num.matrix.base.Vector;

/**
 * {@link LUBandExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class LUBandExecutorTest {
    
    public static final Class<?> TEST_CLASS = LUBandExecutor.class;

    public static class 要ピボッティング行列での振る舞い検証 {

        private BandMatrix matrix;

        @Before
        public void before_行列の準備() {
            //最初の段が実行できない
            /*
             * 0 1 0 0
             * 1 2 2 0
             * 0 3 3 3
             * 0 0 5 4
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 1, 1));
            builder.setValue(0, 0, 0);
            builder.setValue(1, 1, 2);
            builder.setValue(2, 2, 3);
            builder.setValue(3, 3, 4);
            builder.setValue(1, 0, 1);
            builder.setValue(2, 1, 3);
            builder.setValue(3, 2, 5);
            builder.setValue(0, 1, 1);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 3, 3);
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<? extends LUTypeSolver> lub = LUBandExecutor.instance().apply(matrix);
            assertThat(lub.isEmpty(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4_パターン1 {

        private BandMatrix matrix;
        private LUTypeSolver lub;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 1 1 0 0
             * 1 2 2 0
             * 2 3 3 3
             * 0 4 5 4
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 2, 1));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 2);
            builder.setValue(2, 2, 3);
            builder.setValue(3, 3, 4);
            builder.setValue(1, 0, 1);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, 3);
            builder.setValue(3, 1, 4);
            builder.setValue(3, 2, 5);
            builder.setValue(0, 1, 1);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 3, 3);
            matrix = builder.build();
            lub = LUBandExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lub.determinant(), is(closeTo(13.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(lub.logAbsDeterminant(), is(closeTo(Math.log(13.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(lub.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(lub.inverse().operate(v)).minus(v);
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

                Vector res = matrix.operateTranspose(lub.inverse().operateTranspose(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4_パターン2 {

        private BandMatrix matrix;
        private LUTypeSolver lub;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 1 2 3 0
             * 3 2 4 5
             * 0 2 3 6
             * 0 0 1 4
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 1, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 2);
            builder.setValue(2, 2, 3);
            builder.setValue(3, 3, 4);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 2, 4);
            builder.setValue(1, 3, 5);
            builder.setValue(2, 3, 6);
            builder.setValue(1, 0, 3);
            builder.setValue(2, 1, 2);
            builder.setValue(3, 2, 1);
            matrix = builder.build();
            lub = LUBandExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lub.determinant(), is(closeTo(26.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(lub.logAbsDeterminant(), is(closeTo(Math.log(26.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(lub.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(lub.inverse().operate(v)).minus(v);
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

                Vector res = matrix.operateTranspose(lub.inverse().operateTranspose(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(lub.inverse() == lub.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private BandMatrix matrix;
        private LUTypeSolver lub;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * -2
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(1, 1, 1));
            builder.setValue(0, 0, -2);
            matrix = builder.build();
            lub = LUBandExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lub.determinant(), is(closeTo(-2.0, 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(lub.inverse().operate(v)).minus(v);
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

                Vector res = matrix.operateTranspose(lub.inverse().operateTranspose(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }
    
    public static class toString表示 {

        private LUBandExecutor executor = LUBandExecutor.instance();
        private LUTypeSolver lub;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(1, 1, 1));
            builder.setValue(0, 0, -2);
            lub = executor.apply(builder.build()).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(lub);
            System.out.println();
        }
    }
}
