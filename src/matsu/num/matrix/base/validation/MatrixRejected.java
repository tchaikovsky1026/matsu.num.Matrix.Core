/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import matsu.num.matrix.base.Matrix;

/**
 * 拒絶を表す {@linkplain MatrixStructureAcceptance} を扱う.
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class MatrixRejected extends MatrixStructureAcceptance {

    private final Function<Matrix, IllegalArgumentException> exceptionGetter;
    private final String explanation;

    /**
     * 
     * @param exceptionGetter
     * @param explanation 説明
     * @throws IllegalArgumentException 説明がブランクの場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private MatrixRejected(Function<Matrix, IllegalArgumentException> exceptionGetter, String explanation) {
        this.exceptionGetter = Objects.requireNonNull(exceptionGetter);
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("説明がブランク");
        }
        this.explanation = explanation;
    }

    /**
     * rejectを返す.
     */
    @Override
    public Type type() {
        return Type.REJECTED;
    }

    /**
     * このインスタンスの拒絶理由に適した例外インスタンスを取得する.
     * 空でない.
     * 
     * @return スローすべき例外, 空でない
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Optional<IllegalArgumentException> getException(Matrix matrix) {
        return Optional.of(this.exceptionGetter.apply(Objects.requireNonNull(matrix)));
    }

    /**
     * このインスタンスの文字列表現
     * 
     * @return 文字列表現
     */
    @Override
    public String toString() {
        return this.explanation;
    }

    /**
     * 拒絶根拠となる例外で特徴づけられた, 拒絶インスタンスを構築する.
     * 
     * @param exceptionGetter 例外インスタンスの生成器
     * @param explanation 説明
     * @return 拒絶インスタンス
     * @throws IllegalArgumentException 説明がブランクの場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static MatrixRejected by(Function<Matrix, IllegalArgumentException> exceptionGetter, String explanation) {
        return new MatrixRejected(exceptionGetter, explanation);
    }

    /**
     * 拒絶インスタンスを構築する.
     * 
     * @return 拒絶インスタンス
     */
    public static MatrixRejected instance() {
        return new MatrixRejected(
                m -> new IllegalArgumentException(
                        String.format("拒絶:%s", m)),
                "REJECTED");
    }

}
