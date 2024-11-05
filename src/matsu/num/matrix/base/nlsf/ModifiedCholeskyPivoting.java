/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.5
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
 * 対称行列の部分ピボッティング付き修正Cholesky分解を表す. <br>
 * これは, 対称行列 A を A = PLML<sup>T</sup>P<sup>T</sup> の形での分解である. <br>
 * ただし, P: 置換行列, L: 単位 (対角成分が1の) 下三角行列,
 * M: 1*1 あるいは 2*2の対称ブロック要素を持つブロック対角行列.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@link Symmetric} が付与されている.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public final class ModifiedCholeskyPivoting
        extends SkeletalLUTypeSolver<EntryReadableMatrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final EntryReadableMatrix matrix;

    private final Block2OrderSymmetricDiagonalMatrix mxM;
    private final LowerUnitriangular mxL;
    private final PermutationMatrix mxP;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static final ModifiedCholeskyPivoting.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     * 
     * @throws ProcessFailedException 行列が特異に近い場合
     */
    private ModifiedCholeskyPivoting(final EntryReadableMatrix matrix, final double epsilon)
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
     * <i>(外部からの呼び出し不可)</i>
     * 
     * @return -
     */
    @Override
    protected InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
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

    /**
     * <i>(外部からの呼び出し不可)</i>
     * 
     * @return -
     */
    @Override
    protected String solverName() {
        return super.solverName();
    }

    /**
     * <p>
     * 対称行列の部分ピボッティング付き修正Cholesky分解を提供する.
     * </p>
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
     * {@link SolvingFactorizationExecutor} に加わる追加条件は無い.
     * </p>
     * 
     * <p>
     * このクラスのインスタンスは, {@link ModifiedCholeskyPivoting#executor()}
     * メソッドにより得ることができる. <br>
     * 実質的にシングルトンである.
     * </p>
     * 
     * <hr>
     * 
     * <p>
     * 有効要素数が大きすぎるとは,
     * 行列の行数 (= 列数) を <i>n</i> として,
     * <i>n</i> * (<i>n</i> + 1) &gt; {@link Integer#MAX_VALUE}
     * である状態である.
     * </p>
     * 
     */
    public static final class Executor
            extends SkeletalSolvingFactorizationExecutor<
                    EntryReadableMatrix, ModifiedCholeskyPivoting> {

        private static final Executor INSTANCE =
                new Executor();

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
         * <i>(外部からの呼び出し不可)</i>
         * 
         * @param matrix -
         * @return -
         */
        @Override
        protected MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
            if (!(matrix instanceof Symmetric)) {
                return MatrixRejectionInLSF.REJECTED_BY_NOT_SYMMETRIC.get();
            }

            return ModifiedCholeskyPivotingFactorizationHelper.acceptedSize(matrix)
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        /**
         * <i>(外部からの呼び出し不可)</i>
         * 
         * @param matrix -
         * @param epsilon -
         * @return -
         */
        @Override
        protected final Optional<ModifiedCholeskyPivoting> applyConcretely(EntryReadableMatrix matrix,
                double epsilon) {
            try {
                return Optional.of(new ModifiedCholeskyPivoting(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
