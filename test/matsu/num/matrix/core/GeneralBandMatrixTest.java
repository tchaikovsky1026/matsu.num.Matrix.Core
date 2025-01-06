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
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link GeneralBandMatrix} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class GeneralBandMatrixTest {

    public static final Class<?> TEST_CLASS = GeneralBandMatrix.class;

    public static class 帯行列の評価と演算に関する {

        private BandMatrix gbm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            right = builder.build();
        }

        @Before
        public void before() {
            /*
             * 1 10 0
             * 4 2 11
             * 5 6 3
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(3, 2, 1));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 10);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 2);
            builder.setValue(1, 2, 11);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            builder.setValue(2, 2, 3);
            gbm = builder.build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(gbm.entryNormMax(), is(11.0));
        }

        @Test
        public void test_成分の検証_下側優位() {

            double[][] entries = { { 1, 10, 0 }, { 4, 2, 11 }, { 5, 6, 3 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gbm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }

        @Test
        public void test_行列ベクトル積() {
            double[] expected = { 21, 41, 26 };
            Vector result = gbm.operate(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }

        @Test
        public void test_転置行列ベクトル積() {
            double[] expected = { 24, 32, 31 };
            Vector result = gbm.operateTranspose(right);
            assertThat(Arrays.equals(result.entryAsArray(), expected), is(true));
        }
    }

    public static class 片側退化に関する {

        private BandMatrix gbm;

        private Vector right;

        @Before
        public void before_評価用右辺ベクトル() {

            Vector.Builder builder = Vector.Builder.zeroBuilder(VectorDimension.valueOf(3));
            builder.setEntryValue(new double[] { 1, 2, 3 });
            right = builder.build();
        }

        @Before
        public void before() {
            /*
             * 1 0 0
             * 4 2 0
             * 5 6 3
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(3, 2, 0));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 2);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            builder.setValue(2, 2, 3);
            gbm = builder.build();
        }

        @Test
        public void test_成分最大ノルムの検証() {
            assertThat(gbm.entryNormMax(), is(6.0));
        }

        @Test
        public void test_行列ベクトル積が可能() {
            gbm.operate(right);
        }

        @Test
        public void test_転置行列ベクトル積が可能() {
            gbm.operateTranspose(right);
        }
    }

    public static class fromBandMatrixに関する {

        private static class WrappedMatrix extends SkeletalAsymmetricMatrix<BandMatrix> implements BandMatrix {

            private final BandMatrix mx;

            WrappedMatrix(BandMatrix src) {
                this.mx = Objects.requireNonNull(src);
            }

            @Override
            protected BandMatrix createTranspose() {
                return mx.transpose();
            }

            @Override
            public Vector operate(Vector operand) {
                return mx.operate(operand);
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return mx.operateTranspose(operand);
            }

            @Override
            public BandMatrixDimension bandMatrixDimension() {
                return mx.bandMatrixDimension();
            }

            @Override
            public double valueAt(int row, int column) {
                return mx.valueAt(row, column);
            }

            @Override
            public double entryNormMax() {
                return mx.entryNormMax();
            }
        }

        @Test
        public void test_成分の検証_下側優位() {
            /*
             * 1 10 0
             * 4 2 11
             * 5 6 3
             */

            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(3, 2, 1));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 10);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 2);
            builder.setValue(1, 2, 11);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            builder.setValue(2, 2, 3);
            BandMatrix gbm = GeneralBandMatrix.Builder.from(new WrappedMatrix(builder.build())).build();

            double[][] entries = { { 1, 10, 0 }, { 4, 2, 11 }, { 5, 6, 3 } };

            for (int j = 0; j < entries.length; j++) {
                for (int k = 0; k < entries[j].length; k++) {
                    assertThat(
                            String.format("j=%d,k=%d", j, k),
                            gbm.valueAt(j, k), is(entries[j][k]));
                }
            }
        }
    }

    public static class toString表示 {

        private Matrix mx;

        @Before
        public void before_行列を生成() {
            /*
             * 1 0 0
             * 4 2 0
             * 5 6 3
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(3, 2, 0));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 4);
            builder.setValue(1, 1, 2);
            builder.setValue(2, 0, 5);
            builder.setValue(2, 1, 6);
            builder.setValue(2, 2, 3);
            mx = builder.build();
        }

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(mx);
            System.out.println(mx.transpose());
            System.out.println();
        }
    }
}
