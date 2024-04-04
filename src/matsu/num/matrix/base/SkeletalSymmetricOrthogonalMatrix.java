/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base;

import java.util.Optional;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * {@linkplain Symmetric} が付与された
 * {@linkplain OrthogonalMatrix} の骨格実装. <br>
 * {@linkplain OrthogonalMatrix#inverse()} の実装の提供が主な効果である.
 * </p>
 * 
 * <p>
 * 対称な直交行列は逆行列と転置行列が自分自身であるので,
 * それらを実現するように実装を提供し,
 * 同時にオーバーライドを禁止する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 * @param <T> {@code this} のタイプ,
 *            {@linkplain #transpose()},
 *            {@linkplain #inverse()} の戻り値型
 */
public abstract class SkeletalSymmetricOrthogonalMatrix<T extends OrthogonalMatrix>
        implements OrthogonalMatrix, Symmetric {

    private final T castedThis;
    private final Optional<? extends T> thisOptional;

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalSymmetricOrthogonalMatrix() {
        super();
        /*
         * 警告抑制をしているが, ジェネリックキャストなので実行時は全て
         * OrthogonalMatrix に置き換えられ,
         * ClassCastException は発生しない.
         */
        @SuppressWarnings("unchecked")
        T t = (T) this;
        this.castedThis = t;

        this.thisOptional = Optional.of(this.castedThis);
    }

    @Override
    public final Optional<? extends T> inverse() {
        return this.thisOptional;
    }

    @Override
    public final T transpose() {
        return this.castedThis;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final Vector operateTranspose(Vector operand) {
        return this.operate(operand);
    }

}
