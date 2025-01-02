package matsu.num.matrix.core.block;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.ZeroMatrix;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link BlockMatrixStructure} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class BlockMatrixStructureTest {

    public static final Class<?> TEST_CLASS = BlockMatrixStructure.class;

    public static class ビルダのセットのテスト_2_3 {

        private final int rows = 2;
        private final int columns = 3;

        private BlockMatrixStructure.Builder<Matrix> builder;

        @Before
        public void before_ビルダ作成() {
            builder = BlockMatrixStructure.Builder.of(MatrixDimension.rectangle(rows, columns));
        }

        @Test(expected = None.class)
        public void test_範囲内に要素を入れる() {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    builder.setBlockElement(j, k, null);
                }
            }
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_範囲外に要素を入れる_r() {
            builder.setBlockElement(rows, columns - 1, null);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_範囲外に要素を入れる_c() {
            builder.setBlockElement(rows - 1, columns, null);
        }
    }

    public static class ビルダのセットのテスト_3_2 {

        private final int rows = 3;
        private final int columns = 2;

        private BlockMatrixStructure.Builder<Matrix> builder;

        @Before
        public void before_ビルダ作成() {
            builder = BlockMatrixStructure.Builder.of(MatrixDimension.rectangle(rows, columns));
        }

        @Test(expected = None.class)
        public void test_範囲内に要素を入れる() {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    builder.setBlockElement(j, k, null);
                }
            }
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_範囲外に要素を入れる_r() {
            builder.setBlockElement(rows, columns - 1, null);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_範囲外に要素を入れる_c() {
            builder.setBlockElement(rows - 1, columns, null);
        }
    }

    public static class 構造生成のテスト {

        private final MatrixDimension structureDimension = MatrixDimension.square(3);
        private BlockMatrixStructure.Builder<Matrix> builder;

        @Before
        public void before_3_3のビルダ作成() {
            builder = BlockMatrixStructure.Builder.of(structureDimension);
        }

        @Test(expected = None.class)
        public void test_最低限の要素から構造構築() {
            ZeroMatrix element = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
            builder.setBlockElement(0, 1, element);
            builder.setBlockElement(1, 2, element);
            builder.setBlockElement(2, 0, element);
            builder.build();
        }

        @Test(expected = None.class)
        public void test_複雑な成功パターン() {
            builder.setBlockElement(0, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 2)));
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(2, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(1, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 4)));
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 4)));
            builder.setBlockElement(0, 2, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 6)));
            builder.setBlockElement(1, 2, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 6)));
            builder.setBlockElement(2, 2, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 6)));
            builder.build();
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_不能な場合はMFMEx_r() {
            ZeroMatrix element = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
            builder.setBlockElement(0, 1, element);
            builder.setBlockElement(1, 2, element);
            builder.setBlockElement(1, 0, element);
            builder.build();
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_不能な場合はMFMEx_c() {
            ZeroMatrix element = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
            builder.setBlockElement(0, 1, element);
            builder.setBlockElement(1, 2, element);
            builder.setBlockElement(2, 2, element);
            builder.build();
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズミスマッチの場合はMFMEx_r() {
            ZeroMatrix element = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
            builder.setBlockElement(0, 1, element);
            builder.setBlockElement(1, 2, element);
            builder.setBlockElement(2, 0, element);
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 3)));
            builder.build();
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズミスマッチの場合はMFMEx_c() {
            ZeroMatrix element = ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 3));
            builder.setBlockElement(0, 1, element);
            builder.setBlockElement(1, 2, element);
            builder.setBlockElement(2, 0, element);
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(2, 2)));
            builder.build();
        }
    }

    public static class 生成された構造に対するテスト_1_3_5__2_4 {
        private final MatrixDimension structureDimension = MatrixDimension.rectangle(3, 2);
        private BlockMatrixStructure<Matrix> structure;

        @Before
        public void before_1_3_5__2_4の構造作成() {
            BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(structureDimension);
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 4)));
            structure = builder.build();
        }

        @Test
        public void test_ブロック要素の行列次元のテスト() {
            int[] rowStructure = { 1, 3, 5 };
            int[] columnStructure = { 2, 4 };

            for (int j = 0; j < rowStructure.length; j++) {
                for (int k = 0; k < columnStructure.length; k++) {
                    assertThat(
                            structure.elementDimensionAt(j, k),
                            is(MatrixDimension.rectangle(rowStructure[j], columnStructure[k])));
                }
            }
        }

        @Test
        public void test_行列全体の次元のテスト() {
            assertThat(structure.entireMatrixDimension(), is(MatrixDimension.rectangle(9, 6)));
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_r() {
            structure.elementDimensionAt(3, 1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_c() {
            structure.elementDimensionAt(2, 2);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_rm() {
            structure.elementDimensionAt(-1, 0);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_cm() {
            structure.elementDimensionAt(0, -1);
        }
    }

    public static class 生成された構造に対するテスト_1_3__2_4_6 {
        private final MatrixDimension structureDimension = MatrixDimension.rectangle(2, 3);
        private BlockMatrixStructure<Matrix> structure;

        @Before
        public void before_1_3__2_4_6の構造作成() {
            BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(structureDimension);
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(1, 2, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 6)));
            structure = builder.build();
        }

        @Test
        public void test_ブロック要素の行列次元のテスト() {
            int[] rowStructure = { 1, 3 };
            int[] columnStructure = { 2, 4, 6 };

            for (int j = 0; j < rowStructure.length; j++) {
                for (int k = 0; k < columnStructure.length; k++) {
                    assertThat(
                            structure.elementDimensionAt(j, k),
                            is(MatrixDimension.rectangle(rowStructure[j], columnStructure[k])));
                }
            }
        }

        @Test
        public void test_行列全体の次元のテスト() {
            assertThat(structure.entireMatrixDimension(), is(MatrixDimension.rectangle(4, 12)));
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_r() {
            structure.elementDimensionAt(2, 2);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_c() {
            structure.elementDimensionAt(1, 3);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_rm() {
            structure.elementDimensionAt(-1, 0);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_ブロック外を参照するとIOOBEx_cm() {
            structure.elementDimensionAt(0, -1);
        }
    }

    public static class toString表示 {

        private BlockMatrixStructure<Matrix> structure;

        @Before
        public void before_構造作成() {
            MatrixDimension structureDimension = MatrixDimension.rectangle(3, 2);
            BlockMatrixStructure.Builder<Matrix> builder = BlockMatrixStructure.Builder.of(structureDimension);
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 4)));
            structure = builder.build();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(structure);
            System.out.println();
        }
    }
}
