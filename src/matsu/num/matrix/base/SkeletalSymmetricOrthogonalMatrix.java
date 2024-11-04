/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.3
 */
package matsu.num.matrix.base;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * {@link Symmetric} が付与された
 * {@link OrthogonalMatrix} の骨格実装.
 * </p>
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
 * @version 22.0
 * @param <T> {@code this} のタイプ,
 *            {@link #transpose()},
 *            {@link #inverse()} の戻り値型
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
        this.opSelfSupplier = ImmutableLazyCacheSupplier.of(() -> Optional.of(this.self()));
    }

    @Override
    public final Optional<T> inverse() {
        return this.opSelfSupplier.get();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final Vector operateTranspose(Vector operand) {
        return this.operate(operand);
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, orthogonal]",
                this.matrixDimension());
    }
}
