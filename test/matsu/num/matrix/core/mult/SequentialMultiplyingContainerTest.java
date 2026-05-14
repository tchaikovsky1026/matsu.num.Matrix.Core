/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

package matsu.num.matrix.core.mult;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;

/**
 * {@link SequentialMultiplyingContainer} のテスト.
 */
@RunWith(Enclosed.class)
final class SequentialMultiplyingContainerTest {

    /*
     * ========================
     * テスト戦略
     * 
     * 様々なサイズ行列をベース (縦長, 横長, 正方) として,
     * 左右から行列を複数回乗算し, 結果を検証する.
     * ========================
     */
    @RunWith(Theories.class)
    public static class 行列積のテスト {

        @DataPoints
        public static MatrixDimension[] matrixDimensions = {
                MatrixDimension.rectangle(4, 2),
                MatrixDimension.rectangle(2, 4),
                MatrixDimension.rectangle(3, 3)
        };

        @Theory
        public void test_左2回から右2回のパターン(MatrixDimension matrixDimension) {
            EntryReadableMatrix baseSrc = createRandomMatrix(matrixDimension);
            Matrix l1 = createRandomMatrix(matrixDimension.leftSquareDimension());
            Matrix l2 = createRandomMatrix(matrixDimension.leftSquareDimension());
            Matrix r1 = createRandomMatrix(matrixDimension.rightSquareDimension());
            Matrix r2 = createRandomMatrix(matrixDimension.rightSquareDimension());

            var con = SequentialMultiplyingContainer.basedOn(baseSrc);
            con.operateLeftSide(l1);
            con.operateRightSide(r1);
            con.operateLeftSide(l2);
            con.operateRightSide(r2);
            EntryReadableMatrix result = con.build();

            Matrix expected = Matrix.multiply(l2, l1, baseSrc, r1, r2);

            testEntryEquality(result, expected, 1E-14);
        }
    }

    /** 与えられた次元を持ち, -0.5から0.5の範囲のランダムな値の成分を持つ行列を生成する. */
    private static EntryReadableMatrix createRandomMatrix(MatrixDimension matrixDimension) {
        var b = GeneralMatrix.Builder.zero(matrixDimension);
        for (int j = 0; j < matrixDimension.rowAsIntValue(); j++) {
            for (int k = 0; k < matrixDimension.columnAsIntValue(); k++) {
                b.setValue(j, k, ThreadLocalRandom.current().nextDouble() - 0.5);
            }
        }
        return b.build();
    }

    /** 与えた行列が同等かを検証する. */
    private static void testEntryEquality(Matrix m1, Matrix m2, double acceptableResidual) {
        assertThat(m1.matrixDimension(), is(m2.matrixDimension()));

        double res = 0d;
        for (int k = 0; k < m1.matrixDimension().columnAsIntValue(); k++) {
            var b = Vector.Builder.zeroBuilder(m1.matrixDimension().rightOperableVectorDimension());
            b.setValue(k, 1d);
            var rightVector = b.build();

            double currentRes = m1.operate(rightVector)
                    .minus(m2.operate(rightVector))
                    .normMax();
            res = Math.max(res, currentRes);
        }
        assertThat(res, is(lessThanOrEqualTo(acceptableResidual)));
    }

}
