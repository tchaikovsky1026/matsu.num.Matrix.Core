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

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link HouseholderQR} のテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class HouseholderQRTest {

    public static final Class<?> TEST_CLASS = HouseholderQR.class;

    @RunWith(Theories.class)
    public static class 一般化逆行列としての規則の検証 {

        /**
         * 鏡映ベクトルが安定パターン.
         */
        @DataPoint
        public static EntryReadableMatrix mxA_Stable;

        /**
         * 鏡映ベクトルが不安定になるパターン.
         */
        @DataPoint
        public static EntryReadableMatrix mxA_Unstable;

        @BeforeClass
        public static void before_鏡映ベクトルが安定パターンで行列を生成() {

            /*
             * -1 2 3
             * 2 3 2
             * 1 1 2
             * 0 5 6
             */
            GeneralMatrix.Builder builder =
                    GeneralMatrix.Builder.zero(MatrixDimension.rectangle(4, 3));
            builder.setValue(0, 0, -1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 0, 1);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            builder.setValue(3, 0, 0);
            builder.setValue(3, 1, 5);
            builder.setValue(3, 2, 6);
            mxA_Stable = builder.build();
        }

        @BeforeClass
        public static void before_鏡映ベクトルが不安定になるパターンで行列を生成() {

            /*
             * 1 2 3
             * 0 3 2
             * 1E-8 1 2
             * 0 5 6
             */
            GeneralMatrix.Builder builder =
                    GeneralMatrix.Builder.zero(MatrixDimension.rectangle(4, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 0);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 0, 1E-8);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            builder.setValue(3, 0, 0);
            builder.setValue(3, 1, 5);
            builder.setValue(3, 2, 6);
            mxA_Unstable = builder.build();
        }

        @Theory
        public void test_A_Ainv_Aで検証(EntryReadableMatrix mxA) {
            HouseholderQR qr = HouseholderQR.executor().apply(mxA).get();

            //A^{+} A A^{+} = A^{+} を確かめる
            Matrix mxAInv = qr.inverse();
            VectorDimension rightMxA = mxA.matrixDimension().rightOperableVectorDimension();
            for (int i = 0; i < rightMxA.intValue(); i++) {
                Vector.Builder vBuilder = Vector.Builder.zeroBuilder(rightMxA);
                vBuilder.setValue(i, 1);
                Vector vecE = vBuilder.build();

                Vector vecR1 = mxA.operate(vecE);
                Vector vecR2 = mxA.operate(mxAInv.operate(vecR1));
                double res = vecR1.minus(vecR2).normMax();
                assertThat(res, is(lessThan(1E-12)));
            }
        }

        @Theory
        public void test_Ainv_A_Ainvで検証(EntryReadableMatrix mxA) {
            HouseholderQR qr = HouseholderQR.executor().apply(mxA).get();

            //A^{+} A A^{+} = A^{+} を確かめる
            Matrix mxAInv = qr.inverse();
            VectorDimension leftMxA = mxA.matrixDimension().leftOperableVectorDimension();
            for (int i = 0; i < leftMxA.intValue(); i++) {
                Vector.Builder vBuilder = Vector.Builder.zeroBuilder(leftMxA);
                vBuilder.setValue(i, 1);
                Vector vecE = vBuilder.build();

                Vector vecR1 = mxAInv.operate(vecE);
                Vector vecR2 = mxAInv.operate(mxA.operate(vecR1));
                double res = vecR1.minus(vecR2).normMax();
                assertThat(res, is(lessThan(1E-12)));
            }
        }
    }

    public static class 正方行列での検証 {

        private EntryReadableMatrix mxA;

        @Before
        public void before_行列を生成() {

            /*
             * -1 2 3
             * 2 3 2
             * 1 1 2
             */
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 3));
            builder.setValue(0, 0, -1);
            builder.setValue(0, 1, 2);
            builder.setValue(0, 2, 3);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(1, 2, 2);
            builder.setValue(2, 0, 1);
            builder.setValue(2, 1, 1);
            builder.setValue(2, 2, 2);
            mxA = builder.build();
        }

        @Test
        public void test_Ainv_Aで検証() {
            HouseholderQR qr = HouseholderQR.executor().apply(mxA).get();

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

        @Test
        public void test_A_Ainvで検証() {
            HouseholderQR qr = HouseholderQR.executor().apply(mxA).get();

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

        private EntryReadableMatrix matrix;

        @Before
        public void before_行列を生成() {
            GeneralMatrix.Builder builder = GeneralMatrix.Builder.zero(MatrixDimension.rectangle(3, 3));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 1);
            builder.setValue(2, 2, 1);
            matrix = builder.build();
        }

        @Test
        public void toStringを表示する() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(HouseholderQR.executor());
            System.out.println(HouseholderQR.executor().apply(matrix).get());
            System.out.println();
        }
    }
}
