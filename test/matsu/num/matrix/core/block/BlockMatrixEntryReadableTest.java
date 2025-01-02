/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.17
 */
package matsu.num.matrix.core.block;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.ZeroMatrix;

/**
 * {@link BlockMatrixEntryReadable} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class BlockMatrixEntryReadableTest {

    public static final Class<?> TEST_CLASS = BlockMatrixEntryReadable.class;

    public static class 値の取得に関するテスト {

        /**
         * 1_2_2__2_1_3_1の構造 <br>
         * <br>
         * [1, 2][3][4, 5, 6][3] <br>
         * <br>
         * [0, 0][2][0, 0, 0][0] <br>
         * [0, 0][1][0, 0, 0][0] <br>
         * <br>
         * [2, 0][0][3, 2, 1][2] <br>
         * [1, 2][0][0, 1, 2][4]
         */
        private EntryReadableMatrix matrix;

        @Before
        public void before_行列の作成() {

            MatrixDimension structureDimension = MatrixDimension.rectangle(3, 4);

            EntryReadableMatrix m00 = mx00();
            EntryReadableMatrix m01 = mx01();
            EntryReadableMatrix m02 = mx02();
            EntryReadableMatrix m03 = mx03();
            EntryReadableMatrix m11 = mx11();
            EntryReadableMatrix m20 = mx20();
            EntryReadableMatrix m22 = mx22();
            EntryReadableMatrix m23 = mx23();

            BlockMatrixStructure.Builder<EntryReadableMatrix> builder =
                    BlockMatrixStructure.Builder.of(structureDimension);
            builder.setBlockElement(0, 0, m00);
            builder.setBlockElement(0, 1, m01);
            builder.setBlockElement(0, 2, m02);
            builder.setBlockElement(0, 3, m03);
            builder.setBlockElement(1, 1, m11);
            builder.setBlockElement(2, 0, m20);
            builder.setBlockElement(2, 2, m22);
            builder.setBlockElement(2, 3, m23);

            matrix = BlockMatrixEntryReadable.of(builder.build());
        }

        @Test
        public void test_成分の値を検証する() {

            double[][] expected = {
                    { 1, 2, 3, 4, 5, 6, 3 },
                    { 0, 0, 2, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0 },
                    { 2, 0, 0, 3, 2, 1, 2 },
                    { 1, 2, 0, 0, 1, 2, 4 }
            };

            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    assertThat(matrix.valueAt(j, k), is(expected[j][k]));
                }
            }
        }

        @Test
        public void test_最大ノルムは6() {
            assertThat(matrix.entryNormMax(), is(6d));
        }

        private static EntryReadableMatrix mx00() {
            /*
             * [1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);

            return builder.build();
        }

        private static EntryReadableMatrix mx01() {
            /*
             * [3]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 1));
            builder.setValue(0, 0, 3);

            return builder.build();
        }

        private static EntryReadableMatrix mx02() {
            /*
             * [4, 5, 6]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 3));
            builder.setValue(0, 0, 4);
            builder.setValue(0, 1, 5);
            builder.setValue(0, 2, 6);

            return builder.build();
        }

        private static EntryReadableMatrix mx03() {
            /*
             * [3]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(1, 1));
            builder.setValue(0, 0, 3);

            return builder.build();
        }

        private static EntryReadableMatrix mx11() {
            /*
             * [2]
             * [1]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 1));
            builder.setValue(0, 0, 2);
            builder.setValue(1, 0, 1);

            return builder.build();
        }

        private static EntryReadableMatrix mx20() {
            /*
             * [2, 0]
             * [1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 2));
            builder.setValue(0, 0, 2);
            builder.setValue(0, 1, 0);
            builder.setValue(1, 0, 1);
            builder.setValue(1, 1, 2);

            return builder.build();
        }

        private static EntryReadableMatrix mx22() {
            /*
             * [3, 2, 1]
             * [0, 1, 2]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 3));
            builder.setValue(0, 0, 3);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 1);
            builder.setValue(1, 0, 0);
            builder.setValue(1, 1, 1);
            builder.setValue(1, 2, 2);

            return builder.build();
        }

        private static EntryReadableMatrix mx23() {
            /*
             * [2]
             * [4]
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(2, 1));
            builder.setValue(0, 0, 2);
            builder.setValue(1, 0, 4);

            return builder.build();
        }
    }

    public static class toString表示 {

        private EntryReadableMatrix matrix;

        @Before
        public void before_構造作成() {
            MatrixDimension structureDimension = MatrixDimension.rectangle(3, 2);
            BlockMatrixStructure.Builder<EntryReadableMatrix> builder =
                    BlockMatrixStructure.Builder.of(structureDimension);
            builder.setBlockElement(1, 0, ZeroMatrix.matrixOf(MatrixDimension.rectangle(3, 2)));
            builder.setBlockElement(0, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(1, 4)));
            builder.setBlockElement(2, 1, ZeroMatrix.matrixOf(MatrixDimension.rectangle(5, 4)));
            BlockMatrixStructure<EntryReadableMatrix> structure = builder.build();
            matrix = BlockMatrixEntryReadable.of(structure);
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(matrix);
            System.out.println();
        }
    }
}
