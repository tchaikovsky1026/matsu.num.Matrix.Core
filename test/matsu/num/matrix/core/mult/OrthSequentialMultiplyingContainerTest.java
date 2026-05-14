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

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;

/**
 * {@link OrthSequentialMultiplyingContainer} のテスト.
 */
@RunWith(Enclosed.class)
final class OrthSequentialMultiplyingContainerTest {

    /*
     * ========================
     * テスト戦略
     * 
     * 様々なサイズの正方単位行列をベースとして,
     * 左右から行列を複数回乗算し, 結果を検証する.
     * 主に, 成分一致性と直交性を検証.
     * ========================
     */
    @RunWith(Theories.class)
    public static class 行列積のテスト {

        @DataPoints
        public static MatrixDimension[] matrixDimensions = {
                MatrixDimension.square(1),
                MatrixDimension.square(2),
                MatrixDimension.square(3),
                MatrixDimension.square(4)
        };

        @Theory
        public void test_左2回から右2回のパターン(MatrixDimension matrixDimension) {
            UnitMatrix baseSrc = UnitMatrix.matrixOf(matrixDimension);
            OrthogonalMatrix l1 = createRandomOthoMatrix(matrixDimension);
            OrthogonalMatrix l2 = createRandomOthoMatrix(matrixDimension);
            OrthogonalMatrix r1 = createRandomOthoMatrix(matrixDimension);
            OrthogonalMatrix r2 = createRandomOthoMatrix(matrixDimension);

            var con = OrthSequentialMultiplyingContainer.basedOnOrth(baseSrc);
            con.operateLeftSide(l1);
            con.operateRightSide(r1);
            con.operateLeftSide(l2);
            con.operateRightSide(r2);
            ProductOrthogonalMatrix result = con.build();

            OrthogonalMatrix expected = OrthogonalMatrix.multiply(l2, l1, baseSrc, r1, r2);

            testEntryEquality(result, expected, 1E-14);
            testOrth(result, 1E-14);
        }
    }

    /** 与えられた次元を持つランダムな直交行列を作成する. 正方が必要. */
    private static OrthogonalMatrix createRandomOthoMatrix(MatrixDimension matrixDimension) {
        if (!matrixDimension.isSquare()) {
            throw new AssertionError("not square");
        }

        // ハウスホルダー変換を使う
        var b = Vector.Builder.zeroBuilder(matrixDimension.leftOperableVectorDimension());
        for (int j = 0; j < matrixDimension.rowAsIntValue(); j++) {
            double v = ThreadLocalRandom.current().nextDouble() - 0.5;
            if (v == 0d) {
                v = Double.MIN_NORMAL;
            }
            b.setValue(j, v);
        }

        return HouseholderMatrix.from(b.build());
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

    /** 与えた行列が直交行列として正しいかを検証する. */
    private static void testOrth(OrthogonalMatrix m, double acceptableResidual) {
        assertThat(m.matrixDimension().isSquare(), is(true));

        double res = 0d;
        for (int k = 0; k < m.matrixDimension().columnAsIntValue(); k++) {
            var b = Vector.Builder.zeroBuilder(m.matrixDimension().rightOperableVectorDimension());
            b.setValue(k, 1d);
            var rightVector = b.build();

            double currentRes = m.operateTranspose(m.operate(rightVector))
                    .minus(rightVector)
                    .normMax();
            res = Math.max(res, currentRes);
        }
        assertThat(res, is(lessThanOrEqualTo(acceptableResidual)));
    }
}
