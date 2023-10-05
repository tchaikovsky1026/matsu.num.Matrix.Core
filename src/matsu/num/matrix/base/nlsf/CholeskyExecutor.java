/**
 * 2023.8.24
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;
import matsu.num.matrix.base.exception.ProcessFailedException;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;
import matsu.num.matrix.base.nlsf.helper.fact.CholeskyFactorizationHelper;

/**
 * 対称行列のCholesky分解. 
 * 
 * <p>
 * 与えられた対称行列 A を次の形に分解する: A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>. <br>
 * ただし,
 * D<sup>1/2</sup>: 正定値対角行列, L: 単位(対角成分が1の)下三角行列. <br>
 * 正定値行列をサポートする. 
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には{@linkplain Symmetric}が付与されている. 
 * </p>
 * 
 * <p>
 * このクラスが提供する{@linkplain SolvingFactorizationExecutor}について,
 * メソッド{@code apply(matrix, epsilon)}で追加でスローされる例外は次のとおりである, 
 * </p>
 * <ul>
 * <li> {@code IllegalArgumentException 行列の有効要素数が大きすぎる場合(後述)} </li>
 * <li> {@code MatrixNotSymmetricException 対称行列でない場合} </li>
 * <li> {@code ProcessFailedException 行列が正定値でない場合} </li>
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
public final class CholeskyExecutor {

    private static final SolvingFactorizationExecutor<
            EntryReadableMatrix, AsymmetricSqrtFactorization<EntryReadableMatrix>> INSTANCE = new ExecutorImpl();

    private CholeskyExecutor() {
        throw new AssertionError();
    }

    /**
     * このクラスの機能を実行するインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static SolvingFactorizationExecutor<
            EntryReadableMatrix, AsymmetricSqrtFactorization<EntryReadableMatrix>> instance() {
        return INSTANCE;
    }

    private static final class ExecutorImpl
            extends SkeletalSolvingFactorizationExecutor<
                    EntryReadableMatrix, AsymmetricSqrtFactorization<EntryReadableMatrix>>
            implements SolvingFactorizationExecutor<
                    EntryReadableMatrix, AsymmetricSqrtFactorization<EntryReadableMatrix>> {

        private static final String CLASS_EXPLANATION = "CholeskyExecutor";

        @Override
        final AsymmetricSqrtFactorization<EntryReadableMatrix> applyConcretely(EntryReadableMatrix matrix,
                double epsilon) {
            if (!(matrix instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("対称行列でない");
            }
            return new CholeskyFactorization(matrix, epsilon);
        }

        @Override
        public String toString() {
            return CLASS_EXPLANATION;
        }
    }

    /**
     * コレスキー分解の具象クラスを提供する. 
     */
    private static final class CholeskyFactorization
            extends SkeletalAsymmetricSqrtFactorization<EntryReadableMatrix>
            implements AsymmetricSqrtFactorization<EntryReadableMatrix> {

        private static final double EPSILON_A = 1E-100;

        private final EntryReadableMatrix matrix;

        private final DiagonalMatrix mxSqrtD;
        private final LowerUnitriangularEntryReadableMatrix mxL;

        /**
         * ビルダから呼ばれる.
         *
         * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * (dim + 1) > IntMax)
         * @throws ProcessFailedException 行列が正定値でない場合, 成分に極端な値を含み分解が完了できない場合
         */
        private CholeskyFactorization(final EntryReadableMatrix matrix, final double epsilon) {
            CholeskyFactorizationHelper fact = new CholeskyFactorizationHelper(matrix, epsilon + EPSILON_A);

            this.matrix = matrix;

            this.mxL = fact.getMxL();
            this.mxSqrtD = fact.getMxSqrtD();
        }

        @Override
        public EntryReadableMatrix target() {
            return this.matrix;
        }

        @Override
        final LinearEquationSolving<Matrix> calcAsymmetricSqrtSystem() {
            return new AsymmetricSqrtSystem(mxSqrtD, mxL);
        }

        @Override
        public String toString() {
            return LinearEquationSolving.toString(this, this.getClass().getSimpleName());
        }

        private static final class AsymmetricSqrtSystem extends SkeletalLinearEquationSolving<Matrix> {

            private final DiagonalMatrix mxSqrtD;
            private final LowerUnitriangularEntryReadableMatrix mxL;

            private final Matrix asymmSqrt;

            AsymmetricSqrtSystem(DiagonalMatrix mxSqrtD, LowerUnitriangularEntryReadableMatrix mxL) {
                super();
                this.mxSqrtD = mxSqrtD;
                this.mxL = mxL;

                // A = BB^Tとすれば,
                // B = LD^{1/2}
                this.asymmSqrt = Matrix.multiply(this.mxL, this.mxSqrtD);
            }

            @Override
            public Matrix target() {
                return this.asymmSqrt;
            }

            @Override
            InverseAndDeterminantStructure<? extends Matrix> calcInverseAndDeterminantStructure() {
                // A = BB^Tとすれば,
                // B^{-1} = D^{-1/2}L^{-1}
                final Matrix asymmInvSqrt = Matrix.multiply(
                        this.mxSqrtD.inverse().get(),
                        this.mxL.inverse().get());

                final double logDetSqrtL = this.mxSqrtD.logAbsDeterminant();

                return new InverseAndDeterminantStructure<Matrix>(
                        new DeterminantValues(logDetSqrtL, 1),
                        asymmInvSqrt);
            }

        }

    }

}
