/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.sparse;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.helper.matrix.householder.HouseholderMatrixFactory;

/**
 * {@link HouseholderMatrixWithSparseVector} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class HouseholderMatrixWithSparseVectorTest {

    public static final Class<?> TEST_CLASS = HouseholderMatrixFactory.class;

    public static class 生成に関する例外テスト {

        @Test(expected = IllegalArgumentException.class)
        public void test_零ベクトルはIAEx_次元2() {
            new HouseholderMatrixWithSparseVector(
                    new SimpleSparseVector(
                            Vector.Builder.zeroBuilder(VectorDimension.valueOf(2)).build()));
        }
    }

    public static class ベクトルから鏡映変換の作成テスト {

        HouseholderMatrix mxH;

        @Before
        public void before() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(5));
            builder.setEntryValue(1, 1, 1, 1, 1);
            Vector reflection = builder.build();

            mxH = new HouseholderMatrixWithSparseVector(new SimpleSparseVector(reflection));
        }

        @Test
        public void test_行列ベクトル積のテスト() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(5));
            builder.setEntryValue(1, 2, 3, 4, 5);
            Vector v = builder.build();

            double[] result = mxH.operate(v).entryAsArray();
            double[] expected = {
                    -5,
                    -4,
                    -3,
                    -2,
                    -1
            };
            for (int i = 0; i < result.length; i++) {
                assertThat(result[i], is(closeTo(expected[i], 1E-12)));
            }
        }
    }
}
