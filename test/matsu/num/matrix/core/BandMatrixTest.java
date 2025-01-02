package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link BandMatrix}インターフェースのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class BandMatrixTest {

    public static class 非対称帯行列に関する転置テスト {

        private BandMatrix originalMatrix;
        private BandMatrix transposedMatrix;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 4, 7, 11 });
            right = builder.build();
        }

        @Before
        public void before_サイズ4_下2_上1の行列の作成_と_転置() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 2, 1);
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(dimension);
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 5);
            builder.setValue(1, 1, 6);
            builder.setValue(1, 2, 7);
            builder.setValue(2, 0, 9);
            builder.setValue(2, 1, 10);
            builder.setValue(2, 2, 11);
            builder.setValue(2, 3, 12);
            builder.setValue(3, 1, 13);
            builder.setValue(3, 2, 14);
            builder.setValue(3, 3, 15);
            originalMatrix = builder.build();
            transposedMatrix = BandMatrix.createTransposedOf(originalMatrix);
        }

        @Test
        public void test_次元の検証() {
            assertThat(transposedMatrix.bandMatrixDimension(), is(originalMatrix.bandMatrixDimension().transpose()));
        }

        @Test
        public void test_成分が等しい() {
            int transposedRow = transposedMatrix.matrixDimension().rowAsIntValue();
            int transposedColumn = transposedMatrix.matrixDimension().columnAsIntValue();
            for (int j = 0; j < transposedRow; j++) {
                for (int k = 0; k < transposedColumn; k++) {
                    assertThat(transposedMatrix.valueAt(j, k), is(originalMatrix.valueAt(k, j)));
                }
            }
        }

        @Test
        public void test_transposed_operateTranspose_は_original_operate_に等しい() {
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            assertThat(
                    transposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }
    }

    public static class 対称な帯行列に関する転置テスト {

        private BandMatrix originalMatrix;
        private BandMatrix transposedMatrix;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 4, 7, 11 });
            right = builder.build();
        }

        @Before
        public void before_サイズ4_帯2の行列の作成_と_転置() {
            BandMatrixDimension dimension = BandMatrixDimension.symmetric(4, 2);
            SymmetricBandMatrix.Builder builder = SymmetricBandMatrix.Builder.zero(dimension);
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 5);
            builder.setValue(1, 1, 6);
            builder.setValue(2, 0, 9);
            builder.setValue(2, 1, 10);
            builder.setValue(2, 2, 11);
            builder.setValue(3, 1, 13);
            builder.setValue(3, 2, 14);
            builder.setValue(3, 3, 15);
            originalMatrix = builder.build();
            transposedMatrix = BandMatrix.createTransposedOf(originalMatrix);
        }

        @Test
        public void test_次元の検証() {
            assertThat(transposedMatrix.bandMatrixDimension(), is(originalMatrix.bandMatrixDimension().transpose()));
        }

        @Test
        public void test_成分が等しい() {
            int transposedRow = transposedMatrix.matrixDimension().rowAsIntValue();
            int transposedColumn = transposedMatrix.matrixDimension().columnAsIntValue();
            for (int j = 0; j < transposedRow; j++) {
                for (int k = 0; k < transposedColumn; k++) {
                    assertThat(transposedMatrix.valueAt(j, k), is(originalMatrix.valueAt(k, j)));
                }
            }
        }

        @Test
        public void test_transposed_operateTranspose_は_original_operate_に等しい() {
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            assertThat(
                    transposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }

        @Test
        public void test_転置しても対称である() {
            assertThat(transposedMatrix instanceof Symmetric, is(true));
        }
    }
}
