package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * {@link SymmetricMatrix} クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class SymmetricMatrixTest {

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形次元でMFMEx() {
            SymmetricMatrix.Builder.zero(MatrixDimension.rectangle(3, 2));
        }
    }

    public static class 成分の評価に関する {

        private EntryReadableMatrix sm;

        @Before
        public void before_サイズ3_成分1_2_3_4_5_6の対称行列を生成() {
            /*
             * 1 2 4
             * 2 3 5
             * 4 5 6
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(0, 2, 4);
            builder.setValue(1, 2, 5);
            builder.setValue(2, 2, 6);
            sm = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 2, 4 }, { 2, 3, 5 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 行列ベクトル積 {

        private EntryReadableMatrix sm;

        @Before
        public void before_サイズ3_成分1_2_3_4_5_6の対称行列を生成() {
            /*
             * 1 2 4
             * 2 3 5
             * 4 5 6
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(0, 2, 4);
            builder.setValue(1, 2, 5);
            builder.setValue(2, 2, 6);
            sm = builder.build();
        }

        @Test
        public void test_行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            Vector right = builder.build();

            double[] expected = { 17, 23, 32 };
            Vector result = sm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 行の入れ替えに関する {

        private EntryReadableMatrix sm;

        @Before
        public void before_サイズ5_成分1__15の対称行列を生成し_1と3を入れ替え() {
            /*
             * 1 7 4 2 11
             * 7 10 9 8 14
             * 4 9 6 5 12
             * 2 8 5 3 12
             * 11 14 13 12 15
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(5));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(2, 0, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(2, 2, 6);
            builder.setValue(3, 0, 7);
            builder.setValue(3, 1, 8);
            builder.setValue(3, 2, 9);
            builder.setValue(3, 3, 10);
            builder.setValue(4, 0, 11);
            builder.setValue(4, 1, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(4, 3, 14);
            builder.setValue(4, 4, 15);
            builder.swapRowsAndColumns(1, 3);
            sm = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = {
                    { 1, 7, 4, 2, 11 },
                    { 7, 10, 9, 8, 14 },
                    { 4, 9, 6, 5, 13 },
                    { 2, 8, 5, 3, 12 },
                    { 11, 14, 13, 12, 15 }

            };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 単位行列の生成 {

        private EntryReadableMatrix sm;

        @Before
        public void before_サイズ3の単位行列を生成() {
            sm = SymmetricMatrix.Builder.unit(MatrixDimension.square(3)).build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class fromMatrixに関する {

        private static class WrappedMatrix extends SkeletalMatrix implements Matrix, Symmetric {

            private final Matrix mx;

            WrappedMatrix(Matrix src) {
                if (!(src instanceof Symmetric)) {
                    throw new AssertionError("対称行列でない");
                }
                this.mx = Objects.requireNonNull(src);
            }

            @Override
            public MatrixDimension matrixDimension() {
                return mx.matrixDimension();
            }

            @Override
            public Vector operate(Vector operand) {
                return mx.operate(operand);
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return mx.operateTranspose(operand);
            }

        }

        @Test
        public void test_成分の検証() {
            /*
             * 1 2 4
             * 2 3 5
             * 4 5 6
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(0, 2, 4);
            builder.setValue(1, 2, 5);
            builder.setValue(2, 2, 6);
            EntryReadableMatrix sm = SymmetricMatrix.Builder.from(new WrappedMatrix(builder.build())).build();

            double[][] entries = { { 1, 2, 4 }, { 2, 3, 5 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class fromEntryReadableに関する {

        private static class WrappedMatrix extends SkeletalMatrix implements EntryReadableMatrix, Symmetric {

            private final EntryReadableMatrix mx;

            WrappedMatrix(EntryReadableMatrix src) {
                if (!(src instanceof Symmetric)) {
                    throw new AssertionError("対称行列でない");
                }
                this.mx = Objects.requireNonNull(src);
            }

            @Override
            public MatrixDimension matrixDimension() {
                return mx.matrixDimension();
            }

            @Override
            public Vector operate(Vector operand) {
                return mx.operate(operand);
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return mx.operateTranspose(operand);
            }

            @Override
            public double valueAt(int row, int column) {
                return mx.valueAt(row, column);
            }

            @Override
            public double entryNormMax() {
                return mx.entryNormMax();
            }
        }

        @Test
        public void test_成分の検証() {
            /*
             * 1 2 4
             * 2 3 5
             * 4 5 6
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(0, 2, 4);
            builder.setValue(1, 2, 5);
            builder.setValue(2, 2, 6);
            EntryReadableMatrix sm = SymmetricMatrix.Builder.from(new WrappedMatrix(builder.build())).build();

            double[][] entries = { { 1, 2, 4 }, { 2, 3, 5 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            sm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class Matrixの骨格実装のテストを兼ねる_対称行列バージョン {

        private Matrix original;

        @Before
        public void before_行列生成() {
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(0, 2, 4);
            builder.setValue(1, 2, 5);
            builder.setValue(2, 2, 6);
            original = builder.build();
        }

        @Test
        public void test_転置の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.transpose(), is(original.transpose()));
            }

        }

        @Test
        public void test_転置の転置の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.transpose().transpose(), is(original.transpose().transpose()));
            }

        }

        @Test
        public void test_転置の転置は自身と同一() {
            if (original instanceof SkeletalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.transpose().transpose(), is(original));
            }

        }
    }
}
