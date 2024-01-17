/**
 * 2023.12.22
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.exception.ProcessFailedException;

/**
 * <p>
 * {@linkplain SolvingFactorizationExecutor}の骨格実装.
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
 * 正方行列であるMatrixと正当なepsilonを使って行列分解を実行し, その結果を返すように実装する. <br>
 * 行列分解が実行できない/できなかった場合は, インターフェースの要件通り,
 * {@linkplain IllegalArgumentException} のサブクラスの例外を投げてよい. <br>
 * ただし, 例外を投げる条件はクラスドキュメントに記載すべきである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 18.0
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 * @see SolvingFactorizationExecutor
 */
abstract class SkeletalSolvingFactorizationExecutor<
        MT extends Matrix, ST extends LUTypeSolver>
        implements SolvingFactorizationExecutor<MT, ST> {

    SkeletalSolvingFactorizationExecutor() {
        super();
    }

    /**
     * 行列分解を実際に実行する. <br>
     * スローされる例外は{@linkplain #apply(Matrix, double)}に従う.
     * 
     * <p>
     * 有効要素数が大きすぎる場合に例外を投げることになるであろう. <br>
     * その要件はサブクラスの説明に記載する.
     * </p>
     * 
     * @param matrix 正方行列であることが確定した行列
     * @param epsilon 正常(0以上の有限数)であることが確定した相対epsilon
     * @return 行列分解
     * @throws IllegalArgumentException 有効要素数が大きすぎる場合
     */
    abstract ST applyConcretely(MT matrix, double epsilon);

    /**
     * 
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws ProcessFailedException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public final ST apply(MT matrix, double epsilon) {
        if (!Double.isFinite(epsilon) || epsilon < 0) {
            throw new IllegalArgumentException(String.format("不正な値:epsilon=%.16G", epsilon));
        }
        if (!matrix.matrixDimension().isSquare()) {
            throw new MatrixFormatMismatchException(
                    String.format("正方形ではない行列サイズ:%s", matrix.matrixDimension()));
        }
        return this.applyConcretely(Objects.requireNonNull(matrix), epsilon);
    }

    @Override
    public final ST apply(MT matrix) {
        return this.apply(matrix, PseudoRegularMatrixProcess.DEFAULT_EPSILON);
    }
}
