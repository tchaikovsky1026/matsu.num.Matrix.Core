package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * {@link OrthogonalMatrix}インターフェースのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class OrthogonalMatrixTest {

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
            PermutationMatrix.Builder builder_m1 = PermutationMatrix.Builder.unitBuilder(matrixDimension);
            builder_m1.swapRows(0, 3);
            builder_m1.swapRows(3, 1);
            m1 = builder_m1.build();

            PermutationMatrix.Builder builder_m2 = PermutationMatrix.Builder.unitBuilder(matrixDimension);
            builder_m2.swapRows(1, 2);
            m2 = builder_m2.build();

            PermutationMatrix.Builder builder_m3 = PermutationMatrix.Builder.unitBuilder(matrixDimension);
            builder_m3.swapRows(2, 3);
            builder_m3.swapRows(1, 2);
            m3 = builder_m3.build();

            PermutationMatrix.Builder builder_m4 = PermutationMatrix.Builder.unitBuilder(matrixDimension);
            builder_m4.swapRows(2, 3);
            builder_m4.swapRows(0, 1);
            m4 = builder_m4.build();

            m1To4 = OrthogonalMatrix.multiply(m1, m2, m3, m4);
        }

        @Before
        public void before_ベクトルの準備() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(new double[] { 1.3, 4.1, 5.3, 2.4 });
            vec_4 = builder.build();
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
            OrthogonalMatrix multi = OrthogonalMatrix.multiply(
                    OrthogonalMatrix.multiply(m1, m2),
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
            OrthogonalMatrix multi = OrthogonalMatrix.multiply(
                    m1,
                    OrthogonalMatrix.multiply(m2, m3),
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
            OrthogonalMatrix multi = OrthogonalMatrix.multiply(
                    m1,
                    m2,
                    OrthogonalMatrix.multiply(m3, m4));
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
            OrthogonalMatrix.multiply(m1, m2, PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(1)).build());
        }
    }

    private static void doubleArrayEqual(double[] result, double[] expected, double accept) {
        assertThat(result.length, is(expected.length));
        for (int i = 0; i < result.length; i++) {
            assertThat(result[i], is(closeTo(expected[i], 1E-12)));
        }
    }

}
