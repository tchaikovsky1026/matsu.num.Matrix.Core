/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.27
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * {@link Symmetric} が付与された {@link Matrix} の骨格実装.
 * 
 * <p>
 * このクラスは, {@link #transpose()} の適切な実装を提供する. <br>
 * {@link #transpose()} の戻り値は {@code this} である. <br>
 * ただし, 戻り値型を具象クラスにゆだねるため, ジェネリクスと {@link #self()} メソッドの実装を要求する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.1
 * @param <T> {@code this} の具象型,
 *            再帰的ジェネリクスにより {@code transpose()} の戻り値型を具象クラスに伝播させる.
 */
public abstract class SkeletalSymmetricMatrix<T extends SkeletalSymmetricMatrix<T>>
        implements Matrix, Symmetric {

    /**
     * 骨格実装を生成する.
     */
    protected SkeletalSymmetricMatrix() {
        super();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final Vector operateTranspose(Vector operand) {
        return this.operate(operand);
    }

    /**
     * {@code this} を返す.
     * 
     * <p>
     * このメソッドを公開するのは多くの場合不適切である.
     * </p>
     * 
     * @return this
     */
    protected abstract T self();

    @Override
    public final T transpose() {
        return this.self();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * このインスタンスの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:(%dimension)]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s]", this.matrixDimension());
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
}
