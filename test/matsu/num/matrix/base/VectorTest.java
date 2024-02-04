package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.common.ArraysUtil;

/**
 * {@link Vector}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public final class VectorTest {

    public static class 二項演算に関するテスト {

        private final VectorDimension dimension = VectorDimension.valueOf(3);
        private Vector vector1;
        private Vector vector2;

        @Before
        public void before_vector1を生成_次元3__1_2_m3() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(dimension);
            builder.setEntryValue(1.0, 2.0, -3.0);
            vector1 = builder.build();
        }

        @Before
        public void before_vector2を生成_次元3__3_2_1() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(dimension);
            builder.setEntryValue(3.0, 2.0, 1.0);
            vector2 = builder.build();
        }

        @Test
        public void test_Vector1とVector2の加算は4_4_m2() {
            Vector resultVector = vector1.plus(vector2);

            double[] expectedEntry = { 4, 4, -2 };
            assertThat(resultVector.entry(), is(expectedEntry));
        }

        @Test
        public void test_Vector1とVector2の減算はm2_0_m4() {
            Vector resultVector = vector1.minus(vector2);

            double[] expectedEntry = { -2, 0, -4 };
            assertThat(resultVector.entry(), is(expectedEntry));
        }

        @Test
        public void test_Vector1にVector2の2倍を足すと7_6_m1() {
            Vector resultVector = vector1.plusCTimes(vector2, 2.0);

            double[] expectedEntry = { 7, 6, -1 };
            assertThat(resultVector.entry(), is(expectedEntry));
        }

        @Test
        public void test_Vector1とVector2の内積は4() {
            assertThat(vector1.dot(vector2), is(closeTo(4.0, 1E-14)));

        }
    }

    public static class 単項演算に関するテスト {

        private final VectorDimension dimension = VectorDimension.valueOf(3);
        private Vector vector;

        @Before
        public void before_vectorを生成_次元3__1_2_m3() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(dimension);
            builder.setEntryValue(1.0, 2.0, -3.0);
            vector = builder.build();
        }

        @Test
        public void test_Vectorの2倍は2_4_m6() {
            Vector resultVector = vector.times(2.0);

            double[] expectedEntry = { 2, 4, -6 };
            assertThat(resultVector.entry(), is(expectedEntry));
        }

        @Test
        public void test_Vectorの1ノルムは6() {
            assertThat(vector.norm1(), is(6.0));
        }

        @Test
        public void test_Vectorの2ノルム二乗は14() {
            assertThat(vector.norm2Square(), is(14.0));
        }

        @Test
        public void test_Vectorの2ノルムはsqrt_14() {
            assertThat(vector.norm2(), is(closeTo(Math.sqrt(14.0), 1E-12)));
        }

        @Test
        public void test_Vectorの最大ノルムは3() {
            assertThat(vector.normMax(), is(3.0));
        }

        @Test
        public void test_正規化により大きさ1() {
            /*
             * 内部実装を確かめるため, Vector.norm2()でなくArraysUtilを使う.
             */
            assertThat(
                    ArraysUtil.norm2(vector.normalizedEuclidean().entryAsArray()),
                    is(closeTo(1.0, 1E-12)));
            assertThat(
                    ArraysUtil.norm2(vector.times(1E300).normalizedEuclidean().entryAsArray()),
                    is(closeTo(1.0, 1E-12)));
            assertThat(
                    ArraysUtil.norm2(vector.times(1E-300).normalizedEuclidean().entryAsArray()),
                    is(closeTo(1.0, 1E-12)));
        }

        @Test
        public void test_2ノルム_極小() {
            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(4));
            builder.setEntryValue(Double.MIN_VALUE, Double.MIN_VALUE, -Double.MIN_VALUE, Double.MIN_VALUE);
            vector = builder.build();
            assertThat(vector.norm2(), is(2 * Double.MIN_VALUE));
        }

    }

    public static class toStringの表示 {

        private List<Vector> vectors;

        @Before
        public void before_vectorを生成() {
            vectors = new ArrayList<>();

            Vector.Builder builder;

            builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(1.0, 2.0, -3.0);
            vectors.add(builder.build());

            builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(8));
            builder.setEntryValue(1, 2, -3, 4, 5, 6, 7, 8);
            vectors.add(builder.build());
        }

        @Test
        public void test_toString表示() {
            System.out.println(Vector.class.getName() + ":");
            vectors.stream().forEach(System.out::println);
            System.out.println();
        }
    }
}
