package matsu.num.matrix.base.block;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.PermutationMatrix;
import matsu.num.matrix.base.UnitMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * {@link SymmetricBlockDiagonalOrthogonalMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class SymmetricBlockDiagonalOrthogonalMatrixTest {

    public static final Class<?> TEST_CLASS = SymmetricBlockDiagonalOrthogonalMatrix.class;

    public static class 生成に関するテスト {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_firstが非対称ならMNSEx() {
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapColumns(0, 1);

            SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(builder.build());
        }

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_followingが非対称ならMNSEx() {
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapColumns(0, 1);

            SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(
                    UnitMatrix.matrixOf(MatrixDimension.square(1)), builder.build());
        }

        @Test
        public void test_単一行列ならそれ自身を返す() {
            UnitMatrix mx = UnitMatrix.matrixOf(MatrixDimension.square(3));

            assertThat(SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(mx) == mx, is(true));
        }

        @Test
        public void test_次元の検証() {
            OrthogonalMatrix blockMx = SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(
                    UnitMatrix.matrixOf(MatrixDimension.square(3)),
                    UnitMatrix.matrixOf(MatrixDimension.square(2)));

            assertThat(blockMx.matrixDimension(), is(MatrixDimension.square(5)));
        }

        @Test
        public void test_ブロックの入れ子は展開される() {
            UnitMatrix mx = UnitMatrix.matrixOf(MatrixDimension.square(3));
            OrthogonalMatrix blockMx = SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(mx, mx);
            OrthogonalMatrix superBlockMx = SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(blockMx, blockMx, mx);

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
            dim1 = MatrixDimension.square(3);
            mx1 = UnitMatrix.matrixOf(dim1);
        }

        @Before
        public void before_2番目の準備() {
            dim2 = MatrixDimension.square(2);
            mx2 = UnitMatrix.matrixOf(dim2);
        }

        @Before
        public void before_3番目の準備() {
            dim3 = MatrixDimension.square(3);
            mx3 = UnitMatrix.matrixOf(dim3);
        }

        @Test
        public void test_右から乗算() {
            OrthogonalMatrix blockMx = SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(mx1, mx2, mx3);

            Vector right = refVector(blockMx.matrixDimension().rightOperableVectorDimension());
            double[] expected = {
                    0, 1, 2,
                    3, 4,
                    5, 6, 7
            };

            assertThat(Arrays.equals(blockMx.operate(right).entryAsArray(), expected), is(true));
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
