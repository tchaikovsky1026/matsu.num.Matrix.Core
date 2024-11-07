/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.7
 */
package matsu.num.matrix.base.nlsf;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
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
 * @author Matsuura Y.
 * @version 22.2
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 */
abstract non-sealed class SkeletalSolvingFactorizationExecutor<
        MT extends Matrix, ST extends LUTypeSolver>
        implements SolvingFactorizationExecutor<MT, ST> {

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalSolvingFactorizationExecutor() {
        super();
    }

    /**
     * 正方行列に対して, 受け入れ可能かの詳細を判定する. <br>
     * 公開してはいけない.
     * 
     * @param matrix 正方行列であることが確定した行列(当然nullでない)
     * @return 判定結果
     */
    abstract MatrixStructureAcceptance acceptsConcretely(MT matrix);

    /**
     * 受け入れ可能であることが確定した行列に対して, 行列分解を実際に実行する. <br>
     * 例外をスローしてはいけない. <br>
     * 公開してはいけない.
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
            return MatrixRejectionInLSF.REJECTED_BY_NOT_SQUARE.get();
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
        Optional<IllegalArgumentException> throwException = this.accepts(matrix).getException(matrix);
        if (throwException.isPresent()) {
            throw throwException.get();
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
}
