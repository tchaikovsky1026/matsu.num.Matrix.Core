/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.2
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
 * このクラスは, {@link #transpose()} の適切な実装を提供する. <br>
 * 初めて {@link #transpose()} が呼ばれたときに転置行列を
 * {@link #createTranspose()} によって生成, キャッシュし,
 * 以降はそのキャッシュを戻す.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.3
 * @param <TT> 転置行列のタイプ, {@code transpose()} の戻り値型を具象クラスで決める.
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
     * <p>
     * 実装は, <br>
     * {@code this.createTranspose().transpose() == this}
     * <br>
     * を満たすことが推奨される
     * ({@link Matrix} の実装規約より).
     * </p>
     * 
     * @return 自身の転置行列
     */
    protected abstract TT createTranspose();

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * <p>
     * このインスタンスの文字列説明表現を返す.
     * </p>
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
     * {@code finalize()} のオーバーライドを禁止する.
     */
    @Override
    @Deprecated
    protected final void finalize() throws Throwable {
        super.finalize();
    }
}
