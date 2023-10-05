package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * {@link LowerUnitriangularBandBuilder}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class LowerUnitriangularBandBuilderTest {

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_下三角構造でなければMFMEx() {
            LowerUnitriangularBandBuilder.unitBuilder(BandMatrixDimension.of(3, 1, 1));
        }
    }

    public static class 行列の評価と演算に関する {

        private LowerUnitriangularEntryReadableMatrix lbm;

        @Before
        public void before_サイズ4_成分2_3_4_5_6の単位下三角行列を生成() {
            /*
                1 0 0 0
                2 1 0 0
                3 4 1 0
                0 5 6 1
             */
            lbm = LowerUnitriangularBandBuilder.unitBuilder(BandMatrixDimension.of(4, 2, 0))
                    .setValue(1, 0, 2)
                    .setValue(2, 0, 3)
                    .setValue(2, 1, 4)
                    .setValue(3, 1, 5)
                    .setValue(3, 2, 6)
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(lbm.entryNormMax(), is(6.0));
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0, 0 }, { 2, 1, 0, 0 }, { 3, 4, 1, 0 }, { 0, 5, 6, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            lbm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();
            double[] expected = { 1, 4, 14, 32 };
            Vector result = lbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 2, 3, 4 }).build();
            double[] expected = { 14, 34, 27, 4 };
            Vector result = lbm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 3, 5, 7 }).build();
            double[] expected = { 1, 1, -2, 14 };

            //遅延初期化の可能性があるため2回以上呼ぶ
            assertThat(Arrays.equals(lbm.inverse().get().operate(right).entryAsArray(), expected), is(true));
            assertThat(Arrays.equals(lbm.inverse().get().operate(right).entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 3, 5, 7 }).build();
            double[] expected = { -120, 116, -37, 7 };

            assertThat(Arrays.equals(lbm.inverse().get().operateTranspose(right).entryAsArray(), expected), is(true));
        }
    }

    public static class 帯の退化に関する {

        private LowerUnitriangularEntryReadableMatrix lbm;

        @Before
        public void before_サイズ4_成分2_3_4_5_6の単位下三角行列を生成() {
            /*
               1
             */
            lbm = LowerUnitriangularBandBuilder.unitBuilder(BandMatrixDimension.of(1, 0, 0))
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(lbm.entryNormMax(), is(1.0));
        }

        @Test
        public void test_行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();
            double[] expected = { 3 };
            Vector result = lbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();
            double[] expected = { 3 };
            Vector result = lbm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();
            double[] expected = { 3 };
            Vector result = lbm.inverse().get().operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).setEntryValue(new double[] { 3 })
                    .build();
            double[] expected = { 3 };
            Vector result = lbm.inverse().get().operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }
}
