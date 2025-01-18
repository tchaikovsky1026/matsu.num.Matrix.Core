/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.helper.matrix.householder;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link OneDimensionHouseholderHolder} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class OneDimensionHouseholderHolderTest {

    public static class 行列ベクトル積テスト {

        HouseholderMatrix mxH;

        @Before
        public void before() {
            mxH = OneDimensionHouseholderHolder.INSTANCE;
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_次元不一致でMFMEx() {
            var testVec = Vector.Builder.zeroBuilder(VectorDimension.valueOf(2)).build();

            mxH.operate(testVec);
        }

        @Test
        public void test_行列ベクトル積はマイナス1倍() {
            double value = -3d;

            var builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(1));
            builder.setValue(0, value);

            var resultVector = mxH.operate(builder.build());

            assertThat(resultVector.valueAt(0), is(-value));
        }
    }
}
