/**
 * 2023.12.25
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PermutationMatrix;
import matsu.num.matrix.base.exception.ProcessFailedException;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverseAndDeterminantStruct;
import matsu.num.matrix.base.helper.value.InvertibleDeterminantableSystem;
import matsu.num.matrix.base.nlsf.helper.fact.LUPivotingFactorizationHelper;

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
 * このクラスが提供する {@linkplain SolvingFactorizationExecutor} について,
 * メソッド {@code apply(matrix, epsilon)} で追加でスローされる例外は次のとおりである,
 * </p>
 * 
 * <ul>
 * <li>{@code IllegalArgumentException 行列の有効要素数が大きすぎる場合(後述)}</li>
 * </ul>
 * 
 * <p>
 * 有効要素数が大きすぎるとは, <br>
 * 行列の行数(= 列数)を <i>n</i> として, <br>
 * <i>n</i> * <i>n</i> &gt; {@linkplain Integer#MAX_VALUE} <br>
 * である状態である.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 18.0
 */
public final class LUPivotingExecutor {

    private static final SolvingFactorizationExecutor<
            EntryReadableMatrix, LUTypeSolver> INSTANCE = new ExecutorImpl();

    private LUPivotingExecutor() {
        throw new AssertionError();
    }

    /**
     * このクラスの機能を実行するインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static SolvingFactorizationExecutor<
            EntryReadableMatrix, LUTypeSolver> instance() {
        return INSTANCE;
    }

    private static final class ExecutorImpl
            extends SkeletalSolvingFactorizationExecutor<
                    EntryReadableMatrix, LUTypeSolver>
            implements SolvingFactorizationExecutor<
                    EntryReadableMatrix, LUTypeSolver> {

        private static final String CLASS_EXPLANATION = "LUPivotingExecutor";

        @Override
        final LUTypeSolver applyConcretely(EntryReadableMatrix matrix,
                double epsilon) {
            return new LUPivotingFactorization(matrix, epsilon);
        }

        @Override
        public String toString() {
            return CLASS_EXPLANATION;
        }
    }

    private static final class LUPivotingFactorization
            extends InvertibleDeterminantableSystem<Matrix> implements LUTypeSolver {

        private static final String CLASS_STRING = "LU-Pivoting";

        private static final double EPSILON_A = 1E-100;

        private final EntryReadableMatrix matrix;

        private final DiagonalMatrix mxD;
        private final LowerUnitriangularEntryReadableMatrix mxL;
        private final LowerUnitriangularEntryReadableMatrix mxUt;
        private final PermutationMatrix mxP;

        /**
         * ビルダから呼ばれる.
         *
         * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * dim>IntMax)
         * @throws ProcessFailedException 行列が特異に近い場合, 成分に極端な値を含み分解が完了できない場合
         */
        private LUPivotingFactorization(final EntryReadableMatrix matrix, final double epsilon) {

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
        protected InverseAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
            DeterminantValues det = new DeterminantValues(
                    this.mxD.logAbsDeterminant(), this.mxP.signOfDeterminant() * this.mxD.signOfDeterminant());

            // A^{-1} = (PLDU)^{-1} = U^{-1}D^{-1}L^{-1}P^{-1}
            Matrix invMatrix = Matrix.multiply(
                    this.mxUt.inverse().get().transpose(),
                    this.mxD.inverse().get(),
                    this.mxL.inverse().get(),
                    this.mxP.inverse().get());

            return new InverseAndDeterminantStruct<Matrix>(det, invMatrix);
        }

        @Override
        public String toString() {
            return LUTypeSolver.toString(this, CLASS_STRING);
        }

    }
}
