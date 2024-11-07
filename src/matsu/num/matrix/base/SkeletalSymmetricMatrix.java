/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.4
 */
package matsu.num.matrix.base;

/**
 * <p>
 * {@link Symmetric} が付与された {@link Matrix} の骨格実装.
 * </p>
 * 
 * <p>
 * このクラスは, {@link #transpose()} の適切な実装を提供する. <br>
 * {@link #transpose()} の戻り値は {@code this} である. <br>
 * ただし, 戻り値型を具象クラスにゆだねるため, ジェネリクスと {@link #self()} メソッドの実装を要求する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 * @param <T> thisの具象型, 再帰的ジェネリクスによりtransposeの戻り値型を具象クラスにゆだねる.
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
     * {@code this} を返す.
     * 
     * <p>
     * このメソッドは公開してはいけない.
     * </p>
     * 
     * @return this
     */
    protected abstract T self();

    @Override
    public final T transpose() {
        return this.self();
    }

    /**
     * <p>
     * このインスタンスの文字列説明表現を返す.
     * </p>
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:(%dimension)]} <br>
     * ただし, サブクラスがより良い表現を提供するかもしれない.
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s]", this.matrixDimension());
    }
}
