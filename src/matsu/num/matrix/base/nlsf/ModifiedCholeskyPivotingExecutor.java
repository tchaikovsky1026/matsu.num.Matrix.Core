/**
 * 2023.8.24
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;
import matsu.num.matrix.base.exception.ProcessFailedException;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;
import matsu.num.matrix.base.nlsf.helper.fact.Block2OrderSymmetricDiagonalMatrix;
import matsu.num.matrix.base.nlsf.helper.fact.ModifiedCholeskyPivotingFactorizationHelper;

/**
 * 対称行列の部分ピボッティング付き修正Cholesky分解. <br>
 * Bunch-Kaufmanピボッティングにより実装されている. 
 * 
 * <p>
 * このソルバーは, 与えられた対称行列 A を次の形に分解する: A = PLML<sup>T</sup>P<sup>T</sup>. <br>
 * ただし, P: 置換行列, L: 単位(対角成分が1の)下三角行列, <br>
 * M: 1*1 あるいは 2*2の対称ブロック要素を持つブロック対角行列. 
 * </p>
 * 
 * <p>
 * このクラスが提供する{@linkplain SolvingFactorizationExecutor}について,
 * この行列分解が提供する逆行列には{@linkplain Symmetric}が付与されている. 
 * </p>
 * 
 * <p>
 * メソッド{@code apply(matrix, epsilon)}で追加でスローされる例外は次のとおりである, 
 * </p>
 * <ul>
 * <li> {@code IllegalArgumentException 行列の有効要素数が大きすぎる場合(後述)} </li>
 * <li> {@code MatrixNotSymmetricException 対称行列でない場合} </li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を<i>n</i>として, <br>
 * <i>n</i> * (<i>n</i> + 1) {@literal >} {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 15.2
 */
public final class ModifiedCholeskyPivotingExecutor {

    private static SolvingFactorizationExecutor<
            EntryReadableMatrix, LinearEquationSolving<EntryReadableMatrix>> INSTANCE = new ExecutorImpl();

    private ModifiedCholeskyPivotingExecutor() {
        throw new AssertionError();
    }

    /**
     * このクラスの機能を実行するインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static SolvingFactorizationExecutor<
            EntryReadableMatrix, LinearEquationSolving<EntryReadableMatrix>> instance() {
        return INSTANCE;
    }

    private static final class ExecutorImpl
            extends SkeletalSolvingFactorizationExecutor<
                    EntryReadableMatrix, LinearEquationSolving<EntryReadableMatrix>>
            implements SolvingFactorizationExecutor<
                    EntryReadableMatrix, LinearEquationSolving<EntryReadableMatrix>> {

        private static final String CLASS_EXPLANATION = "ModifiedCholeskyPivotingExecutor";

        @Override
        final LinearEquationSolving<EntryReadableMatrix> applyConcretely(EntryReadableMatrix matrix,
                double epsilon) {
            if (!(matrix instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }
            return new ModifiedCholeskyPivotingFactorization(matrix, epsilon);
        }

        @Override
        public String toString() {
            return CLASS_EXPLANATION;
        }
    }

    private static final class ModifiedCholeskyPivotingFactorization
            extends SkeletalLinearEquationSolving<EntryReadableMatrix>
            implements LinearEquationSolving<EntryReadableMatrix> {

        private static final double EPSILON_A = 1E-100;

        private final EntryReadableMatrix matrix;

        private final Block2OrderSymmetricDiagonalMatrix mxM;
        private final LowerUnitriangularEntryReadableMatrix mxL;
        private final OrthogonalMatrix mxP;

        /**
         * ビルダから呼ばれる.
         *
         * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * (dim + 1) > IntMax)
         * @throws ProcessFailedException 行列が特異に近い場合, 成分に極端な値を含み分解が完了できない場合
         */
        private ModifiedCholeskyPivotingFactorization(final EntryReadableMatrix matrix, final double epsilon) {

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
         * 戻り値は対称行列であり, {@linkplain Symmetric}が付与されている.
         */
        @Override
        InverseAndDeterminantStructure<Matrix> calcInverseAndDeterminantStructure() {
            DeterminantValues determinantValues =
                    new DeterminantValues(this.mxM.logAbsDeterminant(), this.mxM.signOfDeterminant());

            // A^{-1} = (PLM(L^T)(P^T))^{-1} = P^{-T}L^{-T}M^{-1}L^{-1}P^{-1} = (P^{-T}L^{-T})M^{-1}(P^{-T}L^{-T})^T
            Matrix invMatrix = Matrix.symmetricMultiply(
                    this.mxM.inverse().get(),
                    Matrix.multiply(
                            this.mxP.inverse().get().transpose(),
                            this.mxL.inverse().get().transpose()));

            return new InverseAndDeterminantStructure<Matrix>(determinantValues, invMatrix);
        }

        @Override
        public String toString() {
            return LinearEquationSolving.toString(this, this.getClass().getSimpleName());
        }
    }

}
