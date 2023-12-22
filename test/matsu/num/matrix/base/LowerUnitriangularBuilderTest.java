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
 * {@link LowerUnitriangularBuilder}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class LowerUnitriangularBuilderTest {

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は生成できない() {
            LowerUnitriangularBuilder.unitBuilder(MatrixDimension.rectangle(3, 4));
        }
    }

    public static class 行列の評価と演算に関する {

        private LowerUnitriangularEntryReadableMatrix lm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 3, 5 });
            right = builder.build();
        }

        @Before
        public void before_サイズ3_成分2_3_4の単位下三角行列を生成() {
            /*
             * 1 0 0
             * 2 1 0
             * 3 4 1
             */
            LowerUnitriangularBuilder builder = LowerUnitriangularBuilder.unitBuilder(MatrixDimension.square(3));
            builder.setValue(1, 0, 2);
            builder.setValue(2, 0, 3);
            builder.setValue(2, 1, 4);
            lm = builder.build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(lm.entryNormMax(), is(4.0));
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 2, 1, 0 }, { 3, 4, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            lm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 1, 5, 20 };
            Vector result = lm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            double[] expected = { 22, 23, 5 };
            Vector result = lm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 1, 1, -2 };

            //遅延初期化の可能性があるため2回以上呼ぶ
            assertThat(Arrays.equals(lm.inverse().get().operate(right).entryAsArray(), expected), is(true));
            assertThat(Arrays.equals(lm.inverse().get().operate(right).entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            double[] expected = { 20, -17, 5 };

            assertThat(Arrays.equals(lm.inverse().get().operateTranspose(right).entryAsArray(), expected), is(true));
        }
    }

    public static class 狭義下三角の退化_サイズ1_に関する {

        private LowerUnitriangularEntryReadableMatrix lm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setEntryValue(new double[] { 3 });
            right = builder.build();
        }

        @Before
        public void before_サイズ1の単位下三角行列を生成() {
            /*
             * 1
             */
            lm = LowerUnitriangularBuilder.unitBuilder(MatrixDimension.square(1))
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(lm.entryNormMax(), is(1.0));
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_逆行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lm.inverse().get().operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置逆行列ベクトル積() {
            double[] expected = { 3 };
            Vector result = lm.inverse().get().operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

}
