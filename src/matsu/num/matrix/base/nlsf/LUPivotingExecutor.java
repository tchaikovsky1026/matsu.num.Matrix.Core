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

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PermutationMatrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * 正方行列の部分ピボッティング付きLU分解を提供する.
 * </p>
 * 
 * <p>
 * 与えられた正方行列 A を次の形に分解する: A = PLDU. <br>
 * ただし, P: 置換行列, L: 単位(対角成分が1の)下三角行列, D: 対角行列, U: 単位上三角行列.
 * </p>
 * 
 * <p>
 * メソッド
 * {@linkplain SolvingFactorizationExecutor#accepts(Matrix)}
 * でrejectされる追加条件は次のとおりである.
 * </p>
 * 
 * <ul>
 * <li>行列の有効要素数が大きすぎる場合(後述)</li>
 * </ul>
 * 
 * <p>
 * メソッド
 * {@linkplain SolvingFactorizationExecutor#apply(Matrix, double)}
 * で空が返る追加条件は無い.
 * </p>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i> として, <br>
 * <i>n</i> * <i>n</i> &gt; {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class LUPivotingExecutor
        extends SkeletalSolvingFactorizationExecutor<
                EntryReadableMatrix, LUTypeSolver>
        implements SolvingFactorizationExecutor<EntryReadableMatrix, LUTypeSolver> {

    private static final LUPivotingExecutor INSTANCE = new LUPivotingExecutor();

    /**
     * 内部から呼ばれる.
     */
    private LUPivotingExecutor() {
        super();

        //シングルトンを強制
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    @Override
    final MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
        return LUPivotingFactorizationHelper.acceptedSize(matrix)
                ? MatrixStructureAcceptance.ACCEPTED
                : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
    }

    @Override
    final Optional<? extends LUTypeSolver> applyConcretely(EntryReadableMatrix matrix,
            double epsilon) {
        return LUPivotingSystem.instanceOf(matrix, epsilon);
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
    public static LUPivotingExecutor instance() {
        return INSTANCE;
    }

    private static final class LUPivotingSystem
            extends InvertibleDeterminantableSystem<Matrix> implements LUTypeSolver {

        private static final String CLASS_STRING = "LU-Pivoting";

        private static final double EPSILON_A = 1E-100;

        private final EntryReadableMatrix matrix;

        private final DiagonalMatrix mxD;
        private final LowerUnitriangular mxL;
        private final LowerUnitriangular mxUt;
        private final PermutationMatrix mxP;

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
        static Optional<LUPivotingSystem> instanceOf(final EntryReadableMatrix matrix, final double epsilon) {

            try {
                return Optional.of(new LUPivotingSystem(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }

        /**
         * staticファクトリから呼ばれる.
         *
         * @throws ProcessFailedException 行列が特異に近い場合
         */
        private LUPivotingSystem(final EntryReadableMatrix matrix, final double epsilon)
                throws ProcessFailedException {

            //ここで例外が発生する可能性がある
            LUPivotingFactorizationHelper fact = new LUPivotingFactorizationHelper(matrix, epsilon + EPSILON_A);

            this.matrix = matrix;

            this.mxD = fact.getMxD();
            this.mxL = fact.getMxL();
            this.mxUt = fact.getMxUt();
            this.mxP = fact.getMxP();
        }

        @Override
        public EntryReadableMatrix target() {
            return this.matrix;
        }

        @Override
        protected InverstibleAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues det = new DeterminantValues(
                    this.mxD.logAbsDeterminant(), this.mxP.signOfDeterminant() * this.mxD.signOfDeterminant());

            // A^{-1} = (PLDU)^{-1} = U^{-1}D^{-1}L^{-1}P^{-1}
            Matrix invMatrix = Matrix.multiply(
                    this.mxUt.inverse().get().transpose(),
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get(),
                    this.mxP.inverse().get());

            return new InverstibleAndDeterminantStruct<Matrix>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }
    }
}
