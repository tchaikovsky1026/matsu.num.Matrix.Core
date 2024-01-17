package matsu.num.matrix.base.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.GeneralBandMatrixBuilder;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.SymmetricBandMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

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

    public static class 行列式と逆行列ベクトル積に関する_サイズ4 {

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
            cb = CholeskyBandExecutor.instance().apply(builder.build());
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
        public void test_逆行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 2, 3, 4 });
            Vector right = builder.build();

            /*
             * 0.31578947368421
             * -0.894736842105263
             * 0.157894736842105
             * 1.42105263157895
             */
            double[] expected = {
                    0.31578947368421,
                    -0.894736842105263,
                    0.157894736842105,
                    1.42105263157895
            };
            Vector result = cb.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

        @Test
        public void test_逆行列はSymmetricである() {
            assertThat(cb.inverse().get() instanceof Symmetric, is(true));
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cb.inverse() == cb.inverse(), is(true));
        }
    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ1 {

        private SymmetrizedSquareTypeSolver cb;

        @Before
        public void before_生成() {
            /*
             * 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(1, 2));
            builder.setValue(0, 0, 5);
            cb = CholeskyBandExecutor.instance().apply(builder.build());
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
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            Vector right = builder.build();

            /*
             * 0.6
             */
            double[] expected = {
                    0.6
            };
            Vector result = cb.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

    }

    public static class 行列の非対称平方根に関するテスト {

        private Matrix matrix;
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
            BandMatrix sbm = builder.build();
            matrix = sbm;
            cho = CholeskyBandExecutor.instance().apply(sbm);
        }

        @Test
        public void test_非対称平方根の検証() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1.3, 2.1, 3.6, 4.2 });
            Vector right = builder.build();

            Matrix asymmSqrt = cho.asymmSqrt();
            double[] expected = matrix.operate(right).entryAsArray();
            double[] result = asymmSqrt.operate(asymmSqrt.operateTranspose(right)).entryAsArray();

            assertThat(result.length, is(expected.length));
            for (int i = 0; i < result.length; i++) {
                assertThat(result[i], is(closeTo(expected[i], 1E-10)));
            }
        }
    }

    public static class 行列の非対称平方根の逆行列に関するテスト {

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
            cho = CholeskyBandExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_非対称平方根の逆行列の検証() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1.3, 2.1, 3.6, 4.2 });
            Vector right = builder.build();

            Matrix asymmInvSqrt = cho.inverseAsymmSqrt();
            double[] expected = cho.inverse().get().operate(right).entryAsArray();
            double[] result = asymmInvSqrt.operateTranspose(asymmInvSqrt.operate(right)).entryAsArray();

            assertThat(result.length, is(expected.length));
            for (int i = 0; i < result.length; i++) {
                assertThat(result[i], is(closeTo(expected[i], 1E-10)));
            }
        }
    }

    public static class toString表示 {

        private SymmetrizedSquareTypeSolver cb;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 5
             */
            SymmetricBandMatrixBuilder builder =
                    SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(1, 0));
            builder.setValue(0, 0, 5);
            cb = CholeskyBandExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(cb);
            System.out.println(cb.asymmSqrt());
            System.out.println(cb.inverseAsymmSqrt());
            System.out.println();
        }
    }
}
