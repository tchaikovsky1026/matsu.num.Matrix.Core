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
import matsu.num.matrix.base.GeneralBandMatrixBuilder;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.SymmetricBandMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * {@link CholeskyBandExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class CholeskyBandExecutorTest {

    public static final Class<?> TEST_CLASS = CholeskyBandExecutor.class;

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_対称行列でなければMNSEx() {
            CholeskyBandExecutor.instance().apply(
                    GeneralBandMatrixBuilder.unitBulder(BandMatrixDimension.symmetric(2, 0)).build());
        }
    }

    public static class 非正定値行列での振る舞い検証 {

        private BandMatrix matrix;

        @Before
        public void before_行列の準備() {
            //非正定値行列である
            /*
             * -5 1 2 0
             * 1 5 3 4
             * 2 3 5 3
             * 0 4 3 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(4, 2));
            builder.setValue(0, 0, -5);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 2, 5);
            builder.setValue(3, 3, 5);
            builder.setValue(1, 0, 1);
            builder.setValue(2, 0, 2);
            builder.setValue(2, 1, 3);
            builder.setValue(3, 1, 4);
            builder.setValue(3, 2, 3);
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<? extends LUTypeSolver> cb = CholeskyBandExecutor.instance().apply(matrix);
            assertThat(cb.isEmpty(), is(true));
        }
    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ4 {

        private BandMatrix matrix;
        private SymmetrizedSquareTypeSolver cb;

        @Before
        public void before_生成() {
            /*
             * 5 1 2 0
             * 1 5 3 4
             * 2 3 5 3
             * 0 4 3 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(4, 2));
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
            cb = CholeskyBandExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(cb.determinant(), is(closeTo(95.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数の計算() {
            assertThat(cb.logAbsDeterminant(), is(closeTo(Math.log(95.0), 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積の検証() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(cb.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列はSymmetricである() {
            assertThat(cb.inverse() instanceof Symmetric, is(true));
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cb.inverse() == cb.inverse(), is(true));
        }
    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ1 {

        private BandMatrix matrix;
        private SymmetrizedSquareTypeSolver cb;

        @Before
        public void before_生成() {
            /*
             * 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(1, 2));
            builder.setValue(0, 0, 5);
            matrix = builder.build();
            cb = CholeskyBandExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(cb.determinant(), is(closeTo(5.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数の計算() {
            assertThat(cb.logAbsDeterminant(), is(closeTo(Math.log(5.0), 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(cb.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

    }

    public static class 行列の非対称平方根に関するテスト {

        private BandMatrix matrix;
        private SymmetrizedSquareTypeSolver cho;

        @Before
        public void before_生成() {
            /*
             * 5 1 2 0
             * 1 5 3 4
             * 2 3 5 3
             * 0 4 3 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(4, 2));
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
            cho = CholeskyBandExecutor.instance().apply(matrix).get();
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
    }

    public static class toString表示 {

        private CholeskyBandExecutor executor = CholeskyBandExecutor.instance();
        private SymmetrizedSquareTypeSolver cb;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(2, 0));
            cb = executor.apply(builder.build()).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(cb);
            System.out.println(cb.asymmSqrt());
            System.out.println(cb.inverseAsymmSqrt());
            System.out.println();
        }
    }
}
