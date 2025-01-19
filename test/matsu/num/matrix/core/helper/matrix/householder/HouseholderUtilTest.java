/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.helper.matrix.householder;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.common.ArraysUtil;

/**
 * {@link HouseholderUtil} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class HouseholderUtilTest {

    @RunWith(Theories.class)
    public static class 鏡映ベクトルの計算に関するテスト {

        /**
         * 規格化済みのソース
         */
        @DataPoints
        public static Vector[] NORMALIZED_SRCS;

        @BeforeClass
        public static void before_ソースの用意() {
            VectorDimension dimension = VectorDimension.valueOf(3);

            double[][] arr_srcs = {
                    { -2d, 3d, 1d },
                    { 2d, 1d, 2d },
                    { 2d, 1E-4d, 0d },
                    { 2d, 1E-6d, -1E-8d },
                    { 2d, -1E-8d, -1E-8d },
                    { 2d, 1E-10d, -1E-11d },
                    { 2d, -1E-12d, 1E-13d },
                    { 2d, 1E-15d, 1E-20d },
                    { 2d, -1E-20d, 1E-20d },
                    { 2d, -1E-50d, -1E-40d },
                    { 2d, 0d, 0d }
            };

            List<Vector> list = new ArrayList<>();
            for (double[] arr_src : arr_srcs) {
                var builder = Vector.Builder.zeroBuilder(dimension);
                builder.setEntryValue(arr_src);
                list.add(builder.build().normalizedEuclidean());
            }

            NORMALIZED_SRCS = list.toArray(new Vector[0]);
        }

        @Theory
        public void test_ソースを変換した結果が標準基底に一致するかを確かめる(Vector src) {
            var reflection = HouseholderUtil.computeReflectionVectorToStandardBasis(src);
            HouseholderMatrix mxH = HouseholderMatrixFactory.createFrom(reflection);
            Vector reflected = mxH.operate(src);

            // reflectedと[1,0,...,0]の差
            double[] arr_diff = reflected.entryAsArray();
            arr_diff[0] -= 1d;

            assertThat(ArraysUtil.norm2(arr_diff), is(lessThan(1E-14)));
        }
    }
}
