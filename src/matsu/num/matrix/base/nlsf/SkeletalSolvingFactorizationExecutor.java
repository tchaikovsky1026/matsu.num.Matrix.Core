/**
 * 2024.2.2
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * {@linkplain SolvingFactorizationExecutor} の骨格実装.
 * </p>
 * 
 * <p>
 * インターフェースのメソッドの説明の通り,
 * {@linkplain #apply(Matrix)} と {@linkplain #apply(Matrix, double)} の違いは
 * epsilonにデフォルト値を使うかどうかである. <br>
 * このクラスではそれらのメソッドの中で, 引数 {@code matrix}, {@code epsilon} の事前条件チェックを行い,
 * その後に抽象メソッド {@linkplain #applyConcretely(Matrix, double)} を呼ぶことで具体的な分解の実行する.
 * </p>
 * 
 * <p>
 * {@linkplain #applyConcretely(Matrix, double)} のオーバーライドでは,
 * 受け入れ可能なMatrixと正当なepsilonを使って行列分解を実行し, その結果を返すように実装する. <br>
 * 行列分解が実行不可能な場合は, インターフェースの要件通り空のオプショナルを返す.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 19.5
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 */
abstract class SkeletalSolvingFactorizationExecutor<
        MT extends Matrix, ST extends LUTypeSolver>
        implements SolvingFactorizationExecutor<MT, ST> {

    SkeletalSolvingFactorizationExecutor() {
        super();
    }

    /**
     * 正方行列に対して, 受け入れ可能かの詳細を判定する.
     * 
     * @param matrix 正方行列であることが確定した行列(当然nullでない)
     * @return 判定結果
     */
    abstract MatrixStructureAcceptance acceptsConcretely(MT matrix);

    /**
     * 行列分解を実際に実行する. <br>
     * 受け入れ可能であることが確定しているため, 例外をスローしてはいけない.
     * 
     * @param matrix 受け入れ可能であることが確定した行列
     * @param epsilon 正常(0以上の有限数)であることが確定した相対epsilon
     * @return 行列分解
     */
    abstract Optional<? extends ST> applyConcretely(MT matrix, double epsilon);

    /**
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public final MatrixStructureAcceptance accepts(MT matrix) {
        if (!matrix.matrixDimension().isSquare()) {
            return MatrixRejectionInLSF.REJECTED_BY_NOT_SQUARE.get();
        }
        return this.acceptsConcretely(matrix);
    }

    /**
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public final Optional<? extends ST> apply(MT matrix, double epsilon) {
        if (!Double.isFinite(epsilon) || epsilon < 0) {
            throw new IllegalArgumentException(String.format("不正な値:epsilon=%s", epsilon));
        }
        Optional<IllegalArgumentException> throwException = this.accepts(matrix).getException(matrix);
        if (throwException.isPresent()) {
            throw throwException.get();
        }

        return this.applyConcretely(Objects.requireNonNull(matrix), epsilon);
    }

    @Override
    public final Optional<? extends ST> apply(MT matrix) {
        return this.apply(matrix, PseudoRegularMatrixProcess.DEFAULT_EPSILON);
    }
    
    /**
     * このインスタンスの文字列表現を返す.
     * 
     * @return 文字列表現
     */
    @Override
    public abstract String toString();
}
