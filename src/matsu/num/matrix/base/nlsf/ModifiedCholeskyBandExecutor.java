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
 * 対称帯行列の修正Cholesky分解を提供する.
 * </p>
 * 
 * <p>
 * 与えられた対称行列 A を次の形に分解する: A = LDL<sup>T</sup>. <br>
 * ただし, D: 対角行列, L:
 * 単位(対角成分が1の)下三角帯行列. <br>
 * 行列が正則であったとしても, 分解できない場合がある(ピボッティングが必要).
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
 * <li>ピボッティングが必要な場合</li>
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
public final class ModifiedCholeskyBandExecutor
        extends SkeletalSolvingFactorizationExecutor<
                BandMatrix, LUTypeSolver>
        implements SolvingFactorizationExecutor<BandMatrix, LUTypeSolver> {

    private static ModifiedCholeskyBandExecutor INSTANCE = new ModifiedCholeskyBandExecutor();

    /**
     * 内部から呼ばれる.
     */
    private ModifiedCholeskyBandExecutor() {
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

        return ModifiedCholeskyBandFactorizationHelper.acceptedSize(matrix)
                ? MatrixStructureAcceptance.ACCEPTED
                : MatrixRejectionInLSF.REJECTED_BY_TOO_MANY_ELEMENTS.get();
    }

    @Override
    Optional<? extends LUTypeSolver> applyConcretely(BandMatrix matrix, double epsilon) {
        return ModifiedCholeskyBandSystem.instanceOf(matrix, epsilon);
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
    public static ModifiedCholeskyBandExecutor instance() {
        return INSTANCE;
    }

    private static final class ModifiedCholeskyBandSystem
            extends InvertibleDeterminantableSystem<Matrix> implements LUTypeSolver {

        private static final String CLASS_STRING = "ModifiedCholesky";

        private static final double EPSILON_A = 1E-100;

        private final BandMatrix matrix;

        private final DiagonalMatrix mxD;
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
        static Optional<ModifiedCholeskyBandSystem> instanceOf(final BandMatrix matrix, final double epsilon) {
            try {
                return Optional.of(new ModifiedCholeskyBandSystem(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }

        /**
         * staticファクトリから呼ばれる.
         *
         * @throws ProcessFailedException 行列が特異あるいはピボッティングが必要な場合
         */
        private ModifiedCholeskyBandSystem(final BandMatrix matrix, final double epsilon)
                throws ProcessFailedException {

            //ここで例外が発生する可能性がある
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
        protected InverstibleAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues det = new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());

            // A^{-1} = (LD(L^T))^{-1} = L^{-T}D^{-1}L^{-1} = (L^{-T})D^{-1}(L^{-T})^T
            Matrix invMatrix = Matrix.symmetricMultiply(
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get().transpose());

            return new InverstibleAndDeterminantStruct<Matrix>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }
    }
}
