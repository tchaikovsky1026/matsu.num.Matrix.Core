package matsu.num.matrix.base.helper.matrix.multiply;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.GeneralMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.SymmetricMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;

/**
 * {@link MatrixMultiplication}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class MatrixMultiplicationTest {

    public static class 行列積のテスト {
        private Matrix m1;
        private Matrix m2;
        private Matrix m3;
        private Matrix m4;

        private Matrix m1To4;

        private Vector vec_6;
        private Vector vec_4;

        @Before
        public void before_行列の準備() {
            GeneralMatrix.Builder builder_m1 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(4, 2));
            builder_m1.setValue(0, 0, 2.2);
            builder_m1.setValue(2, 1, 3.1);
            m1 = builder_m1.build();

            GeneralMatrix.Builder builder_m2 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder_m2.setValue(0, 1, 0.1);
            builder_m2.setValue(1, 0, 2.1);
            m2 = builder_m2.build();

            GeneralMatrix.Builder builder_m3 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 5));
            builder_m3.setValue(2, 4, 1.2);
            builder_m3.setValue(0, 3, 0.8);
            m3 = builder_m3.build();

            GeneralMatrix.Builder builder_m4 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(5, 6));
            builder_m4.setValue(4, 1, 2.5);
            builder_m4.setValue(3, 5, 0.7);
            m4 = builder_m4.build();

            m1To4 = MatrixMultiplication.instance().apply(
                    m1, m2, m3, m4);
        }

        @Before
        public void before_ベクトルの準備() {
            Vector.Builder builder_6 = Vector.Builder.zeroBuilder(VectorDimension.valueOf(6));
            builder_6.setEntryValue(new double[] { 1.3, 4.1, 5.3, 2.4, 3.3, 0.2 });
            vec_6 = builder_6.build();

            Vector.Builder builder_4 = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder_4.setEntryValue(new double[] { 1.3, 4.1, 5.3, 2.4 });
            vec_4 = builder_4.build();
        }

        @Test
        public void test_行列積のサイズは4_6() {
            assertThat(m1To4.matrixDimension(), is(MatrixDimension.rectangle(4, 6)));
        }

        @Test
        public void test_行列積のテスト() {
            doubleArrayEqual(
                    m1To4.operate(vec_6).entryAsArray(),
                    m1.operate(m2.operate(m3.operate(m4.operate(vec_6)))).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    m4.operateTranspose(m3.operateTranspose(m2.operateTranspose(m1.operateTranspose(vec_4))))
                            .entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_前() {
            Matrix multi = MatrixMultiplication.instance().apply(
                    MatrixMultiplication.instance().apply(m1, m2),
                    m3,
                    m4);
            doubleArrayEqual(
                    multi.operate(vec_6).entryAsArray(),
                    m1To4.operate(vec_6).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_中() {
            Matrix multi = MatrixMultiplication.instance().apply(
                    m1,
                    MatrixMultiplication.instance().apply(m2, m3),
                    m4);
            doubleArrayEqual(
                    multi.operate(vec_6).entryAsArray(),
                    m1To4.operate(vec_6).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }

        @Test
        public void test_逐次組み合わせのテスト_後() {
            Matrix multi = MatrixMultiplication.instance().apply(
                    m1,
                    m2,
                    MatrixMultiplication.instance().apply(m3, m4));
            doubleArrayEqual(
                    multi.operate(vec_6).entryAsArray(),
                    m1To4.operate(vec_6).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    multi.operateTranspose(vec_4).entryAsArray(),
                    m1To4.operateTranspose(vec_4).entryAsArray(),
                    1E-12);
        }
    }

    public static class 対称行列積のテスト {
        private Matrix mxL;
        private Matrix mxD;
        private Matrix symmMulti;

        private Vector vec_4;

        @Before
        public void before_行列の準備() {
            GeneralMatrix.Builder lBuilder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(4, 2));
            lBuilder.setValue(0, 0, 2.2);
            lBuilder.setValue(2, 1, 3.1);
            mxL = lBuilder.build();

            DiagonalMatrix.Builder dBuilder = DiagonalMatrix.Builder.zeroBuilder(MatrixDimension.square(2));
            dBuilder.setValue(0, 2.3);
            dBuilder.setValue(1, 0.7);
            mxD = dBuilder.build();
            symmMulti = MatrixMultiplication.instance().symmetricMultiply(mxD, mxL);
        }

        @Before
        public void before_ベクトルの準備() {

            Vector.Builder builder_4 = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder_4.setEntryValue(new double[] { 1.3, 4.1, 5.3, 2.4 });
            vec_4 = builder_4.build();
        }

        @Test
        public void test_サイズは4_4() {
            assertThat(symmMulti.matrixDimension(), is(MatrixDimension.square(4)));
        }

        @Test
        public void test_行列積のテスト() {
            doubleArrayEqual(
                    symmMulti.operate(vec_4).entryAsArray(),
                    mxL.operate(mxD.operate(mxL.operateTranspose(vec_4))).entryAsArray(),
                    1E-12);
            doubleArrayEqual(
                    symmMulti.operateTranspose(vec_4).entryAsArray(),
                    symmMulti.operate(vec_4).entryAsArray(),
                    1E-12);
        }
    }

    public static class toString表示 {
        private Matrix m1;
        private Matrix m2;
        private Matrix m3;
        private Matrix m4;

        private Matrix symm_2;

        @Before
        public void before_行列の準備() {
            m1 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(4, 2)).build();
            m2 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3)).build();
            m3 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 5)).build();
            m4 = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(5, 6)).build();

            symm_2 = SymmetricMatrix.Builder.unit(MatrixDimension.square(2)).build();
        }

        @Test
        public void test_toString表示() {

            System.out.println(MatrixMultiplication.class.getName() + ":");
            //4積
            Matrix multi = MatrixMultiplication.instance().apply(m1, m2, m3, m4);
            System.out.println(multi);
            System.out.println(multi.transpose());

            //4積(分割)
            Matrix multi2 =
                    MatrixMultiplication.instance().apply(MatrixMultiplication.instance().apply(m1, m2), m3, m4);
            System.out.println(multi2);
            System.out.println(multi2.transpose());

            //対称化積
            Matrix multiSymm = MatrixMultiplication.instance().symmetricMultiply(symm_2, m1);
            System.out.println(multiSymm);
            System.out.println(multiSymm.transpose());

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
