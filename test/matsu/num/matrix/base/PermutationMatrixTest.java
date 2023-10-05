package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * {@link PermutationMatrix}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public final class PermutationMatrixTest {

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は生成できない() {
            PermutationMatrix.Builder.unitBuilder(MatrixDimension.rectangle(2, 3));
        }
    }

    public static class 行の交換と行列ベクトル積のテスト {

        private PermutationMatrix pm;

        @Before
        public void before_次元3__swapRow_0_1__swapRow_1_2() {
            /*
                0 1 0
                0 0 1
                1 0 0
             */
            pm = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3))
                    .swapRows(0, 1)
                    .swapRows(1, 2)
                    .build();
        }

        @Test
        public void test_成分評価() {
            double[][] entries = { { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 0 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            pm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_右から1_2_3を乗算() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            Vector result = pm.operate(right);
            double[] expected = { 2, 3, 1 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_左から1_2_3を乗算() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            Vector result = pm.operateTranspose(right);
            double[] expected = { 3, 1, 2 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 列の交換と行列ベクトル積のテスト {

        private PermutationMatrix pm;

        @Before
        public void before_次元3__swapRow_1_2__swapRow_0_1() {
            /*
                0 1 0
                0 0 1
                1 0 0
             */
            pm = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3))
                    .swapColumns(2, 1)
                    .swapColumns(1, 0)
                    .build();
        }

        @Test
        public void test_成分評価() {
            double[][] entries = { { 0, 1, 0 }, { 0, 0, 1 }, { 1, 0, 0 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            pm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_右から1_2_3を乗算() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            Vector result = pm.operate(right);
            double[] expected = { 2, 3, 1 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_左から1_2_3を乗算() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();
            Vector result = pm.operateTranspose(right);
            double[] expected = { 3, 1, 2 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 逆行列のテスト {

        private PermutationMatrix original;

        @Before
        public void before_次元3__swapRow_0_1__swapRow_1_2() {
            /*
                0 1 0
                0 0 1
                1 0 0
             */
            original = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3))
                    .swapRows(0, 1)
                    .swapRows(1, 2)
                    .build();
        }

        @Test
        public void test_inverse_operateはoriginal_operateTransposeに等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();

            //遅延初期化の可能性を考え2回実行パターン,逆行列の逆行列のテスト 
            assertThat(
                    original.inverse().get().inverse().get().inverse().get().operate(right).entryAsArray(),
                    is(original.operateTranspose(right).entryAsArray()));
            assertThat(
                    original.inverse().get().operate(right).entryAsArray(),
                    is(original.operateTranspose(right).entryAsArray()));
        }

        @Test
        public void test_inverse_operateTransposeはoriginal_operateに等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();

            assertThat(
                    original.inverse().get().operateTranspose(right).entryAsArray(),
                    is(original.operate(right).entryAsArray()));
        }

        @Test
        public void test_inverse_inverse_operateはoriginal_operateに等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();

            assertThat(
                    original.inverse().get().inverse().get().operate(right).entryAsArray(),
                    is(original.operate(right).entryAsArray()));
        }

        @Test
        public void test_inverse_inverse_operateTransposeはoriginal_operateTransposeに等しい() {
            Vector right = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3))
                    .setEntryValue(new double[] { 1, 2, 3 }).build();

            assertThat(
                    original.inverse().get().inverse().get().operateTranspose(right).entryAsArray(),
                    is(original.operateTranspose(right).entryAsArray()));
        }

    }

    public static class OrthogonalMatrixの骨格実装のテストを兼ねる {

        private OrthogonalMatrix original;

        @Before
        public void before_直交行列生成() {
            original = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3))
                    .swapRows(0, 1)
                    .swapRows(1, 2)
                    .build();
        }

        @Test
        public void test_逆行列の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                //オプショナルの一致でテスト
                assertThat(original.inverse() == original.inverse(), is(true));
            }

        }

        @Test
        public void test_逆行列の逆行列の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.inverse().get().inverse() == original.inverse().get().inverse(), is(true));
            }

        }

        @Test
        public void test_逆行列の逆行列は自身と同一() {
            if (original instanceof SkeletalOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.inverse().get().inverse().get(), is(original));
            }

        }
    }
}
