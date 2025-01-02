package matsu.num.matrix.core.block;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.PermutationMatrix;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link BlockDiagonalOrthogonalMatrixImpl} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class BlockDiagonalOrthogonalMatrixImplTest {

    public static final Class<?> TEST_CLASS = BlockDiagonalOrthogonalMatrixImpl.class;

    public static class 生成に関するテスト {

        @Test
        public void test_単一行列ならそれ自身を返す() {
            UnitMatrix mx = UnitMatrix.matrixOf(MatrixDimension.square(3));

            assertThat(BlockDiagonalOrthogonalMatrixImpl.matrixOf(mx) == mx, is(true));
        }

        @Test
        public void test_次元の検証() {
            OrthogonalMatrix blockMx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(
                    UnitMatrix.matrixOf(MatrixDimension.square(3)),
                    UnitMatrix.matrixOf(MatrixDimension.square(2)));

            assertThat(blockMx.matrixDimension(), is(MatrixDimension.square(5)));
        }

        @Test
        public void test_ブロックの入れ子は展開される() {
            UnitMatrix mx = UnitMatrix.matrixOf(MatrixDimension.square(3));
            OrthogonalMatrix blockMx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(mx, mx);
            OrthogonalMatrix superBlockMx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(blockMx, blockMx, mx);

            assertThat(superBlockMx instanceof BlockDiagonalOrthogonalMatrix, is(true));
            if (superBlockMx instanceof BlockDiagonalOrthogonalMatrix casted) {
                assertThat(casted.toSeries().size(), is(5));
            }
        }
    }

    public static class 行列積に関するテスト {

        private MatrixDimension dim1;
        private PermutationMatrix mx1;

        private MatrixDimension dim2;
        private PermutationMatrix mx2;

        private MatrixDimension dim3;
        private PermutationMatrix mx3;

        @Before
        public void before_1番目の準備() {
            /*
             * 3次正方
             * 列の1番目と2番目を入れ替え
             * 
             * (0,1,2)を右から演算すると (0,2,1)
             * (0,1,2)を左から演算すると (0,2,1)
             */
            dim1 = MatrixDimension.square(3);

            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(dim1);
            builder.swapColumns(1, 2);
            mx1 = builder.build();
        }

        @Before
        public void before_2番目の準備() {
            /*
             * 2次正方
             * 列の0番目と1番目を入れ替え
             * 
             * (0,1)を右から演算すると (1,0)
             * (0,1)を左から演算すると (1,0)
             */
            dim2 = MatrixDimension.square(2);

            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(dim2);
            builder.swapColumns(0, 1);
            mx2 = builder.build();
        }

        @Before
        public void before_3番目の準備() {
            /*
             * 3次正方
             * 列の0番目と1番目を入れ替え
             * 列の1番目と2番目を入れ替え
             * 
             * (0,1,2)を右から演算すると (2,0,1)
             * (0,1,2)を左から演算すると (1,2,0)
             */
            dim3 = MatrixDimension.square(3);

            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(dim3);
            builder.swapColumns(0, 1);
            builder.swapColumns(1, 2);
            mx3 = builder.build();
        }

        @Test
        public void test_右から乗算() {
            OrthogonalMatrix blockMx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(mx1, mx2, mx3);

            Vector right = refVector(blockMx.matrixDimension().rightOperableVectorDimension());
            double[] expected = {
                    0, 2, 1,
                    4, 3,
                    7, 5, 6
            };

            assertThat(Arrays.equals(blockMx.operate(right).entryAsArray(), expected), is(true));
        }

        @Test
        public void test_左から乗算() {
            OrthogonalMatrix blockMx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(mx1, mx2, mx3);

            Vector right = refVector(blockMx.matrixDimension().rightOperableVectorDimension());
            double[] expected = {
                    0, 2, 1,
                    4, 3,
                    6, 7, 5
            };

            assertThat(Arrays.equals(blockMx.operateTranspose(right).entryAsArray(), expected), is(true));
        }

        private Vector refVector(VectorDimension dim) {
            double[] vecEntry = new double[dim.intValue()];
            for (int i = 0; i < vecEntry.length; i++) {
                vecEntry[i] = i;
            }

            Vector.Builder builder = Vector.Builder.zeroBuilder(dim);
            builder.setEntryValue(vecEntry);
            return builder.build();
        }
    }

    public static class toString表示 {

        private OrthogonalMatrix mx;

        @Before
        public void before() {
            mx = BlockDiagonalOrthogonalMatrixImpl.matrixOf(
                    UnitMatrix.matrixOf(MatrixDimension.square(3)),
                    UnitMatrix.matrixOf(MatrixDimension.square(2)));
        }

        @Test
        public void test_文字列の表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(mx);
            System.out.println(mx.transpose());
            System.out.println(mx.inverse().get());
            System.out.println(mx.transpose().transpose());
            System.out.println(mx.inverse().get().inverse().get());
            System.out.println();
        }
    }
}
