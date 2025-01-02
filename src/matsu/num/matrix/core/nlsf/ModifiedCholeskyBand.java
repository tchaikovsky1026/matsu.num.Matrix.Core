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
 * 対称帯行列の修正Cholesky分解を表す. <br>
 * これは, 対称帯行列 A を A = LDL<sup>T</sup> の形での分解である. <br>
 * ただし, D: 対角行列, L:
 * 単位 (対角成分が1の) 下三角帯行列.
 * 
 * <p>
 * この分解は行列が正則であったとしても, 分解できない場合がある. <br>
 * これはピボッティングが必要な行列である.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@link Symmetric} が付与されている.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
public final class ModifiedCholeskyBand extends SkeletalLUTypeSolver<BandMatrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final BandMatrix matrix;

    private final DiagonalMatrix mxD;
    private final LowerUnitriangular mxL;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static final ModifiedCholeskyBand.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     *
     * @throws ProcessFailedException 行列が特異あるいはピボッティングが必要な場合
     */
    private ModifiedCholeskyBand(final BandMatrix matrix, final double epsilon)
            throws ProcessFailedException {

        //ここで例外が発生する可能性がある
        ModifiedCholeskyBandFactorizationHelper fact = new ModifiedCholeskyBandFactorizationHelper(
                matrix, epsilon + EPSILON_A);

        this.matrix = matrix;

        this.mxD = fact.getMxD();
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
    InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
        DeterminantValues det =
                new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());

        // A^{-1} = (LD(L^T))^{-1} = L^{-T}D^{-1}L^{-1} = (L^{-T})D^{-1}(L^{-T})^T
        Matrix invMatrix = Matrix.symmetricMultiply(
                this.mxD.inverse().get(),
                this.mxL.inverse().get().transpose());

        return new InverstibleAndDeterminantStruct<Matrix>(det, invMatrix);
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
     * 対称帯行列の修正Cholesky分解のエグゼキュータ.
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
     * <li>ピボッティングが必要な場合</li>
     * </ul>
     * 
     * <p>
     * このクラスのインスタンスは, {@link ModifiedCholeskyBand#executor()}
     * メソッドにより得ることができる. <br>
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
            extends SkeletalSolvingFactorizationExecutor<BandMatrix, ModifiedCholeskyBand> {

        private static Executor INSTANCE = new Executor();

        /**
         * 内部から呼ばれる.
         */
        private Executor() {
            super();

            //シングルトンを強制
            if (Objects.nonNull(INSTANCE)) {
                throw new AssertionError();
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
        Optional<ModifiedCholeskyBand> applyConcretely(BandMatrix matrix, double epsilon) {
            try {
                return Optional.of(new ModifiedCholeskyBand(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
