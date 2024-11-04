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

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PermutationMatrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * 対称行列の部分ピボッティング付き修正Cholesky分解を提供する.
 * </p>
 * 
 * <p>
 * このソルバーは, 与えられた対称行列 A を次の形に分解する:
 * A = PLML<sup>T</sup>P<sup>T</sup>. <br>
 * ただし, P: 置換行列, L: 単位(対角成分が1の)下三角行列, <br>
 * M: 1*1 あるいは 2*2の対称ブロック要素を持つブロック対角行列.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@link Symmetric} が付与されている.
 * </p>
 * 
 * <p>
 * メソッド
 * {@link SolvingFactorizationExecutor#accepts(Matrix)}
 * でrejectされる追加条件は次のとおりである.
 * </p>
 * <ul>
 * <li>行列の有効要素数が大きすぎる場合(後述)</li>
 * <li>対称行列でない場合</li>
 * </ul>
 * 
 * <p>
 * メソッド
 * {@link SolvingFactorizationExecutor#apply(Matrix, double)}
 * で空が返る追加条件は無い.
 * </p>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i> として, <br>
 * <i>n</i> * (<i>n</i> + 1) &gt; {@link Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class ModifiedCholeskyPivotingExecutor
        extends SkeletalSolvingFactorizationExecutor<
                EntryReadableMatrix, LUTypeSolver>
        implements SolvingFactorizationExecutor<EntryReadableMatrix, LUTypeSolver> {

    private static final ModifiedCholeskyPivotingExecutor INSTANCE = new ModifiedCholeskyPivotingExecutor();

    /**
     * 内部から呼ばれる.
     */
    private ModifiedCholeskyPivotingExecutor() {
        super();

        //シングルトンを強制
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    @Override
    MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
        if (!(matrix instanceof Symmetric)) {
            return MatrixRejectionInLSF.REJECTED_BY_NOT_SYMMETRIC.get();
        }

        return ModifiedCholeskyPivotingFactorizationHelper.acceptedSize(matrix)
                ? MatrixStructureAcceptance.ACCEPTED
                : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
    }

    @Override
    final Optional<? extends LUTypeSolver> applyConcretely(EntryReadableMatrix matrix,
            double epsilon) {
        return ModifiedCholeskyPivotingSystem.instanceOf(matrix, epsilon);
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
    public static ModifiedCholeskyPivotingExecutor instance() {
        return INSTANCE;
    }

    private static final class ModifiedCholeskyPivotingSystem
            extends InvertibleDeterminantableSystem<Matrix>
            implements LUTypeSolver {

        private static final String CLASS_STRING = "ModifiedCholesky-Pivoting";

        private static final double EPSILON_A = 1E-100;

        private final EntryReadableMatrix matrix;

        private final Block2OrderSymmetricDiagonalMatrix mxM;
        private final LowerUnitriangular mxL;
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
        static Optional<ModifiedCholeskyPivotingSystem> instanceOf(
                final EntryReadableMatrix matrix, final double epsilon) {
            try {
                return Optional.of(new ModifiedCholeskyPivotingSystem(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }

        /**
         * staticファクトリから呼ばれる.
         * 
         * @throws ProcessFailedException 行列が特異に近い場合
         */
        private ModifiedCholeskyPivotingSystem(final EntryReadableMatrix matrix, final double epsilon)
                throws ProcessFailedException {

            //ここで例外が発生する場合がある
            ModifiedCholeskyPivotingFactorizationHelper fact = new ModifiedCholeskyPivotingFactorizationHelper(
                    matrix, epsilon + EPSILON_A);

            this.matrix = matrix;

            this.mxM = fact.getMxM();
            this.mxL = fact.getMxL();
            this.mxP = fact.getMxP();
        }

        @Override
        public EntryReadableMatrix target() {
            return this.matrix;
        }

        /**
         * {@inheritDoc } <br>
         * 戻り値は対称行列であり, {@link Symmetric}が付与されている.
         */
        @Override
        protected InverstibleAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues determinantValues =
                    new DeterminantValues(this.mxM.logAbsDeterminant(), this.mxM.signOfDeterminant());

            // A^{-1} = (PLM(L^T)(P^T))^{-1} = P^{-T}L^{-T}M^{-1}L^{-1}P^{-1} = (P^{-T}L^{-T})M^{-1}(P^{-T}L^{-T})^T
            Matrix invMatrix = Matrix.symmetricMultiply(
                    this.mxM.inverse().get(),
                    Matrix.multiply(
                            this.mxP.inverse().get().transpose(),
                            this.mxL.inverse().get().transpose()));

            return new InverstibleAndDeterminantStruct<Matrix>(determinantValues, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }
    }
}
