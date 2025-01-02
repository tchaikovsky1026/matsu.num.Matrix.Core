package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link SignatureMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class SignatureMatrixTest {

    public static final Class<?> TEST_CLASS = SignatureMatrix.class;

    public static class ビルダの生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は生成できない() {
            SignatureMatrix.Builder.unit(MatrixDimension.rectangle(2, 3));
        }
    }

    public static class ビルダの振る舞いに関する {

        private final MatrixDimension matrixDimension = MatrixDimension.square(5);
        private SignatureMatrix.Builder builder;

        @Before
        public void before_ビルダの初期を生成する() {
            builder = SignatureMatrix.Builder.unit(matrixDimension);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_setNegative_範囲外_IOOBEx() {
            builder.setNegativeAt(5);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_setPositive_範囲外_IOOBEx() {
            builder.setPositiveAt(5);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_reverse_範囲外_IOOBEx() {
            builder.reverseAt(5);
        }

        @Test
        public void test_setNegative() {
            builder.setNegativeAt(3);
            SignatureMatrix m = builder.build();

            double[] expected = { 1, 1, 1, -1, 1 };
            for (int i = 0; i < expected.length; i++) {
                assertThat(m.valueAt(i, i), is(expected[i]));
            }
            assertThat(m.isEven(), is(false));
        }

        @Test
        public void test_setPositive() {
            builder.setNegativeAt(3);
            builder.setNegativeAt(3);
            builder.setPositiveAt(3);
            SignatureMatrix m = builder.build();

            double[] expected = { 1, 1, 1, 1, 1 };
            for (int i = 0; i < expected.length; i++) {
                assertThat(m.valueAt(i, i), is(expected[i]));
            }
            assertThat(m.isEven(), is(true));
        }

        @Test
        public void test_reverse() {
            builder.reverseAt(3);
            builder.reverseAt(2);
            builder.reverseAt(3);
            SignatureMatrix m = builder.build();

            double[] expected = { 1, 1, -1, 1, 1 };
            for (int i = 0; i < expected.length; i++) {
                assertThat(m.valueAt(i, i), is(expected[i]));
            }
            assertThat(m.isEven(), is(false));
        }
    }

    public static class 成分値と行列ベクトル積のテスト {

        private final MatrixDimension matrixDimension = MatrixDimension.square(5);
        private SignatureMatrix m;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {
            var vectorDimension = matrixDimension.rightOperableVectorDimension();
            Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);

            for (int i = 0; i < vectorDimension.intValue(); i++) {
                builder.setValue(i, i + 1);
            }
            right = builder.build();
        }

        @Before
        public void before_符号行列() {
            /*
             * 1 1 -1 -1 1
             */
            var builder = SignatureMatrix.Builder.unit(matrixDimension);
            builder.reverseAt(2);
            builder.reverseAt(3);
            m = builder.build();
        }

        @Test
        public void test_成分評価() {
            double[][] entries = {
                    { 1, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0 },
                    { 0, 0, -1, 0, 0 },
                    { 0, 0, 0, -1, 0 },
                    { 0, 0, 0, 0, 1 }
            };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            m.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_右から1_2_3_4_5を乗算() {

            double[] result = m.operate(right).entryAsArray();
            double[] expected = { 1, 2, -3, -4, 5 };

            for (int i = 0; i < expected.length; i++) {
                assertThat(
                        String.format("i=%d", i),
                        result[i], is(expected[i]));
            }
        }
    }

    public static class toString表示 {

        private SignatureMatrix m;

        @Before
        public void before() {
            var builder = SignatureMatrix.Builder.unit(MatrixDimension.square(3));
            builder.reverseAt(0);
            m = builder.build();
        }

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(m);
            System.out.println(m.inverse().get());
            System.out.println();
        }
    }
}
