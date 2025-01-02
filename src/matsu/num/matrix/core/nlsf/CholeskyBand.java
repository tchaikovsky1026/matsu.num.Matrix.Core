/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.core.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 対称帯行列のCholesky分解を表す. <br>
 * これは, 正定値対称帯行列 A の A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>
 * の形での分解である. <br>
 * ただし,
 * D<sup>1/2</sup>: 正定値対角行列, L: 単位 (対角成分が1の) 下三角帯行列. <br>
 * A = BB<sup>T</sup> の分解として見ると, B = LD<sup>1/2</sup> である.
 * 
 * <p>
 * 行列が正定値であることが, 分解できることの必要十分条件である.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@link Symmetric} が付与されている.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
public final class CholeskyBand
        extends SkeletalSymmetrizedSquareTypeSolver<BandMatrix, Matrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final BandMatrix matrix;

    private final DiagonalMatrix mxSqrtD;
    private final LowerUnitriangular mxL;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static CholeskyBand.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     *
     * @throws ProcessFailedException 行列が正定値でない場合
     */
    private CholeskyBand(final BandMatrix matrix, final double epsilon)
            throws ProcessFailedException {
        CholeskyBandFactorizationHelper fact =
                new CholeskyBandFactorizationHelper(matrix, epsilon + EPSILON_A);

        this.matrix = matrix;

        this.mxSqrtD = fact.getMxSqrtD();
        this.mxL = fact.getMxL();
    }

    @Override
    public BandMatrix target() {
        return this.matrix;
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    final InversionDeterminantableImplementation<Matrix, Matrix> createAsymmetricSqrtSystem() {
        return new AsymmetricSqrtSystem(mxSqrtD, mxL);
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    String solverName() {
        return super.solverName();
    }

    /**
     * 対称帯行列のCholesky分解のエグゼキュータ.
     * 
     * <p>
     * {@code accepts} メソッドでrejectされる条件は,
     * {@link SolvingFactorizationExecutor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>行列の有効要素数が大きすぎる場合 (後述)</li>
     * <li>対称行列でない場合</li>
     * </ul>
     * 
     * <p>
     * {@code apply} メソッドで空が返る条件は,
     * {@link SolvingFactorizationExecutor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>正定値行列でない場合</li>
     * </ul>
     * 
     * <p>
     * このクラスのインスタンスは, {@link CholeskyBand#executor()} メソッドにより得ることができる. <br>
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
            extends SkeletalSolvingFactorizationExecutor<BandMatrix, CholeskyBand> {

        /**
         * 唯一のインスタンス
         */
        private static final Executor INSTANCE = new Executor();

        /**
         * 非公開コンストラクタ.
         */
        private Executor() {
            if (Objects.nonNull(INSTANCE)) {
                throw new AssertionError("シングルトンを強制");
            }
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
            if (!(matrix instanceof Symmetric)) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SYMMETRIC.get();
            }

            return matrix.bandMatrixDimension().isAccepedForBandMatrix()
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        Optional<CholeskyBand> applyConcretely(BandMatrix matrix, double epsilon) {
            try {
                return Optional.of(new CholeskyBand(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }

    private static final class AsymmetricSqrtSystem
            extends InversionDeterminantableImplementation<Matrix, Matrix> {

        private final DiagonalMatrix mxSqrtD;
        private final LowerUnitriangular mxL;

        private final Matrix asymmSqrt;

        AsymmetricSqrtSystem(DiagonalMatrix mxSqrtD, LowerUnitriangular mxL) {
            super();
            this.mxSqrtD = mxSqrtD;
            this.mxL = mxL;

            // A = BB^Tとすれば,
            // B = LD^{1/2}
            this.asymmSqrt = Matrix.multiply(this.mxL, this.mxSqrtD);
        }

        @Override
        public Matrix target() {
            return this.asymmSqrt;
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
            // A = BB^Tとすれば,
            // B^{-1} = D^{-1/2}L^{-1}
            final Matrix asymmInvSqrt = Matrix.multiply(
                    this.mxSqrtD.inverse().get(),
                    this.mxL.inverse().get());

            final double logDetSqrtL = this.mxSqrtD.logAbsDeterminant();

            return new InverstibleAndDeterminantStruct<Matrix>(
                    new DeterminantValues(logDetSqrtL, 1),
                    asymmInvSqrt);
        }
    }
}
