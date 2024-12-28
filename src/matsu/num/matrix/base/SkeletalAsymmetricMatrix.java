/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.26
 */
package matsu.num.matrix.base;

import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * 非対称な {@link Matrix} の骨格実装.
 * 
 * <p>
 * <i> <u>
 * 具象クラスが {@link Symmetric} を付与されるなら,
 * {@link SkeletalSymmetricMatrix} を使用しなければならない. <br>
 * (このクラスからの継承は禁止である.)
 * </u> </i>
 * </p>
 * 
 * <p>
 * このクラスは, {@link Matrix#transpose()} の適切な実装を提供する. <br>
 * 初めて {@code transpose()} が呼ばれたときに転置行列を
 * {@link #createTranspose()} によって生成, キャッシュし,
 * 以降はそのキャッシュを戻す.
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
 * このクラスは型としての互換性は積極的には維持されず,
 * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.2
 * @param <TT> 転置行列のタイプ, {@code transpose()} の戻り値型を具象クラスで限定する.
 */
public abstract class SkeletalAsymmetricMatrix<TT extends Matrix> implements Matrix {

    //転置行列を生成するサプライヤ
    private final Supplier<TT> transposeSupplier;

    /**
     * 骨格実装を生成する.
     * 
     * <p>
     * このクラスの規約を検証するため,
     * このコンストラクタで {@link Symmetric} のサブタイプでないかどうかを検証している. <br>
     * 自身が {@link Symmetric} のサブタイプである場合, エラーをスローする.
     * </p>
     * 
     * @throws AssertionError 自身が {@link Symmetric} のサブタイプである場合
     */
    protected SkeletalAsymmetricMatrix() {
        super();
        this.transposeSupplier = ImmutableLazyCacheSupplier.of(() -> this.createTranspose());
        if (this instanceof Symmetric) {
            throw new AssertionError(
                    String.format(
                            "実装規約違反: %s のサブクラスに %s を付与してはいけない",
                            SkeletalAsymmetricMatrix.class.getSimpleName(),
                            Symmetric.class.getSimpleName()));
        }
    }

    @Override
    public final TT transpose() {
        return this.transposeSupplier.get();
    }

    /**
     * <p>
     * 自身の転置行列を生成する.
     * </p>
     * 
     * <p>
     * {@link #transpose()} を遅延初期化するために実装されるメソッドである. <br>
     * それが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけこのメソッドが呼ばれる. <br>
     * 公開は禁止され, サブクラスからもコールしてはならない.
     * </p>
     * 
     * @implSpec {@link Matrix} の実装規約より,
     *               <blockquote>
     *               {@code this.createTranspose().transpose() == this}
     *               </blockquote>
     *               を満たすことが推奨される. <br>
     *               アクセス修飾子を {@code public} にしてはいけない.
     * 
     * @return 自身の転置行列
     */
    protected abstract TT createTranspose();

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
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
     * {@code Matrix[dim:%dimension]}
     * </p>
     * 
     * @implSpec
     *               継承先においてオーバーライドを許可する. <br>
     *               ただし, {@code Matrix["param":%param]} や
     *               {@code Matrix["param"=%param]} の形が適切であると思われる.
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s]",
                this.matrixDimension());
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
