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

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 2, 3, 4 });
            right = builder.build();
        }

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 1 1 0 0
             * 1 2 2 0
             * 2 3 3 3
             * 0 4 5 4
             */
            GeneralBandMatrixBuilder builder = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(4, 2, 1));
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
            lub = LUBandExecutor.instance().apply(builder.build());
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

            /*
             * 0
             * 1
             * 0
             * 0
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

            /*
             * -0.923076923076923
             * -0.538461538461538
             * 1.23076923076923
             * 0.076923076923077
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

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 2, 3, 4 });
            right = builder.build();
        }

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
             * 1 2 3 0
             * 3 2 4 5
             * 0 2 3 6
             * 0 0 1 4
             */
            GeneralBandMatrixBuilder builder = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(4, 1, 2));
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
            lub = LUBandExecutor.instance().apply(builder.build());
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

            /*
             * -1.53846153846154
             * -4.26923076923077
             * 3.69230769230769
             * 0.0769230769230771
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

            /*
             * 0.0769230769230772
             * 0.307692307692308
             * 0.615384615384615
             * -0.307692307692308
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

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            right = builder.build();
        }

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * -2
             */
            GeneralBandMatrixBuilder builder = GeneralBandMatrixBuilder.zeroBuilder(BandMatrixDimension.of(1, 1, 1));
            builder.setValue(0, 0, -2);

            lub = LUBandExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_行列式の計算() {
            assertThat(lub.determinant(), is(closeTo(-2.0, 1E-12)));
        }

        @Test
        public void test_逆行列ベクトル積() {

            /*
             * -1.5
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

            /*
             * -1.5
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
