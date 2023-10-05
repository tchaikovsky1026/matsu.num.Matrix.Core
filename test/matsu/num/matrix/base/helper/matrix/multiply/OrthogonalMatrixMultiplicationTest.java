package matsu.num.matrix.base.helper.matrix.multiply;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.PermutationMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * {@link OrthogonalMatrixMultiplication}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class OrthogonalMatrixMultiplicationTest {

    public static class 行列積のテスト {

        private final MatrixDimension matrixDimension = MatrixDimension.square(4);

        private OrthogonalMatrix m1;
        private OrthogonalMatrix m2;
        private OrthogonalMatrix m3;
        private OrthogonalMatrix m4;

        private OrthogonalMatrix m1To4;

        private Vector vec_4;

        @Before
        public void before_行列の準備() {
            m1 = PermutationMatrix.Builder.unitBuilder(matrixDimension)
                    .swapRows(0, 3)
                    .swapRows(3, 1)
                    .build();
            m2 = PermutationMatrix.Builder.unitBuilder(matrixDimension)
                    .swapRows(1, 2)
                    .build();
            m3 = PermutationMatrix.Builder.unitBuilder(matrixDimension)
                    .swapRows(2, 3)
                    .swapRows(1, 2)
                    .build();
            m4 = PermutationMatrix.Builder.unitBuilder(matrixDimension)
                    .swapRows(2, 3)
                    .swapRows(0, 1)
                    .build();
            m1To4 = OrthogonalMatrixMultiplication.instance().apply(
                    m1, m2, m3, m4);
        }

        @Before
        public void before_ベクトルの準備() {
            vec_4 = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4))
                    .setEntryValue(new double[] { 1.3, 4.1, 5.3, 2.4 })
                    .build();
        }

        @Test
        public void test_行列積のサイズは4_4() {
            assertThat(m1To4.matrixDimension(), is(matrixDimension));
        }

        @Test
        public void test_行列積のテスト() {
            doubleArrayEqual(
                    m1To4.operate(vec_4).entryAsArray(),
                    m1.operate(m2.operate(m3.operate(m4.operate(vec_4)))).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    m4.operateTranspose(m3.operateTranspose(m2.operateTranspose(m1.operateTranspose(vec_4))))
                            .entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_前() {
            OrthogonalMatrix multi = OrthogonalMatrixMultiplication.instance().apply(
                    OrthogonalMatrixMultiplication.instance().apply(m1, m2),
                    m3,
                    m4);
            doubleArrayEqual(
                    multi.operate(vec_4).entryAsArray(),
                    m1To4.operate(vec_4).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_中() {
            OrthogonalMatrix multi = OrthogonalMatrixMultiplication.instance().apply(
                    m1,
                    OrthogonalMatrixMultiplication.instance().apply(m2, m3),
                    m4);
            doubleArrayEqual(
                    multi.operate(vec_4).entryAsArray(),
                    m1To4.operate(vec_4).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_後() {
            OrthogonalMatrix multi = OrthogonalMatrixMultiplication.instance().apply(
                    m1,
                    m2,
                    OrthogonalMatrixMultiplication.instance().apply(m3, m4));
            doubleArrayEqual(
                    multi.operate(vec_4).entryAsArray(),
                    m1To4.operate(vec_4).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_次元が整合しなければMFMEx() {
            OrthogonalMatrixMultiplication.instance()
                    .apply(m1, m2, PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(1)).build());
        }

        @Test
        public void test_インスタンスの同一性() {
            //このテストは実装の詳細に依存している

            //逆行列は同一
            assertThat(m1To4.inverse() == m1To4.inverse(), is(true));

            //逆行列の逆行列は自分自身と同一
            assertThat(m1To4.inverse().get().inverse().get(), is(m1To4));
        }
    }

    public static class toStrinh表示 {

        private final MatrixDimension matrixDimension = MatrixDimension.square(4);

        private OrthogonalMatrix m1;
        private OrthogonalMatrix m2;
        private OrthogonalMatrix m3;
        private OrthogonalMatrix m4;

        @Before
        public void before_行列の準備() {
            m1 = PermutationMatrix.Builder.unitBuilder(matrixDimension).build();
            m2 = PermutationMatrix.Builder.unitBuilder(matrixDimension).build();
            m3 = PermutationMatrix.Builder.unitBuilder(matrixDimension).build();
            m4 = PermutationMatrix.Builder.unitBuilder(matrixDimension).build();
        }

        @Test
        public void test_toString表示() {
            System.out.println(OrthogonalMatrixMultiplication.class.getSimpleName() + ":");

            //4積
            OrthogonalMatrix multi_1 = OrthogonalMatrixMultiplication.instance().apply(m1, m2, m3, m4);
            System.out.println(multi_1);
            System.out.println(multi_1.inverse().get());

            //4積,逐次
            OrthogonalMatrix multi_2 = OrthogonalMatrixMultiplication.instance().apply(
                    m1, m2,
                    OrthogonalMatrixMultiplication.instance().apply(m3, m4));
            System.out.println(multi_2);
            System.out.println(multi_2.inverse().get());

            System.out.println();
        }
    }

    private static void doubleArrayEqual(double[] result, double[] expected, double accept) {
        assertThat(result.length, is(expected.length));
        for (int i = 0; i < result.length; i++) {
            assertThat(result[i], is(closeTo(expected[i], 1E-12)));
        }
    }

}
