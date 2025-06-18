/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.qr;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.GeneralBandMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link HouseholderQRBand} のテスト.
 */
@RunWith(Enclosed.class)
final class HouseholderQRBandTest {

    public static final Class<?> TEST_CLASS = HouseholderQRBand.class;

    @RunWith(Theories.class)
    public static class 逆行列の検証 {

        /**
         * 帯幅がフルのパターン.
         */
        @DataPoint
        public static BandMatrix mxA_Full;

        /**
         * 上側帯幅が大きいパターン.
         */
        @DataPoint
        public static BandMatrix mxA_UpperLarge;

        /**
         * 下側帯幅が大きいパターン.
         */
        @DataPoint
        public static BandMatrix mxA_LowerLarge;

        @BeforeClass
        public static void before_帯行列を生成_帯幅がフルのパターン() {

            /*
             * -1 2 3 2
             * 2 3 2 1
             * 1 1 2 1
             * 0 5 6 0
             */
            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 3, 3));
            builder.setValue(0, 0, -1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(0, 3, 2);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(1, 3, 1);
            builder.setValue(2, 0, 1);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            builder.setValue(2, 3, 1.1);
            builder.setValue(3, 0, 0);
            builder.setValue(3, 1, 5);
            builder.setValue(3, 2, 6);
            builder.setValue(3, 3, 0);
            mxA_Full = builder.build();
        }

        @BeforeClass
        public static void before_帯行列を生成_上側帯幅が大きいパターン() {

            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 1, 2));
            builder.setValue(0, 0, -1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(1, 3, 1);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            builder.setValue(2, 3, 1.1);
            builder.setValue(3, 2, 6);
            builder.setValue(3, 3, 0);
            mxA_UpperLarge = builder.build();
        }

        @BeforeClass
        public static void before_帯行列を生成_下側帯幅が大きいパターン() {

            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 2, 1));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 0, 2.2);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            builder.setValue(2, 3, 1.1);
            builder.setValue(3, 1, 2);
            builder.setValue(3, 2, 6);
            builder.setValue(3, 3, 0);
            mxA_LowerLarge = builder.build();
        }

        @Theory
        public void test_Ainv_Aで検証(BandMatrix mxA) {
            HouseholderQRBand qr = HouseholderQRBand.executor().apply(mxA).get();

            //A^{+} A = I を確かめる
            Matrix mxAInv = qr.inverse();
            VectorDimension rightMxA = mxA.matrixDimension().rightOperableVectorDimension();
            for (int i = 0; i < rightMxA.intValue(); i++) {
                Vector.Builder vBuilder = Vector.Builder.zeroBuilder(rightMxA);
                vBuilder.setValue(i, 1);
                Vector vecE = vBuilder.build();

                Vector vecR = mxAInv.operate(mxA.operate(vecE));
                double res = vecR.minus(vecE).normMax();
                assertThat(res, is(lessThan(1E-12)));
            }
        }

        @Theory
        public void test_A_Ainvで検証(BandMatrix mxA) {
            HouseholderQRBand qr = HouseholderQRBand.executor().apply(mxA).get();

            //A A^{+} = I を確かめる
            Matrix mxAInv = qr.inverse();
            VectorDimension leftMxA = mxA.matrixDimension().leftOperableVectorDimension();
            for (int i = 0; i < leftMxA.intValue(); i++) {
                Vector.Builder vBuilder = Vector.Builder.zeroBuilder(leftMxA);
                vBuilder.setValue(i, 1);
                Vector vecE = vBuilder.build();

                Vector vecR = mxA.operate(mxAInv.operate(vecE));
                double res = vecR.minus(vecE).normMax();
                assertThat(res, is(lessThan(1E-12)));
            }
        }
    }

    public static class toString表示 {

        private BandMatrix matrix;

        @Before
        public void before_行列を生成() {

            GeneralBandMatrix.Builder builder = GeneralBandMatrix.Builder.zero(BandMatrixDimension.of(4, 1, 2));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 1);
            builder.setValue(2, 2, 1);
            builder.setValue(3, 3, 1);
            matrix = builder.build();
        }

        @Test
        public void toStringを表示する() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(HouseholderQRBand.executor());
            System.out.println(HouseholderQRBand.executor().apply(matrix).get());
            System.out.println();
        }
    }
}
