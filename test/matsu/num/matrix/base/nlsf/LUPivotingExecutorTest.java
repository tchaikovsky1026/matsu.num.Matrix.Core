package matsu.num.matrix.base.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.GeneralMatrixBuilder;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * {@link LUPivotingExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class LUPivotingExecutorTest {

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は使用できないMFMEx() {
            LUPivotingExecutor.instance().apply(
                    GeneralMatrixBuilder.zeroBuilder(MatrixDimension.rectangle(3, 2)).build());
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元4 {

        private LinearEquationSolving<EntryReadableMatrix> lup;

        @Before
        public void before_次元4の正方行列のソルバを用意する() {
            /*
                1 2 3 4
                2 5 9 3
                2 6 3 1
                 -1 0 1 1
             */
            EntryReadableMatrix gm = GeneralMatrixBuilder.zeroBuilder(MatrixDimension.square(4))
                    .setValue(0, 0, 1)
                    .setValue(0, 1, 2)
                    .setValue(0, 2, 3)
                    .setValue(0, 3, 4)
                    .setValue(1, 0, 2)
                    .setValue(1, 1, 5)
                    .setValue(1, 2, 9)
                    .setValue(1, 3, 3)
                    .setValue(2, 0, 2)
                    .setValue(2, 1, 6)
                    .setValue(2, 2, 3)
                    .setValue(2, 3, 1)
                    .setValue(3, 0, -1)
                    .setValue(3, 1, 0)
                    .setValue(3, 2, 1)
                    .setValue(3, 3, 1)
                    .build();
            lup = LUPivotingExecutor.instance().apply(gm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                -3.666666667
                1.666666667
                0
                0.333333333
             */
            double[] expected = { -2.0 / 3 - 3.0, 1.0 + 2.0 / 3, 0, 1.0 / 3 };
            Vector result = lup.inverse().get().operate(right);
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
                1
                0
                0
                0
             */
            double[] expected = { 1.0, 0.0, 0.0, 0.0 };
            Vector result = lup.inverse().get().operateTranspose(right);
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
            assertThat(lup.inverse() == lup.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private LinearEquationSolving<EntryReadableMatrix> lup;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
                2
             */
            EntryReadableMatrix gm = GeneralMatrixBuilder.zeroBuilder(MatrixDimension.square(1))
                    .setValue(0, 0, 2)
                    .build();
            lup = LUPivotingExecutor.instance().apply(gm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();

            /*
                1.5
             */
            double[] expected = { 1.5 };
            Vector result = lup.inverse().get().operate(right);
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
                1.5
             */
            double[] expected = { 1.5 };
            Vector result = lup.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }

}
