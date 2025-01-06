/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link PermutationMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class PermutationMatrixTest {

    public static final Class<?> TEST_CLASS = PermutationMatrix.class;

    public static class 生成に関する {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_長方形行列は生成できない() {
            PermutationMatrix.Builder.unitBuilder(MatrixDimension.rectangle(2, 3));
        }
    }

    public static class 行の交換と行列ベクトル積のテスト {

        private PermutationMatrix pm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            right = builder.build();
        }

        @Before
        public void before_次元3__swapRow_0_1__swapRow_1_2() {
            /*
             * 0 1 0
             * 0 0 1
             * 1 0 0
             */
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapRows(0, 1);
            builder.swapRows(1, 2);

            pm = builder.build();
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

            Vector result = pm.operate(right);
            double[] expected = { 2, 3, 1 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_左から1_2_3を乗算() {
            Vector result = pm.operateTranspose(right);
            double[] expected = { 3, 1, 2 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 列の交換と行列ベクトル積のテスト {

        private PermutationMatrix pm;
        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            right = builder.build();
        }

        @Before
        public void before_次元3__swapRow_1_2__swapRow_0_1() {
            /*
             * 0 1 0
             * 0 0 1
             * 1 0 0
             */
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapColumns(2, 1);
            builder.swapColumns(1, 0);

            pm = builder.build();
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
            Vector result = pm.operate(right);
            double[] expected = { 2, 3, 1 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_左から1_2_3を乗算() {
            Vector result = pm.operateTranspose(right);
            double[] expected = { 3, 1, 2 };
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 逆行列のテスト {

        private PermutationMatrix original;
        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            right = builder.build();
        }

        @Before
        public void before_次元3__swapRow_0_1__swapRow_1_2() {
            /*
             * 0 1 0
             * 0 0 1
             * 1 0 0
             */
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapRows(0, 1);
            builder.swapRows(1, 2);

            original = builder.build();
        }

        @Test
        public void test_inverse_operateはoriginal_operateTransposeに等しい() {

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
            assertThat(
                    original.inverse().get().operateTranspose(right).entryAsArray(),
                    is(original.operate(right).entryAsArray()));
        }

        @Test
        public void test_inverse_inverse_operateはoriginal_operateに等しい() {
            assertThat(
                    original.inverse().get().inverse().get().operate(right).entryAsArray(),
                    is(original.operate(right).entryAsArray()));
        }

        @Test
        public void test_inverse_inverse_operateTransposeはoriginal_operateTransposeに等しい() {
            assertThat(
                    original.inverse().get().inverse().get().operateTranspose(right).entryAsArray(),
                    is(original.operateTranspose(right).entryAsArray()));
        }

    }

    public static class OrthogonalMatrixの骨格実装のテストを兼ねる {

        private OrthogonalMatrix original;

        @Before
        public void before_直交行列生成() {
            PermutationMatrix.Builder builder = PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapRows(0, 1);
            builder.swapRows(1, 2);
            original = builder.build();
        }

        @Test
        public void test_逆行列の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalAsymmetricOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                //オプショナルの一致でテスト
                assertThat(original.inverse() == original.inverse(), is(true));
            }

        }

        @Test
        public void test_逆行列の逆行列の呼び出しは同一のインスタンスを参照する() {
            if (original instanceof SkeletalAsymmetricOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.inverse().get().inverse() == original.inverse().get().inverse(), is(true));
            }

        }

        @Test
        public void test_逆行列の逆行列は自身と同一() {
            if (original instanceof SkeletalAsymmetricOrthogonalMatrix) {
                //骨格実装を継承している場合のみ, このテストを走らせる
                assertThat(original.inverse().get().inverse().get(), is(original));
            }

        }
    }

    public static class toString表示 {

        private PermutationMatrix pm;

        @Before
        public void before() {
            PermutationMatrix.Builder builder =
                    PermutationMatrix.Builder.unitBuilder(MatrixDimension.square(3));
            builder.swapColumns(0, 1);
            builder.swapRows(1, 2);
            pm = builder.build();
        }

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(pm);
            System.out.println(pm.inverse().get());
            System.out.println();
        }
    }
}
