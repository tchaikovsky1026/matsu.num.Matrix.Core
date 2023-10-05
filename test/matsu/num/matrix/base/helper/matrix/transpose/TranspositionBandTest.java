package matsu.num.matrix.base.helper.matrix.transpose;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.GeneralBandMatrixBuilder;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.SymmetricBandMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;

/**
 * {@link TranspositionBand}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class TranspositionBandTest {

    public static class 非対称帯行列に関する転置テスト {

        private BandMatrix originalMatrix;
        private BandMatrix transposedMatrix;
        private BandMatrix transposedTransposedMatrix;

        @Before
        public void before_サイズ4_下2_上1の行列の作成_と_転置() {
            BandMatrixDimension dimension = BandMatrixDimension.of(4, 2, 1);
            originalMatrix = GeneralBandMatrixBuilder.zeroBuilder(dimension)
                    .setValue(0, 0, 1).setValue(0, 1, 2)
                    .setValue(1, 0, 5).setValue(1, 1, 6).setValue(1, 2, 7)
                    .setValue(2, 0, 9).setValue(2, 1, 10).setValue(2, 2, 11).setValue(2, 3, 12)
                    .setValue(3, 1, 13).setValue(3, 2, 14).setValue(3, 3, 15)
                    .build();
            transposedMatrix = TranspositionBand.instance().apply(originalMatrix);
            transposedTransposedMatrix = TranspositionBand.instance().apply(transposedMatrix);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }

        @Test
        public void test_transposedTransposed_operate_は_original_operate_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedTransposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposedTransposed_operateTranspose_は_original_operateTranspose_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedTransposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }
    }

    public static class 対称な帯行列に関する転置テスト {

        private BandMatrix originalMatrix;
        private BandMatrix transposedMatrix;

        @Before
        public void before_サイズ4_帯2の行列の作成_と_転置() {
            BandMatrixDimension dimension = BandMatrixDimension.symmetric(4, 2);
            originalMatrix = SymmetricBandMatrixBuilder.zeroBuilder(dimension)
                    .setValue(0, 0, 1)
                    .setValue(1, 0, 5).setValue(1, 1, 6)
                    .setValue(2, 0, 9).setValue(2, 1, 10).setValue(2, 2, 11)
                    .setValue(3, 1, 13).setValue(3, 2, 14).setValue(3, 3, 15)
                    .build();
            transposedMatrix = TranspositionBand.instance().apply(originalMatrix);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
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
