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
 * {@link UnitMatrix}クラスのテスト.
 *
 * @author Matsuura, Y.
 */
@RunWith(Enclosed.class)
public class UnitMatrixTest {

    public static class 成分の評価に関する {

        private UnitMatrix matrix;

        @Before
        public void before_次元3の単位行列の作成() {
            matrix = UnitMatrix.matrixOf(MatrixDimension.square(3));
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            matrix.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_0_3は範囲外() {
            matrix.valueAt(0, 3);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_3_0は範囲外() {
            matrix.valueAt(3, 0);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_0_m1は範囲外() {
            matrix.valueAt(0, -1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_m1_0は範囲外() {
            matrix.valueAt(-1, 0);
        }

    }

    public static class 行列ベクトル積に関する {

        private UnitMatrix matrix;

        @Before
        public void before_次元3の単位行列の作成() {
            matrix = UnitMatrix.matrixOf(MatrixDimension.square(3));
        }

        @Test
        public void test_成功パターン() {
            double[] expected = { 1, 2, 3 };
            Vector result = matrix
                    .operate(Vector.Builder.zeroBuilder(VectorDimension.valueOf(3)).setEntryValue(expected).build());
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_失敗パターン() {
            double[] expected = { 1, 2, 3, 4 };
            matrix.operate(Vector.Builder.zeroBuilder(VectorDimension.valueOf(4)).setEntryValue(expected).build());
        }
    }

    public static class 直交行列の骨格実装のテストを兼ねる {

        private UnitMatrix matrix;

        @Before
        public void before_次元3の単位行列の作成() {
            matrix = UnitMatrix.matrixOf(MatrixDimension.square(3));
        }

        @Test
        public void test_対称な直交行列の逆行列は自身と同一() {
            if (matrix instanceof SkeletalOrthogonalMatrix) {
                //単位行列が骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(matrix.inverse().get(), is(matrix));
            }
        }

        @Test
        public void test_逆行列のオプショナルは同一インスタンスを参照する() {
            if (matrix instanceof SkeletalOrthogonalMatrix) {
                //単位行列が骨格実装を継承している場合のみ, このテストを走らせる
                //オプショナル自体が同一インスタンスである
                assertThat(matrix.inverse() == matrix.inverse(), is(true));
            }
        }
    }

}
