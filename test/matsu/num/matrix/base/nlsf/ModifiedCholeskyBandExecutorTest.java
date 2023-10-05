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
import matsu.num.matrix.base.SymmetricBandMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

/**
 * {@link ModifiedCholeskyBandExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class ModifiedCholeskyBandExecutorTest {

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_対称行列でなければMNSEx() {
            ModifiedCholeskyBandExecutor.instance().apply(
                    GeneralBandMatrixBuilder.unitBulder(BandMatrixDimension.symmetric(2, 0)).build());
        }
    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ4 {

        private LinearEquationSolving<BandMatrix> mcb;

        @Before
        public void before_生成() {
            /*
                5 1 2 0
                1 5 3 4
                2 3 5 3
                0 4 3 5
             */

            BandMatrix sbm = SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(4, 2))
                    .setValue(0, 0, 5)
                    .setValue(1, 1, 5)
                    .setValue(2, 2, 5)
                    .setValue(3, 3, 5)
                    .setValue(1, 0, 1)
                    .setValue(2, 0, 2)
                    .setValue(2, 1, 3)
                    .setValue(3, 1, 4)
                    .setValue(3, 2, 3)
                    .build();
            mcb = ModifiedCholeskyBandExecutor.instance().apply(sbm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();

            /*
                0.31578947368421
                -0.894736842105263
                0.157894736842105
                1.42105263157895
             */
            double[] expected = {
                    0.31578947368421,
                    -0.894736842105263,
                    0.157894736842105,
                    1.42105263157895
            };
            Vector result = mcb.inverse().get().operateTranspose(right);
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
            assertThat(mcb.inverse() == mcb.inverse(), is(true));
        }

    }

    public static class 行列式と逆行列ベクトル積に関する_サイズ1 {

        private LinearEquationSolving<BandMatrix> mcb;

        @Before
        public void before_生成() {
            /*
                5
             */

            BandMatrix sbm = SymmetricBandMatrixBuilder.unitBuilder(BandMatrixDimension.symmetric(1, 2))
                    .setValue(0, 0, 5)
                    .build();
            mcb = ModifiedCholeskyBandExecutor.instance().apply(sbm);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();

            /*
                0.6
             */
            double[] expected = {
                    0.6
            };
            Vector result = mcb.inverse().get().operate(right);
            double[] resultArray = result.entryAsArray();
            for (int i = 0; i < resultArray.length; i++) {
                assertThat(
                        String.format("i=%d,result=%f,expected=%f", i, resultArray[i], expected[i]),
                        resultArray[i], is(closeTo(expected[i], 1E-12)));
            }
        }

    }

}
