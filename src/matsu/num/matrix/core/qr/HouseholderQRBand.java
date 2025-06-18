/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.17
 */
package matsu.num.matrix.core.qr;

import java.util.Optional;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * Householder変換による正方帯行列のQR分解を表す.
 * 
 * <p>
 * 正方帯行列 A に対する分解のうち, <br>
 * A = QRD, Q: 直交行列, D: 対角成分が正の対角行列,
 * R: 単位上三角行列を扱い,
 * Q が Householder変換の積 Q = H<sub>1</sub>H<sub>2</sub>, ...
 * により表現される.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class HouseholderQRBand
        extends SkeletalQRTypeSolver<BandMatrix, Matrix> {

    private final BandMatrix target;

    private final OrthogonalMatrix mxQ;
    private final DiagonalMatrix mxD;

    /**
     * R^{T} を表す.
     */
    private final LowerUnitriangular mxRt;

    /**
     * 内部から呼ばれる.
     */
    private HouseholderQRBand(HouseholderQRBandHelper helper) {
        super();
        this.target = helper.target();
        this.mxQ = helper.mxQ();
        this.mxD = helper.mxD();
        this.mxRt = helper.mxRt();

        assert this.mxD.inverse().isPresent();
    }

    @Override
    public BandMatrix target() {
        return this.target;
    }

    @Override
    Matrix createInverse() {
        /*
         * A = QRD
         * の一般化逆行列は,
         * 
         * A^{+}
         * = D^{-1} R^{-1} Q^{T}
         * 
         * である.
         */
        return Matrix.multiply(
                this.mxD.inverse().get(),
                this.mxRt.inverse().get().transpose(),
                this.mxQ.transpose());
    }

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static HouseholderQRBand.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * Householder変換による正方帯行列のQR分解のエグゼキュータ.
     * 
     * <p>
     * {@code accepts} メソッドでrejectされる条件は,
     * {@link QRTypeSolver.Executor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>行列の有効要素数が大きすぎる場合(後述)</li>
     * </ul>
     * 
     * <p>
     * {@code apply} メソッドで空が返る条件は,
     * {@link QRTypeSolver.Executor} に加わる追加条件はない.
     * </p>
     * 
     * <p>
     * このクラスのインスタンスは, {@link HouseholderQRBand#executor()} メソッドにより得ることができる.
     * <br>
     * 実質的にシングルトンである.
     * </p>
     * 
     * <hr>
     * 
     * <p>
     * 有効要素数が大きすぎるかどうかは,
     * {@link BandMatrixDimension#isAccepedForBandMatrix()}
     * に従う.
     * </p>
     */
    public static final class Executor
            extends SkeletalQRTypeSolver.Executor<BandMatrix, HouseholderQRBand> {

        private static final Executor INSTANCE = new Executor();

        /**
         * 非公開のコンストラクタ, シングルトン.
         */
        private Executor() {
            super();
        }

        @Override
        MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
            return matrix.bandMatrixDimension().isAccepedForBandMatrix()
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        @Override
        Optional<HouseholderQRBand> applyConcretely(BandMatrix matrix, double epsilon) {
            try {
                final double EPSILON_A = 1E-100;
                HouseholderQRBandHelper helper = new HouseholderQRBandHelper(matrix, epsilon + EPSILON_A);

                return Optional.of(new HouseholderQRBand(helper));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
