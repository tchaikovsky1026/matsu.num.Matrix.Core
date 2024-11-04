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

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * <p>
 * 非対称な {@link OrthogonalMatrix} の骨格実装. <br>
 * {@link OrthogonalMatrix#transpose()}, {@link OrthogonalMatrix#inverse()}
 * の実装の提供が主な効果である.
 * </p>
 * 
 * <p>
 * <i> <u>
 * 具象クラスが {@link Symmetric} を付与されるなら,
 * {@link SkeletalSymmetricOrthogonalMatrix} を使用しなければならない.
 * </u> </i>
 * </p>
 * 
 * <p>
 * このクラスは, {@link #transpose()}, {@link #inverse()}
 * の適切な実装を提供する. <br>
 * 初めてそれらが呼ばれたときに転置行列を
 * {@link #createTranspose()} によって生成,
 * その {@link Optional} をキャッシュし, 以降はそのキャッシュを戻す.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 * @param <TT> 転置行列のタイプ, transposeの戻り値型を具象クラスにゆだねる.
 */
public abstract class SkeletalAsymmetricOrthogonalMatrix<TT extends OrthogonalMatrix>
        implements OrthogonalMatrix {

    //逆行列(転置行列)を生成するサプライヤ
    private final Supplier<Optional<TT>> inverseSupplier;

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalAsymmetricOrthogonalMatrix() {
        super();
        this.inverseSupplier = ImmutableLazyCacheSupplier.of(
                () -> Optional.of(this.createTranspose()));
    }

    @Override
    public final Optional<TT> inverse() {
        return this.inverseSupplier.get();
    }

    @Override
    public final TT transpose() {
        return this.inverseSupplier.get().get();
    }

    /**
     * 自身の転置行列を計算する.
     * 
     * <p>
     * {@link #transpose()}, {@link #inverse()} を遅延初期化するために実装されるメソッドである. <br>
     * それらのどちらかが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけこのメソッドが呼ばれる. <br>
     * 公開してはいけない.
     * </p>
     * 
     * <p>
     * 実装は, <br>
     * {@code this.createTranspose().transpose() == this} <br>
     * {@code this.createTranspose().inverse().get() == this}
     * <br>
     * を満たすことが推奨される
     * ({@link OrthogonalMatrix} の実装規約より).
     * </p>
     * 
     * @return 自身の転置行列
     */
    protected abstract TT createTranspose();

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:(%dimension), orthogonal]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, orthogonal]",
                this.matrixDimension());
    }
}
