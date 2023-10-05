package matsu.num.matrix.base.helper.matrix.transpose;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.GeneralMatrixBuilder;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.SymmetricMatrixBuilder;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;

/**
 * {@link TranspositionEntryReadable}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class TranspositionEntryReadableTest {

    public static class 長方形行列に関する転置テスト {

        private EntryReadableMatrix originalMatrix;
        private EntryReadableMatrix transposedMatrix;
        private EntryReadableMatrix transposedTransposedMatrix;

        @Before
        public void before_縦3横4の行列の作成_と_転置() {
            MatrixDimension dimension = MatrixDimension.rectangle(3, 4);
            originalMatrix = GeneralMatrixBuilder.zeroBuilder(dimension)
                    .setValue(0, 0, 1).setValue(0, 1, 2).setValue(0, 2, 3).setValue(0, 3, 4)
                    .setValue(1, 0, 5).setValue(1, 1, 6).setValue(1, 2, 7).setValue(1, 3, 8)
                    .setValue(2, 0, 9).setValue(2, 1, 10).setValue(2, 2, 11).setValue(2, 3, 12)
                    .build();
            transposedMatrix = TranspositionEntryReadable.instance().apply(originalMatrix);
            transposedTransposedMatrix = TranspositionEntryReadable.instance().apply(transposedMatrix);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 4, 7 })
                    .build();
            assertThat(
                    transposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }

        @Test
        public void test_transposedTrabsposed_operate_は_original_operate_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1, 4, 7, 11 })
                    .build();
            assertThat(
                    transposedTransposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposedTrabsposed_operateTranspose_は_original_operateTranspose_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 4, 7 })
                    .build();
            assertThat(
                    transposedTransposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }

    }

    public static class 対称行列に関する転置テスト {

        private EntryReadableMatrix originalMatrix;
        private EntryReadableMatrix transposedMatrix;

        @Before
        public void before_サイズ3の行列の作成_と_転置() {
            MatrixDimension dimension = MatrixDimension.square(3);
            originalMatrix = SymmetricMatrixBuilder.zeroBuilder(dimension)
                    .setValue(0, 0, 1)
                    .setValue(1, 0, 5).setValue(1, 1, 6)
                    .setValue(2, 0, 9).setValue(2, 1, 10).setValue(2, 2, 11)
                    .build();
            transposedMatrix = TranspositionEntryReadable.instance().apply(originalMatrix);
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
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 4, 7 })
                    .build();
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 4, 7 })
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
