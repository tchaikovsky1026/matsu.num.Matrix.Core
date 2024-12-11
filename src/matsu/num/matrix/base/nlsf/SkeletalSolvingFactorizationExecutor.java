/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.base.nlsf;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
import matsu.num.matrix.base.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * {@link SolvingFactorizationExecutor} の骨格実装.
 * 
 * <p>
 * このクラスでは,
 * {@link #accepts(Matrix)}, {@link #apply(Matrix)},
 * {@link #apply(Matrix, double)}
 * メソッドの適切な実装を提供する.
 * </p>
 * 
 * <p>
 * {@link #accepts(Matrix)} の実装では,
 * 正方行列でない場合は無条件にrejectされる. <br>
 * さらに, 正方行列に対して抽象メソッド {@link #acceptsConcretely(Matrix)}
 * により検証される.
 * </p>
 * 
 * <p>
 * {@link #apply(Matrix)}, {@link #apply(Matrix, double)} の実装では,
 * ({@code epsilon} の検証と) {@link #accepts(Matrix)} による行列の正当性の検証が行われ, <br>
 * 正当な行列に対して {@link #applyConcretely(Matrix, double)} によって行列分解が実行される.
 * </p>
 * 
 * 
 * <hr>
 * 
 * <h2>使用上の注意</h2>
 * 
 * <p>
 * このクラスはインターフェースの骨格実装を提供するためのものであり,
 * 型として扱うべきではない. <br>
 * 具体的に, 次のような取り扱いは強く非推奨である.
 * </p>
 * 
 * <ul>
 * <li>このクラスを変数宣言の型として使う.</li>
 * <li>{@code instanceof} 演算子により, このクラスのサブタイプかを判定する.</li>
 * <li>インスタンスをこのクラスにキャストして使用する.</li>
 * </ul>
 * 
 * <p>
 * このクラスは, 型としての互換性は積極的には維持されず,
 * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.0
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 */
abstract non-sealed class SkeletalSolvingFactorizationExecutor<
        MT extends Matrix, ST extends LUTypeSolver>
        implements SolvingFactorizationExecutor<MT> {

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalSolvingFactorizationExecutor() {
        super();
    }

    /**
     * 正方行列に対して, 受け入れ可能かの詳細を判定する.
     * 
     * <p>
     * このメソッドは, {@link #accepts(Matrix)} の具体的処理を実装するための抽象メソッドである. <br>
     * {@link #accepts(Matrix)} がコールされたときに引数が正方行列であるかどうかが検証され,
     * 正方行列であることが確定した行列がこの抽象メソッドに渡される. <br>
     * このメソッドの実装では, 正方行列に対して追加の条件を判定する.
     * </p>
     * 
     * <p>
     * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
     * </p>
     * 
     * @param matrix 正方行列であることが確定した行列(当然nullでない)
     * @return 判定結果
     */
    abstract MatrixStructureAcceptance acceptsConcretely(MT matrix);

    /**
     * 受け入れ可能であることが確定した行列に対して, 行列分解を実際に実行する.
     * 
     * <p>
     * このメソッドは, {@link #apply(Matrix, double)} の具体的処理を実装するための抽象メソッドである. <br>
     * {@link #apply(Matrix, double)} がコールされたときに {@code matrix} が
     * {@link #accepts(Matrix)} により検証, {@code epsilon} も検証され,
     * 不適切の場合は例外をスローする. <br>
     * 適切であることが確定した {@code matrix} 行列と
     * {@code epsilon} がこの抽象メソッドに渡される. <br>
     * このメソッドの実装では, 行列分解を実行し, 行列分解かもしくは空オプショナルを返す. <br>
     * このメソッドに渡される引数は正当であるため, 例外をスローしてはならない.
     * </p>
     * 
     * <p>
     * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
     * </p>
     * 
     * @param matrix 受け入れ可能であることが確定した行列
     * @param epsilon 正常(0以上の有限数)であることが確定した相対epsilon
     * @return 行列分解
     */
    abstract Optional<ST> applyConcretely(MT matrix, double epsilon);

    /**
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public final MatrixStructureAcceptance accepts(MT matrix) {
        if (!matrix.matrixDimension().isSquare()) {
            return MatrixRejectionConstant.REJECTED_BY_NOT_SQUARE.get();
        }
        return this.acceptsConcretely(matrix);
    }

    /**
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public final Optional<ST> apply(MT matrix, double epsilon) {
        if (!Double.isFinite(epsilon) || epsilon < 0) {
            throw new IllegalArgumentException(String.format("不正な値:epsilon=%s", epsilon));
        }

        MatrixStructureAcceptance acceptance = this.accepts(matrix);
        if (acceptance.isReject()) {
            throw acceptance.getException(matrix);
        }

        return this.applyConcretely(Objects.requireNonNull(matrix), epsilon);
    }

    @Override
    public final Optional<ST> apply(MT matrix) {
        return this.apply(matrix, PseudoRegularMatrixProcess.DEFAULT_EPSILON);
    }

    /**
     * このインスタンスの文字列表現を返す.
     * 
     * @return 文字列表現
     */
    @Override
    public String toString() {
        Deque<Class<?>> enclosingClassLevels = new LinkedList<>();

        Class<?> currentLevel = this.getClass();
        while (Objects.nonNull(currentLevel)) {
            enclosingClassLevels.add(currentLevel);
            currentLevel = currentLevel.getEnclosingClass();
        }

        StringBuilder sb = new StringBuilder();
        for (Iterator<Class<?>> ite = enclosingClassLevels.descendingIterator();
                ite.hasNext();) {
            Class<?> clazz = ite.next();
            sb.append(clazz.getSimpleName());
            if (ite.hasNext()) {
                sb.append('.');
            }
        }

        return sb.toString();
    }

    /**
     * -
     * 
     * @return -
     * @throws CloneNotSupportedException 常に
     * @deprecated Clone不可
     */
    @Deprecated
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * オーバーライド不可.
     */
    @Override
    @Deprecated
    protected final void finalize() throws Throwable {
        super.finalize();
    }
}
