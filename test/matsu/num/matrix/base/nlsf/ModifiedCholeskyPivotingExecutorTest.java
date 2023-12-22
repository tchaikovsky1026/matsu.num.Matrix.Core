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
import matsu.num.matrix.base.SymmetricMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

/**
 * {@link ModifiedCholeskyPivotingExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class ModifiedCholeskyPivotingExecutorTest {

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_非対称行列は使用できないMNSEx() {
            ModifiedCholeskyPivotingExecutor.instance().apply(
                    GeneralMatrixBuilder.zeroBuilder(MatrixDimension.square(3)).build());
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン1 {

        private LinearEquationSolving<EntryReadableMatrix> mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 1 3 6 10 15 21
             * 3 2 5 9 14 20
             * 6 5 4 8 13 19
             * 10 9 8 7 12 18
             * 15 14 13 12 11 17
             * 21 20 19 18 17 16
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(6));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(2, 2, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(2, 0, 6);
            builder.setValue(3, 3, 7);
            builder.setValue(3, 2, 8);
            builder.setValue(3, 1, 9);
            builder.setValue(3, 0, 10);
            builder.setValue(4, 4, 11);
            builder.setValue(4, 3, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(4, 1, 14);
            builder.setValue(4, 0, 15);
            builder.setValue(5, 5, 16);
            builder.setValue(5, 4, 17);
            builder.setValue(5, 3, 18);
            builder.setValue(5, 2, 19);
            builder.setValue(5, 1, 20);
            builder.setValue(5, 0, 21);
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(-43074.0, 1E-9)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(43074.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(-1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(6));
            builder.setEntryValue(new double[] { 1, 2, 3, 4, 5, 6 });
            Vector right = builder.build();

            /*
             * 0.42902911268979
             * -0.107257278172448
             * -0.0643543669034688
             * -0.042902911268979
             * -0.0306449366206991
             * 0.103217718345173
             */
            double[] expected = {
                    0.42902911268979,
                    -0.107257278172448,
                    -0.0643543669034688,
                    -0.042902911268979,
                    -0.0306449366206991,
                    0.103217718345173
            };
            Vector result = mcp.inverse().get().operateTranspose(right);
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
            assertThat(mcp.inverse() == mcp.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン2 {

        private LinearEquationSolving<EntryReadableMatrix> mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 1 2 4 7 11 16
             * 2 3 5 8 12 17
             * 4 5 6 9 13 18
             * 7 8 9 10 14 19
             * 11 12 13 14 15 20
             * 16 17 18 19 20 21
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(6));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(2, 0, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(2, 2, 6);
            builder.setValue(3, 0, 7);
            builder.setValue(3, 1, 8);
            builder.setValue(3, 2, 9);
            builder.setValue(3, 3, 10);
            builder.setValue(4, 0, 11);
            builder.setValue(4, 1, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(4, 3, 14);
            builder.setValue(4, 4, 15);
            builder.setValue(5, 0, 16);
            builder.setValue(5, 1, 17);
            builder.setValue(5, 2, 18);
            builder.setValue(5, 3, 19);
            builder.setValue(5, 4, 20);
            builder.setValue(5, 5, 21);
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(-24.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(24), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(-1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(6));
            builder.setEntryValue(new double[] { 1, 2, 3, 4, 5, 6 });
            Vector right = builder.build();

            /*
             * 15
             * -15
             * 0
             * 0
             * 0
             * 1
             */
            double[] expected = { 15, -15, 0, 0, 0, 1 };
            Vector result = mcp.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン3 {

        private LinearEquationSolving<EntryReadableMatrix> mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 0 2 4 7 11 16
             * 2 0 5 8 12 17
             * 4 5 0 0 13 18
             * 7 8 0 0 0 19
             * 11 12 13 0 0 0
             * 16 17 18 19 0 0
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(6));
            builder.setValue(1, 0, 2);
            builder.setValue(2, 0, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(3, 0, 7);
            builder.setValue(3, 1, 8);
            builder.setValue(4, 0, 11);
            builder.setValue(4, 1, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(5, 0, 16);
            builder.setValue(5, 1, 17);
            builder.setValue(5, 2, 18);
            builder.setValue(5, 3, 19);
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(1786236.0, 1E-8)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(1786236.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(6));
            builder.setEntryValue(new double[] { 1, 2, 3, 4, 5, 6 });
            Vector right = builder.build();

            /*
             * 0.42565372100887
             * -0.0350345643016937
             * 0.0567864492709809
             * -0.0651067384153045
             * 0.0184857991889089
             * 0.0684579193342874
             */
            double[] expected = {
                    0.42565372100887,
                    -0.0350345643016937,
                    0.0567864492709809,
                    -0.0651067384153045,
                    0.0184857991889089,
                    0.0684579193342874
            };
            Vector result = mcp.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private LinearEquationSolving<EntryReadableMatrix> mcp;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrixBuilder builder = SymmetricMatrixBuilder.zeroBuilder(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(builder.build());
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(2.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(2.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            Vector right = builder.build();

            /*
             * 1.5
             */
            double[] expected = {
                    1.5
            };
            Vector result = mcp.inverse().get().operateTranspose(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }

}
