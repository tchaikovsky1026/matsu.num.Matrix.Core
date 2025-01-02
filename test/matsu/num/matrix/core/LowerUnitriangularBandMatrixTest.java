package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link LowerUnitriangularBandMatrix} クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class LowerUnitriangularBandMatrixTest {

    public static final Class<?> TEST_CLASS = LowerUnitriangularBandMatrix.class;

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_下三角構造でなければMFMEx() {
            LowerUnitriangularBandMatrix.Builder.unit(BandMatrixDimension.of(3, 1, 1));
        }
    }

    public static class 行列の評価と演算に関する {

        private LowerUnitriangular lbm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 3, 5, 7 });
            right = builder.build();
        }

        @Before
        public void before_サイズ4_成分2_3_4_5_6の単位下三角行列を生成() {
            /*
             * 1 0 0 0
             * 2 1 0 0
             * 3 4 1 0
             * 0 5 6 1
             */
            LowerUnitriangularBandMatrix.Builder builder =
                    LowerUnitriangularBandMatrix.Builder.unit(BandMatrixDimension.of(4, 2, 0));
            builder.setValue(1, 0, 2);
            builder.setValue(2, 0, 3);
            builder.setValue(2, 1, 4);
            builder.setValue(3, 1, 5);
            builder.setValue(3, 2, 6);
            lbm = builder.build();
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
            double[] expected = { 1, 5, 20, 52 };
            Vector result = lbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            double[] expected = { 22, 58, 47, 7 };
            Vector result = lbm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 1, 1, -2, 14 };

            //遅延初期化の可能性があるため2回以上呼ぶ
            assertThat(Arrays.equals(lbm.inverse().get().operate(right).entryAsArray(), expected), is(true));
            assertThat(Arrays.equals(lbm.inverse().get().operate(right).entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            double[] expected = { -120, 116, -37, 7 };

            assertThat(Arrays.equals(lbm.inverse().get().operateTranspose(right).entryAsArray(), expected), is(true));
        }
    }

    public static class 帯の退化に関する {

        private LowerUnitriangular lbm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            right = builder.build();
        }

        @Before
        public void before_サイズ4_成分2_3_4_5_6の単位下三角行列を生成() {
            /*
             * 1
             */
            lbm = LowerUnitriangularBandMatrix.Builder.unit(BandMatrixDimension.of(1, 0, 0))
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(lbm.entryNormMax(), is(1.0));
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lbm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lbm.inverse().get().operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lbm.inverse().get().operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class toString表示 {

        private LowerUnitriangular lbm;

        @Before
        public void before_サイズ4_成分2_3_4_5_6の単位下三角行列を生成() {
            /*
             * 1 0 0 0
             * 2 1 0 0
             * 3 4 1 0
             * 0 5 6 1
             */
            LowerUnitriangularBandMatrix.Builder builder =
                    LowerUnitriangularBandMatrix.Builder.unit(BandMatrixDimension.of(4, 2, 0));
            builder.setValue(1, 0, 2);
            builder.setValue(2, 0, 3);
            builder.setValue(2, 1, 4);
            builder.setValue(3, 1, 5);
            builder.setValue(3, 2, 6);
            lbm = builder.build();
        }

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(lbm);
            System.out.println(lbm.transpose());
            System.out.println(lbm.inverse().get());
            System.out.println();
        }
    }
}
