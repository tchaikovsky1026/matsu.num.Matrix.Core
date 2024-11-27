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

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link Symmetric} が付与された
 * {@link OrthogonalMatrix} の骨格実装.
 * 
 * <p>
 * このクラスは, {@link #transpose()}, {@link #inverse()} の適切な実装を提供する. <br>
 * {@link #transpose()} の戻り値は {@code this} である. <br>
 * {@link #inverse()} が最初に呼ばれたときに
 * {@code this} の {@link Optional} が生成, キャッシュされ,
 * 以降はそのキャッシュを戻す. <br>
 * ただし, 戻り値型を具象クラスにゆだねるため, ジェネリクスと {@link #self()} メソッドの実装を要求する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.1
 * @param <T> {@code this} の具象型,
 *            再帰的ジェネリクスにより
 *            {@code transpose()}, {@code inverse()}
 *            の戻り値型を具象クラスに伝播させる.
 */
public abstract class SkeletalSymmetricOrthogonalMatrix<
        T extends SkeletalSymmetricOrthogonalMatrix<T>>
        extends SkeletalSymmetricMatrix<T>
        implements OrthogonalMatrix, Symmetric {

    /**
     * thisのオプショナルを生成するサプライヤ.
     */
    private final Supplier<Optional<T>> opSelfSupplier;

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalSymmetricOrthogonalMatrix() {
        super();
        this.opSelfSupplier = ImmutableLazyCacheSupplier.of(() -> Optional.of(this.transpose()));
    }

    @Override
    public final Optional<T> inverse() {
        return this.opSelfSupplier.get();
    }

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
