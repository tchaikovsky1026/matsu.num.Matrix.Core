package matsu.num.matrix.base;

import static matsu.num.matrix.base.helper.value.BandDimensionPositionState.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * {@link BandMatrixDimension}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class BandMatrixDimensionTest {

    public static final Class<?> TEST_CLASS = BandMatrixDimension.class;

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形の行列次元ではMFMEx() {
            BandMatrixDimension.of(MatrixDimension.rectangle(3, 2), 0, 0);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_下側帯幅が負ではIAEx() {
            BandMatrixDimension.of(MatrixDimension.square(3), -1, 0);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_上側帯幅が負ではIAEx() {
            BandMatrixDimension.of(MatrixDimension.square(3), 0, -1);
        }
    }

    public static class 帯構造に関する {

        @Test
        public void test_両側帯行列() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 1, 1);
            assertThat(dimension.isLowerTriangular(), is(false));
            assertThat(dimension.isUpperTriangular(), is(false));
            assertThat(dimension.isDiagonal(), is(false));
        }

        @Test
        public void test_下三角行列() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 1, 0);
            assertThat(dimension.isLowerTriangular(), is(true));
            assertThat(dimension.isUpperTriangular(), is(false));
            assertThat(dimension.isDiagonal(), is(false));
        }

        @Test
        public void test_上三角行列() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 0, 1);
            assertThat(dimension.isLowerTriangular(), is(false));
            assertThat(dimension.isUpperTriangular(), is(true));
            assertThat(dimension.isDiagonal(), is(false));
        }

        @Test
        public void test_対角行列() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 0, 0);
            assertThat(dimension.isLowerTriangular(), is(true));
            assertThat(dimension.isUpperTriangular(), is(true));
            assertThat(dimension.isDiagonal(), is(true));
        }
    }

    public static class indexStateに関する {

        private BandMatrixDimension dimension;

        @Before
        public void before_次元4_帯2_1の構造を作成() {
            dimension = BandMatrixDimension.of(4, 2, 1);
        }

        @Test
        public void test_行列内部の検証() {
            BandDimensionPositionState[][] expected = new BandDimensionPositionState[][] {
                    { DIAGONAL, UPPER_BAND, OUT_OF_BAND, OUT_OF_BAND },
                    { LOWER_BAND, DIAGONAL, UPPER_BAND, OUT_OF_BAND },
                    { LOWER_BAND, LOWER_BAND, DIAGONAL, UPPER_BAND },
                    { OUT_OF_BAND, LOWER_BAND, LOWER_BAND, DIAGONAL }
            };

            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            BandDimensionPositionState.positionStateAt(j, k, dimension), is(expected[j][k]));
                }
            }
        }

        @Test
        public void test_0_4は外部() {
            assertThat(BandDimensionPositionState.positionStateAt(0, 4, dimension), is(OUT_OF_MATRIX));
        }

        @Test
        public void test_4_0は外部() {
            assertThat(BandDimensionPositionState.positionStateAt(4, 0, dimension), is(OUT_OF_MATRIX));
        }

        @Test
        public void test_0_m1は外部() {
            assertThat(BandDimensionPositionState.positionStateAt(0, -1, dimension), is(OUT_OF_MATRIX));
        }

        @Test
        public void test_m1_0は外部() {
            assertThat(BandDimensionPositionState.positionStateAt(-1, 0, dimension), is(OUT_OF_MATRIX));
        }

    }

    public static class 転置に関する {

        @Test
        public void test_サイズ4_下2_上1_の転置は_下1_上2() {
            BandMatrixDimension bandMatrixDimension = BandMatrixDimension.of(4, 2, 1);
            //遅延初期化の可能性があるので2回実行
            assertThat(bandMatrixDimension.transpose(), is(BandMatrixDimension.of(4, 1, 2)));
        }

        @Test
        public void test_サイズ4_下2_上1_の転置の転置は_下2_上1() {
            BandMatrixDimension bandMatrixDimension = BandMatrixDimension.of(4, 2, 1);
            //遅延初期化の可能性があるので2回実行
            assertThat(bandMatrixDimension.transpose().transpose(), is(BandMatrixDimension.of(4, 2, 1)));
        }

        @Test
        public void test_サイズ4_帯2_対称_の転置はそのまま() {
            BandMatrixDimension bandMatrixDimension = BandMatrixDimension.symmetric(4, 2);
            //遅延初期化の可能性があるので2回実行
            assertThat(bandMatrixDimension.transpose(), is(BandMatrixDimension.symmetric(4, 2)));
        }

        @Test
        public void test_サイズ4_帯2_対称_の転置の転置はそのまま() {
            BandMatrixDimension bandMatrixDimension = BandMatrixDimension.symmetric(4, 2);
            //遅延初期化の可能性があるので2回実行
            assertThat(bandMatrixDimension.transpose().transpose(), is(BandMatrixDimension.symmetric(4, 2)));
        }
    }
    
    public static class toString表示 {

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(BandMatrixDimension.of(4, 2, 1));
            System.out.println();
        }
    }
}
