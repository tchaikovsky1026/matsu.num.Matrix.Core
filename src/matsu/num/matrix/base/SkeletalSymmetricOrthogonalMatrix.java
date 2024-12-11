/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.base;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * {@link Symmetric} が付与された
 * {@link OrthogonalMatrix} の骨格実装.
 * 
 * <p>
 * このクラスは, {@link OrthogonalMatrix#transpose()},
 * {@link OrthogonalMatrix#inverse()} の適切な実装を提供する. <br>
 * {@link OrthogonalMatrix#transpose()} の戻り値は {@code this} である. <br>
 * {@link OrthogonalMatrix#inverse()} が最初に呼ばれたときに
 * {@code this} の {@link Optional} が生成, キャッシュされ,
 * 以降はそのキャッシュを戻す. <br>
 * ただし, 戻り値型をサブタイプに限定できるようにするため, ジェネリクスと {@code self()} メソッドの実装を要求する.
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
 * @version 25.0
 * @param <T> {@code this} の具象型,
 *            再帰的ジェネリクスにより
 *            {@code transpose()}, {@code inverse()}
 *            の戻り値型をサブタイプで扱う.
 */
public abstract class SkeletalSymmetricOrthogonalMatrix<
        T extends SkeletalSymmetricOrthogonalMatrix<T>>
        implements OrthogonalMatrix, Symmetric {

    /**
     * thisのオプショナルを生成するサプライヤ. <br>
     * {@code Optional<T>} を生成するためにオーバーライド可能なメソッド
     * {@code self()} が直接的あるいは間接的に必ず呼ばれるため,
     * 遅延初期化を行う.
     */
    private final Supplier<Optional<T>> opSelfSupplier;

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalSymmetricOrthogonalMatrix() {
        super();
        this.opSelfSupplier = ImmutableLazyCacheSupplier.of(
                () -> Optional.of(this.self()));
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
     * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
     * </p>
     * 
     * @implSpec アクセス修飾子を {@code public} にしてはいけない.
     * 
     * @return this
     */
    protected abstract T self();

    @Override
    public final T transpose() {
        return this.self();
    }

    @Override
    public final Optional<T> inverse() {
        return this.opSelfSupplier.get();
    }

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
