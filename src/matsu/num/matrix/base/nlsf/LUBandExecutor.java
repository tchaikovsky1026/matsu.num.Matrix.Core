/**
 * 2023.12.25
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.exception.ProcessFailedException;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverseAndDeterminantStruct;
import matsu.num.matrix.base.helper.value.InvertibleDeterminantableSystem;
import matsu.num.matrix.base.nlsf.helper.fact.LUBandFactorizationHelper;

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
 * このクラスが提供する {@linkplain SolvingFactorizationExecutor} について,
 * メソッド {@code apply(matrix, epsilon)} で追加でスローされる例外は次のとおりである,
 * </p>
 * <ul>
 * <li>{@code IllegalArgumentException 行列の有効要素数が大きすぎる場合(後述)}</li>
 * <li>{@code ProcessFailedException ピボッティングが必要な場合}</li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i>, 上側帯幅と下側帯幅の大きい方を <i>b</i> として, <br>
 * <i>n</i> * <i>b</i> &gt; {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 18.0
 */
public final class LUBandExecutor {

    private static final SolvingFactorizationExecutor<
            BandMatrix, LUTypeSolver> INSTANCE = new ExecutorImpl();

    private LUBandExecutor() {
        throw new AssertionError();
    }

    /**
     * このクラスの機能を実行するインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static SolvingFactorizationExecutor<
            BandMatrix, LUTypeSolver> instance() {
        return INSTANCE;
    }

    private static final class ExecutorImpl
            extends SkeletalSolvingFactorizationExecutor<
                    BandMatrix, LUTypeSolver>
            implements SolvingFactorizationExecutor<
                    BandMatrix, LUTypeSolver> {

        private static final String CLASS_EXPLANATION = "LUBandExecutor";

        @Override
        final LUTypeSolver applyConcretely(BandMatrix matrix, double epsilon) {
            return new LUBandFactorization(matrix, epsilon);
        }

        @Override
        public String toString() {
            return CLASS_EXPLANATION;
        }
    }

    private static final class LUBandFactorization
            extends InvertibleDeterminantableSystem<Matrix> implements LUTypeSolver {

        private static final String CLASS_STRING = "LU";

        private static final double EPSILON_A = 1E-100;

        private final BandMatrix matrix;

        private final DiagonalMatrix mxD;
        private final LowerUnitriangularEntryReadableMatrix mxL;
        private final LowerUnitriangularEntryReadableMatrix mxUt;

        /**
         * ビルダから呼ばれる.
         * 
         * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax
         *             or dim * ub > IntMax)
         * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合,
         *             成分に極端な値を含み分解が完了できない場合
         */
        private LUBandFactorization(BandMatrix matrix, double epsilon) {
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
        protected InverseAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues det = new DeterminantValues(this.mxD.logAbsDeterminant(), this.mxD.signOfDeterminant());
            // A^{-1} = (LDU)^{-1} = U^{-1}D^{-1}L^{-1}
            Matrix invMatrix = Matrix.multiply(
                    this.mxUt.inverse().get().transpose(),
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get());
            return new InverseAndDeterminantStruct<>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }
    }

}
