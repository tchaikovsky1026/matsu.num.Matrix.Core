package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link GeneralMatrix}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class GeneralMatrixTest {

    public static class 成分の評価に関する {

        private EntryReadableMatrix gm;

        @Before
        public void before_サイズ2_3_成分1_2_3_4_5_6の長方形行列を生成() {
            /*
             * 1 2 3
             * 4 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 5);
            builder.setValue(1, 2, 6);
            gm = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 2, 3 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 行列ベクトル積_横長 {

        private EntryReadableMatrix gm;

        @Before
        public void before_サイズ2_3_成分1_2_3_4_5_6の長方形行列を生成() {
            /*
             * 1 2 3
             * 4 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 5);
            builder.setValue(1, 2, 6);
            gm = builder.build();
        }

        @Test
        public void test_行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            Vector right = builder.build();
            double[] expected = { 14, 32 };
            Vector result = gm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(2));
            builder.setEntryValue(new double[] { 1, 2 });
            Vector right = builder.build();
            double[] expected = { 9, 12, 15 };
            Vector result = gm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 行の入れ替えに関する {

        private EntryReadableMatrix gm;

        @Before
        public void before_サイズ3_2_成分1_2_3_4_5_6の長方形行列_swapRow_0_2を生成() {
            /*
             * 5 6
             * 3 4
             * 1 2
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(1, 1, 4);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            builder.swapRows(0, 2);
            gm = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 5, 6 }, { 3, 4 }, { 1, 2 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 列の入れ替えに関する {

        private EntryReadableMatrix gm;

        @Before
        public void before_サイズ2_3_成分1_2_3_4_5_6の長方形行列_swapColumn_0_2を生成() {
            /*
             * 3 2 1
             * 6 5 4
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 5);
            builder.setValue(1, 2, 6);
            builder.swapColumns(0, 2);
            gm = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 3, 2, 1 }, { 6, 5, 4 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class 単位行列の生成 {

        private EntryReadableMatrix gm;

        @Before
        public void before_サイズ3の単位行列を生成() {
            gm = GeneralMatrix.Builder.unit(MatrixDimension.square(3)).build();
        }

        @Test
        public void test_成分の検証() {
            double[][] entries = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class fromMatrixに関する {

        private static class WrappedMatrix extends SkeletalMatrix implements Matrix {

            private final Matrix mx;

            WrappedMatrix(Matrix src) {
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
        public void test_横長行列で成分の検証() {
            /*
             * 1 2 3
             * 4 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 5);
            builder.setValue(1, 2, 6);
            EntryReadableMatrix innerGm = builder.build();
            EntryReadableMatrix gm = GeneralMatrix.Builder.from(new WrappedMatrix(innerGm))
                    .build();

            double[][] entries = { { 1, 2, 3 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_縦長行列で成分の検証() {
            /*
             * 1 2
             * 3 4
             * 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(1, 1, 4);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            EntryReadableMatrix innerGm = builder.build();
            EntryReadableMatrix gm = GeneralMatrix.Builder.from(new WrappedMatrix(innerGm))
                    .build();

            double[][] entries = { { 1, 2 }, { 3, 4 }, { 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class fromEntryReadableMatrixに関する {

        private static class WrappedMatrix extends SkeletalMatrix implements EntryReadableMatrix {

            private final EntryReadableMatrix mx;

            WrappedMatrix(EntryReadableMatrix src) {
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
        public void test_横長行列で成分の検証() {
            /*
             * 1 2 3
             * 4 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 5);
            builder.setValue(1, 2, 6);
            EntryReadableMatrix innerGm = builder.build();
            EntryReadableMatrix gm = GeneralMatrix.Builder.from(new WrappedMatrix(innerGm))
                    .build();

            double[][] entries = { { 1, 2, 3 }, { 4, 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_縦長行列で成分の検証() {
            /*
             * 1 2
             * 3 4
             * 5 6
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(1, 1, 4);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            EntryReadableMatrix innerGm = builder.build();
            EntryReadableMatrix gm = GeneralMatrix.Builder.from(new WrappedMatrix(innerGm))
                    .build();

            double[][] entries = { { 1, 2 }, { 3, 4 }, { 5, 6 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class Matrixの骨格実装のテストを兼ねる {

        private Matrix original;

        @Before
        public void before_行列生成() {

            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(1, 1, 4);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
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
