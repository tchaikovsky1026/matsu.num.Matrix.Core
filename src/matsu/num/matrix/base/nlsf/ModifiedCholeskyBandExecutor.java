/**
 * 2023.8.24
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;
import matsu.num.matrix.base.exception.ProcessFailedException;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;
import matsu.num.matrix.base.nlsf.helper.fact.ModifiedCholeskyBandFactorizationHelper;

/**
 * 対称帯行列の修正Cholesky分解. 
 * 
 * <p>
 * 与えられた対称行列 A を次の形に分解する: A = LDL<sup>T</sup>. ただし, D: 対角行列, L:
 * 単位(対角成分が1の)下三角帯行列. <br>
 * 行列が正則であったとしても, 分解できない場合がある(ピボッティングが必要). 
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
 * <li> {@code ProcessFailedException ピボッティングが必要な場合} </li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を<i>n</i>, 片側帯幅を<i>b</i>として, <br>
 * <i>n</i> * <i>b</i> {@literal >} {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 15.2
 */
public final class ModifiedCholeskyBandExecutor {

    private static SolvingFactorizationExecutor<
            BandMatrix, LinearEquationSolving<BandMatrix>> INSTANCE = new ExecutorImpl();

    private ModifiedCholeskyBandExecutor() {
        throw new AssertionError();
    }

    /**
     * このクラスの機能を実行するインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static SolvingFactorizationExecutor<
            BandMatrix, LinearEquationSolving<BandMatrix>> instance() {
        return INSTANCE;
    }

    private static final class ExecutorImpl
            extends SkeletalSolvingFactorizationExecutor<
                    BandMatrix, LinearEquationSolving<BandMatrix>>
            implements SolvingFactorizationExecutor<
                    BandMatrix, LinearEquationSolving<BandMatrix>> {

        private static final String CLASS_EXPLANATION = "ModifiedCholeskyBandExecutor";

        @Override
        final LinearEquationSolving<BandMatrix> applyConcretely(BandMatrix matrix, double epsilon) {
            if (!(matrix instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }
            return new ModifiedCholeskyBandFactorization(matrix, epsilon);
        }

        @Override
        public String toString() {
            return CLASS_EXPLANATION;
        }
    }

    private static final class ModifiedCholeskyBandFactorization
            extends SkeletalLinearEquationSolving<BandMatrix>
            implements LinearEquationSolving<BandMatrix> {

        private static final double EPSILON_A = 1E-100;

        private final BandMatrix matrix;

        private final DiagonalMatrix mxD;
        private final LowerUnitriangularEntryReadableMatrix mxL;

        /**
         * ビルダから呼ばれる.
         *
         * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax)
         * @throws ProcessFailedException 行列が特異あるいはピボッティングが必要な場合, 成分に極端な値を含み分解が完了できない場合
         */
        private ModifiedCholeskyBandFactorization(final BandMatrix matrix, final double epsilon) {

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
         * {@inheritDoc } <br>
         * 戻り値は対称行列であり, {@linkplain Symmetric}が付与されている.
         */
        @Override
        InverseAndDeterminantStructure<Matrix> calcInverseAndDeterminantStructure() {
            DeterminantValues det = new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());

            // A^{-1} = (LD(L^T))^{-1} = L^{-T}D^{-1}L^{-1} = (L^{-T})D^{-1}(L^{-T})^T
            Matrix invMatrix = Matrix.symmetricMultiply(
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get().transpose());

            return new InverseAndDeterminantStructure<Matrix>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LinearEquationSolving.toString(this, this.getClass().getSimpleName());
        }

    }

}
