/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.sparse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * [{@link LocalSparseVector} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class LocalSparseVectorTest {

    public static final Class<?> TEST_CLASS = LocalSparseVector.class;

    public static class 生成に関するテスト {

        @Test(expected = IllegalArgumentException.class)
        public void test_開始位置が負の場合() {
            LocalSparseVector.of(
                    VectorDimension.valueOf(5), -1, new double[] { 1 });
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_開始位置が次元以上の場合() {
            LocalSparseVector.of(
                    VectorDimension.valueOf(5), 5, new double[] { 1 });
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_開始位置が次元以上の場合_空配列() {
            LocalSparseVector.of(
                    VectorDimension.valueOf(5), 5, new double[] {});
        }

        @Test
        public void test_不正値を含む場合も正常値に置き換えられる() {
            var vec = LocalSparseVector.of(
                    VectorDimension.valueOf(5), 3, new double[] { Double.NEGATIVE_INFINITY });

            for (double v : vec.asVector().entryAsArray()) {
                assertThat(Vector.acceptValue(v), is(true));
            }
        }
    }

    public static class LocalSparseVectorの生成と演算に関するテスト {

        private static final VectorDimension DIMENSION = VectorDimension.valueOf(5);

        /**
         * [0, 1, 2, 3, 0]
         */
        private SparseVector localSparseVector;

        /**
         * [1, 3, 5, 7, 9]
         */
        private Vector ref;

        @Before
        public void before_スパースベクトルの生成() {
            localSparseVector = LocalSparseVector.of(DIMENSION, 1, new double[] { 1, 2, 3 });
        }

        @Before
        public void before_リファレンスベクトルの生成() {
            var builder = Vector.Builder.zeroBuilder(DIMENSION);
            builder.setEntryValue(new double[] { 1, 3, 5, 7, 9 });
            ref = builder.build();
        }

        @Test
        public void test_成分の検証() {
            double[] expected = { 0, 1, 2, 3, 0 };

            assertThat(localSparseVector.asVector().entryAsArray(), is(expected));
            for (int i = 0; i < expected.length; i++) {
                assertThat(localSparseVector.valueAt(i), is(expected[i]));
            }
        }

        @Test
        public void test_2ノルムの検証() {
            assertThat(localSparseVector.norm2(), is(closeTo(Math.sqrt(14), 1E-14)));
        }

        @Test
        public void test_最大値ノルムの検証() {
            assertThat(localSparseVector.normMax(), is(3d));
        }

        @Test
        public void test_内積の検証() {
            assertThat(localSparseVector.dot(ref), is(34d));
        }

        @Test
        public void test_定数倍の検証() {
            var result = localSparseVector.times(2d);
            double[] expected = { 0, 2, 4, 6, 0 };

            assertThat(result.asVector().entryAsArray(), is(expected));
        }

        @Test
        public void test_定数倍の検証_不正値の変換() {
            var extreme = LocalSparseVector.of(
                    DIMENSION, 1, new double[] {
                            Double.MAX_VALUE,
                            -Double.MAX_VALUE,
                            Double.NaN
                    });

            var result = extreme.times(1E+30);

            for (double v : result.asVector().entryAsArray()) {
                assertThat(Vector.acceptValue(v), is(true));
            }
        }

        @Test
        public void test_規格化の検証() {
            var result = localSparseVector.normalizedEuclidean();

            double[] expected = {
                    0,
                    1 / Math.sqrt(14),
                    2 / Math.sqrt(14),
                    3 / Math.sqrt(14),
                    0 };

            for (int i = 0; i < expected.length; i++) {
                assertThat(result.valueAt(i), is(closeTo(expected[i], 1E-15)));
            }
        }

        @Test
        public void test_逆元の検証() {
            var result = localSparseVector.negated();
            double[] expected = { 0, -1, -2, -3, 0 };

            assertThat(result.asVector().entryAsArray(), is(expected));
        }

        @Test
        public void test_和の検証() {
            var result = localSparseVector.plus(ref);
            double[] expected = { 1, 4, 7, 10, 9 };

            assertThat(result.entryAsArray(), is(expected));
        }

    }

    public static class toString表示 {

        private LocalSparseVector vec;

        @Before
        public void before() {
            vec = LocalSparseVector.of(
                    VectorDimension.valueOf(5),
                    1, new double[] { 2d, 2d });
        }

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(vec);
            System.out.println();
        }
    }
}
