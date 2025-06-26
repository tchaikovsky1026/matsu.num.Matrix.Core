/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 正方帯行列のLU分解を表す. <br>
 * これは, 正方帯行列 A の A = LDU の形での分解である. <br>
 * ただし, L: 単位 (対角成分が1の) 下三角帯行列, D: 対角行列, U:
 * 単位上三角帯行列.
 * 
 * <p>
 * この分解は行列が正則であったとしても, 分解できない場合がある. <br>
 * これはピボッティングが必要な行列である.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class LUBand extends SkeletalLUTypeSolver<BandMatrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final BandMatrix matrix;

    private final DiagonalMatrix mxD;
    private final LowerUnitriangular mxL;
    private final LowerUnitriangular mxUt;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static final LUBand.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     * 
     * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合
     */
    private LUBand(BandMatrix matrix, double epsilon) throws ProcessFailedException {
        super();
        LUBandFactorizationHelper fact = new LUBandFactorizationHelper(matrix, epsilon + EPSILON_A);

        this.matrix = matrix;

        this.mxD = fact.getMxD();
        this.mxL = fact.getMxL();
        this.mxUt = fact.getMxUt();
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
     */
    @Override
    InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
        DeterminantValues det =
                new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());
        // A^{-1} = (LDU)^{-1} = U^{-1}D^{-1}L^{-1}
        Matrix invMatrix = Matrix.multiply(
                this.mxUt.inverse().get().transpose(),
                this.mxD.inverse().get(),
                this.mxL.inverse().get());
        return new InverstibleAndDeterminantStruct<>(det, invMatrix);
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     */
    @Override
    String solverName() {
        return super.solverName();
    }

    /**
     * 正方帯行列のLU分解のエグゼキュータ.
     * 
     * <p>
     * {@code accepts} メソッドでrejectされる条件は,
     * {@link SolvingFactorizationExecutor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>行列の有効要素数が大きすぎる場合 (後述)</li>
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
     * このクラスのインスタンスは, {@link LUBand#executor()} メソッドにより得ることができる. <br>
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
            extends SkeletalLUTypeSolver.Executor<
                    BandMatrix, LUBand> {

        private static final Executor INSTANCE = new Executor();

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
         */
        @Override
        MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
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
         */
        @Override
        final Optional<LUBand> applyConcretely(BandMatrix matrix, double epsilon) {
            try {
                return Optional.of(new LUBand(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
