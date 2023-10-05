package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link SymmetricBandMatrixBuilder}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class SymmetricBandMatrixBuilderTest {

    public static class 対称帯行列の評価と演算に関する {

        private BandMatrix sbm;

        @Before
        public void before() {
            /*
                1 4 0
                4 2 5
                0 5 3
             */
            sbm = SymmetricBandMatrixBuilder.zeroBuilder(BandMatrixDimension.symmetric(3, 1))
                    .setValue(0, 0, 1)
                    .setValue(1, 0, 4)
                    .setValue(1, 1, 2)
                    .setValue(1, 2, 5)
                    .setValue(2, 2, 3)
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(sbm.entryNormMax(), is(5.0));
        }

        @Test
        public void test_行列ベクトル積() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            double[] expected = { 9, 23, 19 };
            Vector result = sbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_成分の検証() {

            double[][] entries = { { 1, 4, 0 }, { 4, 2, 5 }, { 0, 5, 3 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sbm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 帯の退化に関する {

        private BandMatrix sbm;

        @Before
        public void before() {
            /*
                1 4 0
                4 2 5
                0 5 3
             */
            sbm = SymmetricBandMatrixBuilder.zeroBuilder(BandMatrixDimension.symmetric(3, 0))
                    .setValue(0, 0, 1)
                    .setValue(1, 1, 2)
                    .setValue(2, 2, 3)
                    .build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(sbm.entryNormMax(), is(3.0));
        }

        @Test
        public void test_行列ベクトル積が可能() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            sbm.operate(right);
        }
    }

    public static class fromBandMatrixに関する {

        private static class WrappedMatrix extends SkeletalMatrix implements BandMatrix, Symmetric {

            private final BandMatrix mx;

            WrappedMatrix(BandMatrix src) {
                if (!(src instanceof Symmetric)) {
                    throw new AssertionError("対称行列でない");
                }
                this.mx = Objects.requireNonNull(src);
            }

            @Override
            public Vector operate(Vector operand) {
                return mx.operate(operand);
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return mx.operateTranspose(operand);
            }

            @Override
            public BandMatrixDimension bandMatrixDimension() {
                return mx.bandMatrixDimension();
            }

            @Override
            public double valueAt(int row, int column) {
                return mx.valueAt(row, column);
            }

            @Override
            public double entryNormMax() {
                return mx.entryNormMax();
            }
        }

        @Test
        public void test_成分の検証() {
            /*
                1 4 0
                4 2 5
                0 5 3
             */
            BandMatrix sbm = SymmetricBandMatrixBuilder.from(
                    new WrappedMatrix(
                            SymmetricBandMatrixBuilder.zeroBuilder(BandMatrixDimension.symmetric(3, 1))
                                    .setValue(0, 0, 1)
                                    .setValue(1, 0, 4)
                                    .setValue(1, 1, 2)
                                    .setValue(1, 2, 5)
                                    .setValue(2, 2, 3)
                                    .build()))
                    .build();

            double[][] entries = { { 1, 4, 0 }, { 4, 2, 5 }, { 0, 5, 3 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sbm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }
}
