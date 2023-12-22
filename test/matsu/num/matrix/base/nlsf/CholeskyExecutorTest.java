package matsu.num.matrix.base.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.GeneralMatrixBuilder;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.SymmetricMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

/**
 * {@link CholeskyExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class CholeskyExecutorTest {

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_非対称行列は使用できないMNSEx() {
            CholeskyExecutor.instance().apply(
                    GeneralMatrixBuilder.zeroBuilder(MatrixDimension.square(3)).build());
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4 {

        private AsymmetricSqrtFactorization<EntryReadableMatrix> cho;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 3 2 2 -1
             * 2 5 -1 0
             * 2 -1 5 1
             * -1 0 1 3
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(4));
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
            cho = CholeskyExecutor.instance().apply(builder.build());
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
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 2, 3, 4 });
            Vector right = builder.build();

            /*
             * 1.38461538461539
             * -0.230769230769231
             * -0.384615384615383
             * 1.92307692307692
             */
            double[] expected = {
                    1.38461538461539,
                    -0.230769230769231,
                    -0.384615384615383,
                    1.92307692307692
            };
            Vector result = cho.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

        @Test
        public void test_逆行列はSymmetricである() {
            assertThat(cho.inverse().get() instanceof Symmetric, is(true));
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.inverse() == cho.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private AsymmetricSqrtFactorization<EntryReadableMatrix> cho;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            cho = CholeskyExecutor.instance().apply(builder.build());
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
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            Vector right = builder.build();

            /*
             * 1.5
             */
            double[] expected = { 1.5 };
            Vector result = cho.inverse().get().operateTranspose(right);
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
        private AsymmetricSqrtFactorization<EntryReadableMatrix> cho;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1.3, 2.1, 3.6, 4.2 });
            right = builder.build();
        }

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 3 2 2 -1
             * 2 5 -1 0
             * 2 -1 5 1
             * -1 0 1 3
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(4));
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
            EntryReadableMatrix em = builder.build();
            matrix = em;
            cho = CholeskyExecutor.instance().apply(em);
        }

        @Test
        public void test_非対称平方根の検証() {
            Matrix asymmSqrt = cho.asymmetricSqrtSystem().target();
            double[] expected = matrix.operate(right).entryAsArray();
            double[] result = asymmSqrt.operate(asymmSqrt.operateTranspose(right)).entryAsArray();

            assertThat(result.length, is(expected.length));
            for (int i = 0; i < result.length; i++) {
                assertThat(result[i], is(closeTo(expected[i], 1E-10)));
            }
        }

        @Test
        public void test_平方根生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //平方根の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.asymmetricSqrtSystem(), is(cho.asymmetricSqrtSystem()));
        }

        @Test
        public void test_非対称平方根の逆行列の検証() {
            Matrix asymmInvSqrt = cho.asymmetricSqrtSystem().inverse().get();
            double[] expected = cho.inverse().get().operate(right).entryAsArray();
            double[] result = asymmInvSqrt.operateTranspose(asymmInvSqrt.operate(right)).entryAsArray();

            assertThat(result.length, is(expected.length));
            for (int i = 0; i < result.length; i++) {
                assertThat(result[i], is(closeTo(expected[i], 1E-10)));
            }
        }

        @Test
        public void test_平方根の逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //平方根の逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(cho.asymmetricSqrtSystem().inverse() == cho.asymmetricSqrtSystem().inverse(), is(true));
        }
    }

    public static class toString表示 {

        private AsymmetricSqrtFactorization<EntryReadableMatrix> cho;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            EntryReadableMatrix em = builder.build();
            cho = CholeskyExecutor.instance().apply(em);
        }

        @Test
        public void test_toString表示() {
            System.out.println(CholeskyExecutor.class.getName() + ":");
            System.out.println(cho);
            System.out.println(cho.asymmetricSqrtSystem());
            System.out.println();
        }
    }
}
