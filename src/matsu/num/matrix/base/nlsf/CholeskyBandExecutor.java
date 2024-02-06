/**
 * 2024.2.5
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * 対称帯行列のCholesky分解を提供する.
 * </p>
 * 
 * <p>
 * 与えられた正定値対称行列 A を次の形に分解する:
 * A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>. <br>
 * ただし,
 * D<sup>1/2</sup>: 正定値対角行列, L: 単位(対角成分が1の)下三角帯行列. <br>
 * 行列が正定値であることが, 分解できることの必要十分条件である.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@linkplain Symmetric} が付与されている.
 * </p>
 * 
 * <p>
 * メソッド
 * {@linkplain SolvingFactorizationExecutor#accepts(Matrix)}
 * でrejectされる追加条件は次のとおりである.
 * </p>
 * <ul>
 * <li>行列の有効要素数が大きすぎる場合(後述)</li>
 * <li>対称行列でない場合</li>
 * </ul>
 * 
 * <p>
 * メソッド
 * {@linkplain SolvingFactorizationExecutor#apply(Matrix, double)}
 * で空が返る追加条件は次のとおりである.
 * </p>
 * <ul>
 * <li>正定値行列でない場合</li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i>, 片側帯幅を <i>b</i> として, <br>
 * <i>n</i> * <i>b</i> &gt; {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 20.0
 */
public final class CholeskyBandExecutor
        extends SkeletalSolvingFactorizationExecutor<BandMatrix, SymmetrizedSquareTypeSolver>
        implements SolvingFactorizationExecutor<BandMatrix, SymmetrizedSquareTypeSolver> {

    private static final CholeskyBandExecutor INSTANCE = new CholeskyBandExecutor();

    /**
     * 内部から呼ばれる.
     */
    private CholeskyBandExecutor() {
        super();
        
        //シングルトンを強制
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    @Override
    MatrixStructureAcceptance acceptsConcretely(BandMatrix matrix) {
        if (!(matrix instanceof Symmetric)) {
            return MatrixRejectionInLSF.REJECTED_BY_NOT_SYMMETRIC.get();
        }

        return CholeskyBandFactorizationHelper.acceptedSize(matrix)
                ? MatrixStructureAcceptance.ACCEPTED
                : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
    }

    @Override
    Optional<? extends SymmetrizedSquareTypeSolver> applyConcretely(BandMatrix matrix, double epsilon) {
        return CholeskyBandSystem.instanceOf(matrix, epsilon);
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
    public static CholeskyBandExecutor instance() {
        return INSTANCE;
    }

    private static final class CholeskyBandSystem
            extends SkeletalSymmetrizedSquareTypeSolver
            implements SymmetrizedSquareTypeSolver {

        private static final String CLASS_STRING = "Cholesky";

        private static final double EPSILON_A = 1E-100;

        private final BandMatrix matrix;

        private final DiagonalMatrix mxSqrtD;
        private final LowerUnitriangular mxL;

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
        static Optional<CholeskyBandSystem> instanceOf(final BandMatrix matrix, final double epsilon) {
            try {
                return Optional.of(new CholeskyBandSystem(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }

        /**
         * staticファクトリから呼ばれる.
         *
         * @throws ProcessFailedException 行列が正定値でない場合
         */
        private CholeskyBandSystem(final BandMatrix matrix, final double epsilon) throws ProcessFailedException {
            CholeskyBandFactorizationHelper fact =
                    new CholeskyBandFactorizationHelper(matrix, epsilon + EPSILON_A);

            this.matrix = matrix;

            this.mxSqrtD = fact.getMxSqrtD();
            this.mxL = fact.getMxL();
        }

        @Override
        public BandMatrix target() {
            return this.matrix;
        }

        @Override
        final InvertibleDeterminantableSystem<Matrix> createAsymmetricSqrtSystem() {
            return new AsymmetricSqrtSystem(mxSqrtD, mxL);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }
    }

    private static final class AsymmetricSqrtSystem
            extends InvertibleDeterminantableSystem<Matrix> {

        private final DiagonalMatrix mxSqrtD;
        private final LowerUnitriangular mxL;

        private final Matrix asymmSqrt;

        AsymmetricSqrtSystem(DiagonalMatrix mxSqrtD, LowerUnitriangular mxL) {
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
        protected InverstibleAndDeterminantStruct<? extends Matrix> calcInverseDeterminantStruct() {
            // A = BB^Tとすれば,
            // B^{-1} = D^{-1/2}L^{-1}
            final Matrix asymmInvSqrt = Matrix.multiply(
                    this.mxSqrtD.inverse().get(),
                    this.mxL.inverse().get());

            final double logDetSqrtL = this.mxSqrtD.logAbsDeterminant();

            return new InverstibleAndDeterminantStruct<Matrix>(
                    new DeterminantValues(logDetSqrtL, 1),
                    asymmInvSqrt);
        }

    }

}
