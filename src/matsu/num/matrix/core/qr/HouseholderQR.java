/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.16
 */
package matsu.num.matrix.core.qr;

import java.util.Optional;

import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * Householder変換によるQR分解を表す.
 * 
 * <p>
 * 列フルランクな行列 A に対する分解のうち, <br>
 * A = QR'D, Q: 直交行列, D: 対角成分が正の対角行列, <br>
 * R' = <br>
 * &lceil; R &rceil; <br>
 * &lfloor; O &rfloor;, <br>
 * R: 単位上三角行列, <br>
 * を扱い,
 * Q が Householder変換の積 Q = H<sub>1</sub>H<sub>2</sub>, ...
 * により表現する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class HouseholderQR
        extends SkeletalQRTypeSolver<EntryReadableMatrix, Matrix> {

    private final EntryReadableMatrix target;

    private final OrthogonalMatrix mxQ;
    private final DiagonalMatrix mxD;

    /**
     * R^{T} を表す.
     */
    private final LowerUnitriangular mxRt;

    /**
     * 内部から呼ばれる.
     */
    private HouseholderQR(HouseholderQRHelper helper) {
        super();
        this.target = helper.target();
        this.mxQ = helper.mxQ();
        this.mxD = helper.mxD();
        this.mxRt = helper.mxRt();

        assert this.mxD.inverse().isPresent();
    }

    @Override
    public EntryReadableMatrix target() {
        return this.target;
    }

    @Override
    Matrix createInverse() {
        /*
         * A = QR'D = Q_{1} RD
         * の一般化逆行列は,
         * 
         * A^{+}
         * = D^{-1} R^{-1} Q_{1}^{T}
         * = D^{-1} (R^{-1} O) Q^{T}
         * 
         * である.
         */

        /* R'^{+} = (R^{-1} O) */
        Matrix mxInvExtR = ZeroExtendedMatrixUtil.instanceOf(
                this.mxRt.inverse().get().transpose(),
                this.target.matrixDimension().transpose());

        return Matrix.multiply(
                this.mxD.inverse().get(),
                mxInvExtR,
                this.mxQ.transpose());
    }

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static HouseholderQR.Executor executor() {
        return Executor.INSTANCE;
    }

    public static final class Executor
            extends SkeletalQRTypeSolver.Executor<EntryReadableMatrix, HouseholderQR> {

        private static final Executor INSTANCE = new Executor();

        /**
         * 非公開のコンストラクタ, シングルトン.
         */
        private Executor() {
            super();
        }

        @Override
        MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
            return matrix.matrixDimension().isAccepedForDenseMatrix()
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        @Override
        Optional<HouseholderQR> applyConcretely(EntryReadableMatrix matrix, double epsilon) {
            try {
                final double EPSILON_A = 1E-100;
                HouseholderQRHelper helper = new HouseholderQRHelper(matrix, epsilon + EPSILON_A);

                return Optional.of(new HouseholderQR(helper));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
