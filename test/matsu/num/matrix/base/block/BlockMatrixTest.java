package matsu.num.matrix.base.block;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.GeneralMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.ZeroMatrix;

/**
 * {@link BlockMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class BlockMatrixTest {

    public static final Class<?> TEST_CLASS = BlockMatrix.class;

    public static class 行列ベクトル積に関するテスト {

        /**
         * 1_2_2__2_1_3_1の構造 <br>
         * <br>
         * [1, 2][3][4, 5, 6][3] <br>
         * <br>
         * [0, 0][2][0, 0, 0][0] <br>
         * [0, 0][1][0, 0, 0][0] <br>
         * <br>
         * [2, 0][0][3, 2, 1][2] <br>
         * [1, 2][0][0, 1, 2][4]
         */
        private Matrix matrix;

        @Before
        public void before_行列の作成() {

            MatrixDimension structureDimension = MatrixDimension.rectangle(3, 4);

            Matrix m00 = mx00();
            Matrix m01 = mx01();
            Matrix m02 = mx02();
            Matrix m03 = mx03();
            Matrix m11 = mx11();
            Matrix m20 = mx20();
            Matrix m22 = mx22();
            Matrix m23 = mx23();

            BlockMatrixStructure.Builder<Matrix> builder =
                    BlockMatrixStructure.builderOf(structureDimension);
            builder.setBlockElement(0, 0, m00);
            builder.setBlockElement(0, 1, m01);
            builder.setBlockElement(0, 2, m02);
            builder.setBlockElement(0, 3, m03);
            builder.setBlockElement(1, 1, m11);
            builder.setBlockElement(2, 0, m20);
            builder.setBlockElement(2, 2, m22);
            builder.setBlockElement(2, 3, m23);
            BlockMatrixStructure<Matrix> structure = builder.build();

            matrix = BlockMatrix.of(structure);
        }

        @Test
        public void test_右から単位行列をかけることで行列ベクトル積を検証する() {

            //縦ベクトルの配列を表示したいので、コードの表示上は転置に見える
            double[][] expected = {
                    { 1, 0, 0, 2, 1 },
                    { 2, 0, 0, 0, 2 },
                    { 3, 2, 1, 0, 0 },
                    { 4, 0, 0, 3, 0 },
                    { 5, 0, 0, 2, 1 },
                    { 6, 0, 0, 1, 2 },
                    { 3, 0, 0, 2, 4 }
            };

            VectorDimension operandVectorDimension = matrix.matrixDimension().rightOperableVectorDimension();
            for (int k = 0; k < expected.length; k++) {
                Vector result = matrix.operate(unitVector(operandVectorDimension, k));

                assertThat(Arrays.equals(result.entryAsArray(), expected[k]), is(true));
            }

        }

        @Test
        public void test_転置行列に対して右から単位行列をかけることで行列ベクトル積転置を検証する() {

            //縦ベクトルの配列を表示したいので、コードの表示上は転置に見える
            double[][] expected = {
                    { 1, 2, 3, 4, 5, 6, 3 },
                    { 0, 0, 2, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0 },
                    { 2, 0, 0, 3, 2, 1, 2 },
                    { 1, 2, 0, 0, 1, 2, 4 }
            };

            VectorDimension operandVectorDimension = matrix.matrixDimension().leftOperableVectorDimension();
            for (int j = 0; j < expected.length; j++) {
                Vector result = matrix.operateTranspose(unitVector(operandVectorDimension, j));

                assertThat(Arrays.equals(result.entryAsArray(), expected[j]), is(true));
            }

        }

        private static Vector unitVector(VectorDimension dimension, int index) {
            Vector.Builder builder = Vector.Builder.zeroBuilder(dimension);
            builder.setValue(index, 1d);
            return builder.build();
        }

        private static Matrix mx00() {
            /*
             * [1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);

            return builder.build();
        }

        private static Matrix mx01() {
            /*
             * [3]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 1));
            builder.setValue(0, 0, 3);

            return builder.build();
        }

        private static Matrix mx02() {
            /*
             * [4, 5, 6]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 3));
            builder.setValue(0, 0, 4);
            builder.setValue(0, 1, 5);
            builder.setValue(0, 2, 6);

            return builder.build();
        }

        private static Matrix mx03() {
            /*
             * [3]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 1));
            builder.setValue(0, 0, 3);

            return builder.build();
        }

        private static Matrix mx11() {
            /*
             * [2]
             * [1]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 1));
            builder.setValue(0, 0, 2);
            builder.setValue(1, 0, 1);

            return builder.build();
        }

        private static Matrix mx20() {
            /*
             * [2, 0]
             * [1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 2));
            builder.setValue(0, 0, 2);
            builder.setValue(0, 1, 0);
            builder.setValue(1, 0, 1);
            builder.setValue(1, 1, 2);

            return builder.build();
        }

        private static Matrix mx22() {
            /*
             * [3, 2, 1]
             * [0, 1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 3);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 1);
            builder.setValue(1, 0, 0);
            builder.setValue(1, 1, 1);
            builder.setValue(1, 2, 2);

            return builder.build();
        }

        private static Matrix mx23() {
            /*
             * [2]
             * [4]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 1));
            builder.setValue(0, 0, 2);
            builder.setValue(1, 0, 4);

            return builder.build();
        }
    }

    public static class toString表示 {

        private Matrix matrix;

        @Before
        public void before_構造作成() {
            MatrixDimension structureDimension = MatrixDimension.rectangle(3, 2);
            BlockMatrixStructure.Builder<Matrix> builder =
                    BlockMatrixStructure.builderOf(structureDimension);
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 4)));
            BlockMatrixStructure<Matrix> structure = builder.build();
            matrix = BlockMatrix.of(structure);
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(matrix);
            System.out.println();
        }
    }
}
