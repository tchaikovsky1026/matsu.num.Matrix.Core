/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link EntryReadableMatrix} インターフェースのテスト.
 */
@RunWith(Enclosed.class)
final class EntryReadableMatrixTest {

    public static class 長方形行列に関する転置テスト {

        private EntryReadableMatrix originalMatrix;
        private EntryReadableMatrix transposedMatrix;

        @Before
        public void before_縦3横4の行列の作成_と_転置() {
            MatrixDimension dimension = MatrixDimension.rectangle(3, 4);
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(dimension);
            int count = 0;
            for (int j = 0; j < dimension.rowAsIntValue(); j++) {
                for (int k = 0; k < dimension.columnAsIntValue(); k++) {
                    count++;
                    builder.setValue(j, k, count);
                }
            }
            originalMatrix = builder.build();
            transposedMatrix = EntryReadableMatrix.createTransposedOf(originalMatrix);
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
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 4, 7, 11 });
            Vector right = builder.build();
            assertThat(
                    transposedMatrix.operateTranspose(right).entryAsArray(),
                    is(originalMatrix.operate(right).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 4, 7 });
            Vector right = builder.build();
            assertThat(
                    transposedMatrix.operate(right).entryAsArray(),
                    is(originalMatrix.operateTranspose(right).entryAsArray()));
        }

    }

    public static class 対称行列に関する転置テスト {

        private EntryReadableMatrix originalMatrix;
        private EntryReadableMatrix transposedMatrix;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 4, 7 });
            right = builder.build();
        }

        @Before
        public void before_サイズ3の行列の作成_と_転置() {
            MatrixDimension dimension = MatrixDimension.square(3);
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(dimension);
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 5);
            builder.setValue(1, 1, 6);
            builder.setValue(2, 0, 9);
            builder.setValue(2, 1, 10);
            builder.setValue(2, 2, 11);
            originalMatrix = builder.build();
            transposedMatrix = EntryReadableMatrix.createTransposedOf(originalMatrix);
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
