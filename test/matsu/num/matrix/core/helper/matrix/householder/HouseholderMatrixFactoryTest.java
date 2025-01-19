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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link HouseholderMatrixFactory} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class HouseholderMatrixFactoryTest {

    public static final Class<?> TEST_CLASS = HouseholderMatrixFactory.class;

    public static class 生成に関する例外テスト {

        @Test(expected = IllegalArgumentException.class)
        public void test_零ベクトルはIAEx_次元1() {
            HouseholderMatrixFactory.createFrom(Vector.Builder.zeroBuilder(VectorDimension.valueOf(1)).build());
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_零ベクトルはIAEx_次元2() {
            HouseholderMatrixFactory.createFrom(Vector.Builder.zeroBuilder(VectorDimension.valueOf(2)).build());
        }
    }

    public static class ベクトルから鏡映変換の作成テスト {

        HouseholderMatrix mxH;

        @Before
        public void before() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(5));
            builder.setEntryValue(1, 1, 1, 1, 1);
            Vector reflection = builder.build();

            mxH = HouseholderMatrixFactory.createFrom(reflection);
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

    @RunWith(Theories.class)
    public static class ソースをターゲットに変換する鏡映変換の生成のテスト {

        /**
         * [source, target] のペア, 規格化済み
         */
        @DataPoints
        public static Vector[][] NORMALIZED_SOURCE_AND_TARGET;

        @BeforeClass
        public static void before_ソースの用意() {
            VectorDimension dimension = VectorDimension.valueOf(3);

            double[][][] arr_srcs = {
                    { { 2d, 1d, -1d }, { -1d, 1d, 3d } },
                    { { 2d, 1d, -1d }, { 2d - 1E-14d, 1d + 1E-12d, -1d + 3E-13d } },
                    { { 2d, -1d, -1d }, { 2d, -1d, -1d } },
            };

            List<Vector[]> list = new ArrayList<>();
            for (double[][] arr_src : arr_srcs) {
                Vector[] pair = new Vector[2];

                //source
                {
                    var builder = Vector.Builder.zeroBuilder(dimension);
                    builder.setEntryValue(arr_src[0]);
                    pair[0] = builder.build().normalizedEuclidean();
                }

                //target
                {
                    var builder = Vector.Builder.zeroBuilder(dimension);
                    builder.setEntryValue(arr_src[1]);
                    pair[1] = builder.build().normalizedEuclidean();
                }

                list.add(pair);
            }

            NORMALIZED_SOURCE_AND_TARGET = list.toArray(new Vector[0][0]);
        }

        @Theory
        public void test_ソースを変換した結果がターゲットに一致するかを確かめる(Vector[] pair) {
            var source = pair[0];
            var target = pair[1];

            HouseholderMatrix mxH = HouseholderMatrixFactory.createFrom(source, target);
            Vector residual = mxH.operate(source).minus(target);

            assertThat(residual.norm2(), is(lessThan(1E-14)));
        }
    }

    public static class toString表示 {

        @Test
        public void test_toString() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(5));
            builder.setValue(0, 1);

            System.out.println(TEST_CLASS.getName());
            System.out.println(HouseholderMatrixFactory.createFrom(builder.build()));
            System.out.println();
        }
    }
}
