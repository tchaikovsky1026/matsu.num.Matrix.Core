/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
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
 * 正方帯行列のLU分解を提供する.
 * </p>
 * 
 * <p>
 * 与えられた正方帯行列 A を次の形に分解する: A = LDU. <br>
 * ただし, L: 単位(対角成分が1の)下三角帯行列, D: 対角行列, U:
 * 単位上三角帯行列. <br>
 * 行列が正則であったとしても, 分解できない場合がある(ピボッティングが必要).
 * </p>
 * 
 * <p>
 * メソッド
 * {@link SolvingFactorizationExecutor#accepts(Matrix)}
 * でrejectされる追加条件は次のとおりである.
 * </p>
 * <ul>
 * <li>行列の有効要素数が大きすぎる場合(後述)</li>
 * </ul>
 * 
 * <p>
 * メソッド
 * {@link SolvingFactorizationExecutor#apply(Matrix, double)}
 * で空が返る追加条件は次のとおりである.
 * </p>
 * <ul>
 * <li>ピボッティングが必要な場合</li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i>, 上側帯幅と下側帯幅の大きい方を <i>b</i> として, <br>
 * <i>n</i> * <i>b</i> &gt; {@link Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class LUBandExecutor
        extends SkeletalSolvingFactorizationExecutor<
                BandMatrix, LUTypeSolver>
        implements SolvingFactorizationExecutor<BandMatrix, LUTypeSolver> {

    private static final LUBandExecutor INSTANCE = new LUBandExecutor();

    /**
     * 内部から呼ばれる.
     */
    private LUBandExecutor() {
        super();

        //シングルトンを強制
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    @Override
    MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
        return LUBandFactorizationHelper.acceptedSize(matrix)
                ? MatrixStructureAcceptance.ACCEPTED
                : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
    }

    @Override
    final Optional<? extends LUTypeSolver> applyConcretely(BandMatrix matrix, double epsilon) {
        return LUBandSystem.instanceOf(matrix, epsilon);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * このクラスのインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static LUBandExecutor instance() {
        return INSTANCE;
    }

    private static final class LUBandSystem
            extends InvertibleDeterminantableSystem<Matrix> implements LUTypeSolver {

        private static final String CLASS_STRING = "LU";

        private static final double EPSILON_A = 1E-100;

        private final BandMatrix matrix;

        private final DiagonalMatrix mxD;
        private final LowerUnitriangular mxL;
        private final LowerUnitriangular mxUt;

        /**
         * <p>
         * 与えた行列を分解し, 分解構造を返す. <br>
         * 分解できなかった場合, 空が返る.
         * </p>
         * 
         * <p>
         * このメソッドはエンクロージングクラスから呼ばれ,
         * 必ず構造的にacceptedな引数が与えられる.
         * </p>
         */
        static Optional<LUBandSystem> instanceOf(BandMatrix matrix, double epsilon) {
            try {
                return Optional.of(new LUBandSystem(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }

        /**
         * staticファクトリから呼ばれる.
         * 
         * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合
         */
        private LUBandSystem(BandMatrix matrix, double epsilon) throws ProcessFailedException {
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

        @Override
        protected InverstibleAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues det = new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());
            // A^{-1} = (LDU)^{-1} = U^{-1}D^{-1}L^{-1}
            Matrix invMatrix = Matrix.multiply(
                    this.mxUt.inverse().get().transpose(),
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get());
            return new InverstibleAndDeterminantStruct<>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }

    }
}
