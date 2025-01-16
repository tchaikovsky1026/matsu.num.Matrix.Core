/*
 * Copyright © 2024 Matsuura Y.
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

import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.PermutationMatrix;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 正方行列の部分ピボッティング付きLU分解を表す. <br>
 * これは, 正方行列 A の A = PLDU の形での分解である. <br>
 * ただし, P: 置換行列, L: 単位 (対角成分が1の) 下三角行列, D: 対角行列, U: 単位上三角行列.
 * 
 * @author Matsuura Y.
 */
public final class LUPivoting extends SkeletalLUTypeSolver<EntryReadableMatrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final EntryReadableMatrix matrix;

    private final DiagonalMatrix mxD;
    private final LowerUnitriangular mxL;
    private final LowerUnitriangular mxUt;
    private final PermutationMatrix mxP;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static final LUPivoting.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     *
     * @throws ProcessFailedException 行列が特異に近い場合
     */
    private LUPivoting(final EntryReadableMatrix matrix, final double epsilon)
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
     * 正方行列の部分ピボッティング付きLU分解のエグゼキュータ.
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
     * {@link SolvingFactorizationExecutor} に加わる追加条件は無い.
     * </p>
     * 
     * <p>
     * このクラスのインスタンスは, {@link LUPivoting#executor()} メソッドにより得ることができる. <br>
     * 実質的にシングルトンである.
     * </p>
     * 
     * <hr>
     * 
     * <p>
     * 有効要素数が大きすぎるかどうかは,
     * {@link MatrixDimension#isAccepedForDenseMatrix()}
     * に従う.
     * </p>
     */
    public static final class Executor
            extends SkeletalSolvingFactorizationExecutor<EntryReadableMatrix, LUPivoting> {

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
         * 
         * @return -
         */
        @Override
        final MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
            return matrix.matrixDimension().isAccepedForDenseMatrix()
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
        final Optional<LUPivoting>
                applyConcretely(EntryReadableMatrix matrix, double epsilon) {

            try {
                return Optional.of(new LUPivoting(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }
}
