/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.7
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * 正方帯行列のLU分解を表す. <br>
 * これは, 正方帯行列 A の A = LDU の形での分解である. <br>
 * ただし, L: 単位 (対角成分が1の) 下三角帯行列, D: 対角行列, U:
 * 単位上三角帯行列.
 * </p>
 * 
 * <p>
 * この分解は行列が正則であったとしても, 分解できない場合がある. <br>
 * これはピボッティングが必要な行列である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.2
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
     * @return -
     * @deprecated (外部からの呼び出し不可)
     */
    @Deprecated
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
     * @return -
     * @deprecated (外部からの呼び出し不可)
     */
    @Deprecated
    @Override
    String solverName() {
        return super.solverName();
    }

    /**
     * <p>
     * 正方帯行列のLU分解のエグゼキュータ.
     * </p>
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
     * 有効要素数が大きすぎるとは,
     * 行列の行数 (= 列数) を <i>n</i>, 上側帯幅と下側帯幅の大きい方を <i>b</i> として,
     * <i>n</i> * <i>b</i> &gt; {@link Integer#MAX_VALUE}
     * である状態である.
     * </p>
     */
    public static final class Executor
            extends SkeletalSolvingFactorizationExecutor<
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
         * @param matrix -
         * @return -
         * @deprecated (外部からの呼び出し不可)
         */
        @Deprecated
        @Override
        MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
            return LUBandFactorizationHelper.acceptedSize(matrix)
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        /**
         * -
         * 
         * @param matrix -
         * @param epsilon -
         * @return -
         * @deprecated (外部からの呼び出し不可)
         */
        @Deprecated
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
