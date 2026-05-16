/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.16
 */
package matsu.num.matrix.core.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.core.Invertible;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.lazy.ImmutableLazyCacheSupplier;

/**
 * 直交行列の,
 * transpose, inverse に関わる骨格実装.
 * 
 * <p>
 * このクラスは, {@link Matrix#transpose()},
 * {@link Invertible#inverse()}
 * の適切な実装を提供する. <br>
 * 初めてそれらが呼ばれたときに転置行列を
 * {@link #createTranspose()} によって生成,
 * その {@link Optional} をキャッシュし, 以降はそのキャッシュを戻す. <br>
 * 自身が {@link Symmetric} を付与されるなら, {@code this} を返すようにする.
 * </p>
 * 
 * <p>
 * このクラスはを型として扱ってはいけない.
 * </p>
 * 
 * @implSpec
 *               型をバインドしたクラスは {@code final} とするのが望ましい.
 * 
 * @author Matsuura Y.
 * @param <TT>
 *            生成する転置行列の型を表す. <br>
 *            {@link #transpose()} の戻り値型と,
 *            {@link #inverse()} の戻り値 {@code Optional} の要素型にバインドされる型を決める.
 */
public abstract class SkeletalOrthogonalMatrix<TT extends OrthogonalMatrix>
        implements OrthogonalMatrix {

    /**
     * (外部からの呼び出し不可)
     */
    protected final Supplier<Optional<TT>> transposeSupplier;

    /**
     * 唯一のコンストラクタ.
     */
    protected SkeletalOrthogonalMatrix() {
        super();
        this.transposeSupplier = ImmutableLazyCacheSupplier.of(
                () -> Optional.of(this.createTranspose()));
    }

    @Override
    @SuppressWarnings("removal")
    public final TT transpose() {
        return transposeSupplier.get().get();
    }

    @Override
    @SuppressWarnings("removal")
    public final Optional<TT> inverse() {
        return transposeSupplier.get();
    }

    @Override
    public final OrthogonalMatrix transposeOrth() {
        return transposeSupplier.get().get();
    }

    /**
     * <p>
     * 自身の転置行列を生成する.
     * </p>
     * 
     * <p>
     * {@link #transpose()} を遅延初期化するために実装されるメソッドである. <br>
     * それが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけこのメソッドが呼ばれる. <br>
     * 公開は禁止されており, サブクラスからもコールしてはならない.
     * </p>
     *
     * @implSpec
     *               自身が {@link Symmetric} を付与されるなら, {@code this} を返すこと. <br>
     *               {@link Matrix} の実装規約より,
     * 
     *               <blockquote>
     * 
     *               <pre>
     * this.createTranspose().transpose() == this</pre>
     * 
     *               </blockquote>
     * 
     *               を満たすことが推奨される. <br>
     *               アクセス修飾子を {@code public} にしてはいけない.
     * 
     * @return 自身の転置行列
     */
    protected abstract TT createTranspose();

    /**
     * このインスタンスの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:%dimension, orthogonal]}
     * </p>
     * 
     * @implSpec
     *               継承先においてオーバーライドを許可する. <br>
     *               {@code Matrix["param":%param]} や
     *               {@code Matrix["param"=%param]} の形が適切であると思われる.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, orthogonal]",
                this.matrixDimension());
    }
}
