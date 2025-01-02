package matsu.num.matrix.core.helper.matrix.transpose;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.SymmetricMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link TranspositionUtil}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class TranspositionUtilTest {

    public static class 長方形行列に関する転置テスト {

        private Matrix originalMatrix;
        private Matrix transposedMatrix;
        private Matrix transposedTransposedMatrix;

        private Vector right3;
        private Vector right4;

        @Before
        public void before_評価用右辺ベクトル_dim4() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1, 4, 7, 11 });
            right4 = builder.build();
        }

        @Before
        public void before_評価用右辺ベクトル_dim3() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 4, 7 });
            right3 = builder.build();
        }

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
            transposedMatrix = TranspositionUtil.apply(originalMatrix);
            transposedTransposedMatrix = TranspositionUtil.apply(transposedMatrix);
        }

        @Test
        public void test_次元は4_3() {
            assertThat(transposedMatrix.matrixDimension(), is(originalMatrix.matrixDimension().transpose()));
        }

        @Test
        public void test_transposed_operateTranspose_は_original_operate_に等しい() {
            assertThat(
                    transposedMatrix.operateTranspose(right4).entryAsArray(),
                    is(originalMatrix.operate(right4).entryAsArray()));
        }

        @Test
        public void test_transposed_operate_は_original_operateTranspose_に等しい() {
            assertThat(
                    transposedMatrix.operate(right3).entryAsArray(),
                    is(originalMatrix.operateTranspose(right3).entryAsArray()));
        }

        @Test
        public void test_transposedTransposed_operate_は_original_operate_に等しい() {
            assertThat(
                    transposedTransposedMatrix.operate(right4).entryAsArray(),
                    is(originalMatrix.operate(right4).entryAsArray()));
        }

        @Test
        public void test_transposedTransposed_operateTranspose_は_original_operateTranspose_に等しい() {
            assertThat(
                    transposedTransposedMatrix.operateTranspose(right3).entryAsArray(),
                    is(originalMatrix.operateTranspose(right3).entryAsArray()));
        }

    }

    public static class 対称行列に関する転置テスト {

        private Matrix originalMatrix;
        private Matrix transposedMatrix;

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
            transposedMatrix = TranspositionUtil.apply(originalMatrix);
        }

        @Test
        public void test_次元は3() {
            assertThat(transposedMatrix.matrixDimension(), is(originalMatrix.matrixDimension().transpose()));
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
