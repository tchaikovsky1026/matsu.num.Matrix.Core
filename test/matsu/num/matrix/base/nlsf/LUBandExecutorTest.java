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
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;

/**
 * {@link LUBandExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class LUBandExecutorTest {

    public static class 行列分解と逆行列ベクトル積_次元4_パターン1 {

        private LinearEquationSolving<BandMatrix> lub;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
                1 1 0 0
                1 2 2 0
                2 3 3 3
                0 4 5 4
             */
            BandMatrix gbm = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(4, 2, 1))
                    .setValue(0, 0, 1)
                    .setValue(1, 1, 2)
                    .setValue(2, 2, 3)
                    .setValue(3, 3, 4)
                    .setValue(1, 0, 1)
                    .setValue(2, 0, 2)
                    .setValue(2, 1, 3)
                    .setValue(3, 1, 4)
                    .setValue(3, 2, 5)
                    .setValue(0, 1, 1)
                    .setValue(1, 2, 2)
                    .setValue(2, 3, 3)
                    .build();

            lub = LUBandExecutor.instance().apply(gbm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                0
                1
                0
                0
             */
            double[] expected = { 0, 1, 0, 0 };
            Vector result = lub.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                -0.923076923076923
                -0.538461538461538
                1.23076923076923
                0.076923076923077
             */
            double[] expected = {
                    -0.923076923076923,
                    -0.538461538461538,
                    1.23076923076923,
                    0.076923076923077
            };
            Vector result = lub.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4_パターン2 {

        private LinearEquationSolving<BandMatrix> lub;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
                1 2 3 0
                3 2 4 5
                0 2 3 6
                0 0 1 4
             */
            BandMatrix gbm = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(4, 1, 2))
                    .setValue(0, 0, 1)
                    .setValue(1, 1, 2)
                    .setValue(2, 2, 3)
                    .setValue(3, 3, 4)
                    .setValue(0, 1, 2)
                    .setValue(0, 2, 3)
                    .setValue(1, 2, 4)
                    .setValue(1, 3, 5)
                    .setValue(2, 3, 6)
                    .setValue(1, 0, 3)
                    .setValue(2, 1, 2)
                    .setValue(3, 2, 1)
                    .build();

            lub = LUBandExecutor.instance().apply(gbm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                -1.53846153846154
                -4.26923076923077
                3.69230769230769
                0.0769230769230771
             */
            double[] expected = {
                    -1.53846153846154,
                    -4.26923076923077,
                    3.69230769230769,
                    0.0769230769230771
            };
            Vector result = lub.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                0.0769230769230772
                0.307692307692308
                0.615384615384615
                -0.307692307692308
             */
            double[] expected = {
                    0.0769230769230772,
                    0.307692307692308,
                    0.615384615384615,
                    -0.307692307692308
            };
            Vector result = lub.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
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

        private LinearEquationSolving<BandMatrix> lub;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
                -2
             */
            BandMatrix gbm = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(1, 1, 1))
                    .setValue(0, 0, -2)
                    .build();

            lub = LUBandExecutor.instance().apply(gbm);
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lub.determinant(), is(closeTo(-2.0, 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();

            /*
                -1.5
             */
            double[] expected = { -1.5 };
            Vector result = lub.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();

            /*
                -1.5
             */
            double[] expected = { -1.5 };
            Vector result = lub.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }
}
