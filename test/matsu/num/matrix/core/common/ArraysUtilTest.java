/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.common;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * {@link ArraysUtil} のテスト.
 */
@RunWith(Enclosed.class)
final class ArraysUtilTest {

    @RunWith(Theories.class)
    public static class 二項演算に関するテスト {

        private static final int MIN_DIMENSION = 3;
        private static final int MAX_DIMENSION = 10;

        @DataPoints
        public static double[][] vectors;

        @DataPoints
        public static int[] dimensions;

        @BeforeClass
        public static void before_次元の用意() {
            dimensions = IntStream.rangeClosed(MIN_DIMENSION, MAX_DIMENSION)
                    .toArray();
        }

        @BeforeClass
        public static void before_ベクトルの用意() {
            vectors = IntStream.range(0, 5)
                    .mapToObj(c -> {
                        double[] arr = new double[MAX_DIMENSION];
                        for (int i = 0; i < arr.length; i++) {
                            arr[i] = ThreadLocalRandom.current().nextDouble() - 0.5;
                        }
                        return arr;
                    })
                    .toArray(double[][]::new);
        }

        @Theory
        public void test_add(double[] v1, double[] v2, int dimension) {
            v1 = Arrays.copyOf(v1, dimension);
            v2 = Arrays.copyOf(v2, dimension);

            double[] result = v1.clone();
            ArraysUtil.add(result, v2);

            double[] expected = new double[v1.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = v1[i] + v2[i];
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_subtract(double[] v1, double[] v2, int dimension) {
            v1 = Arrays.copyOf(v1, dimension);
            v2 = Arrays.copyOf(v2, dimension);

            double[] result = v1.clone();
            ArraysUtil.subtract(result, v2);

            double[] expected = new double[v1.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = v1[i] - v2[i];
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_addCTimes(double[] v1, double[] v2, int dimension) {
            v1 = Arrays.copyOf(v1, dimension);
            v2 = Arrays.copyOf(v2, dimension);

            double scalar = 1.4;

            double[] result = v1.clone();
            ArraysUtil.addCTimes(result, v2, scalar);

            double[] expected = new double[v1.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = v1[i] + v2[i] * scalar;
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_dot(double[] v1, double[] v2, int dimension) {
            v1 = Arrays.copyOf(v1, dimension);
            v2 = Arrays.copyOf(v2, dimension);

            double result = ArraysUtil.dot(v1, v2);

            double expected = 0d;
            for (int i = 0; i < v1.length; i++) {
                expected += v1[i] * v2[i];
            }

            assertThat(Math.abs(result - expected), is(lessThan(1E-14)));
        }
    }

    @RunWith(Theories.class)
    public static class 単項演算に関するテスト {

        private static final int MIN_DIMENSION = 3;
        private static final int MAX_DIMENSION = 10;

        @DataPoints
        public static double[][] vectors;

        @DataPoints
        public static int[] dimensions;

        @BeforeClass
        public static void before_次元の用意() {
            dimensions = IntStream.rangeClosed(MIN_DIMENSION, MAX_DIMENSION)
                    .toArray();
        }

        @BeforeClass
        public static void before_ベクトルの用意() {
            vectors = IntStream.range(0, 5)
                    .mapToObj(c -> {
                        double[] arr = new double[MAX_DIMENSION];
                        for (int i = 0; i < arr.length; i++) {
                            arr[i] = ThreadLocalRandom.current().nextDouble() - 0.5;
                        }
                        return arr;
                    })
                    .toArray(double[][]::new);
        }

        @Theory
        public void test_multiply(double[] v, int dimension) {
            double scalar = 1.3;

            v = Arrays.copyOf(v, dimension);

            double[] result = v.clone();
            ArraysUtil.multiply(result, scalar);

            double[] expected = new double[v.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = v[i] * scalar;
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_negate(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double[] result = v.clone();
            result = ArraysUtil.negated(result);

            double[] expected = new double[v.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = -v[i];
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_normalizeEuclidean(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double[] result = v.clone();
            result = ArraysUtil.normalizeEuclidean(result);

            double[] expected = new double[v.length];
            double norm2 = 0d;
            for (int i = 0; i < expected.length; i++) {
                norm2 += v[i] * v[i];
            }
            norm2 = Math.sqrt(norm2);
            for (int i = 0; i < expected.length; i++) {
                expected[i] = v[i] / norm2;
            }

            double[] res = result.clone();
            ArraysUtil.subtract(res, expected);

            assertThat(ArraysUtil.normMax(res), is(lessThan(1E-14)));
        }

        @Theory
        public void test_norm1(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double result = ArraysUtil.norm1(v);

            double expected = 0d;
            for (int i = 0; i < v.length; i++) {
                expected += Math.abs(v[i]);
            }

            assertThat(Math.abs(result - expected), is(lessThan(1E-14)));
        }

        @Theory
        public void test_norm2(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double result = ArraysUtil.norm2(v);

            double expected = 0d;
            for (int i = 0; i < v.length; i++) {
                expected += v[i] * v[i];
            }
            expected = Math.sqrt(expected);

            assertThat(Math.abs(result - expected), is(lessThan(1E-14)));
        }

        @Theory
        public void test_norm2Square(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double result = ArraysUtil.norm2Square(v);

            double expected = 0d;
            for (int i = 0; i < v.length; i++) {
                expected += v[i] * v[i];
            }

            assertThat(Math.abs(result - expected), is(lessThan(1E-14)));
        }

        @Theory
        public void test_normMax(double[] v, int dimension) {
            v = Arrays.copyOf(v, dimension);

            double result = ArraysUtil.normMax(v);

            double expected = 0d;
            for (int i = 0; i < v.length; i++) {
                expected = Math.max(expected, Math.abs(v[i]));
            }

            assertThat(Math.abs(result - expected), is(lessThan(1E-14)));
        }
    }
}
