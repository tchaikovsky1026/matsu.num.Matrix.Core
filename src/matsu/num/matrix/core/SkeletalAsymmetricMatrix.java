/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core;

import java.util.function.Supplier;

import matsu.num.matrix.core.lazy.ImmutableLazyCacheSupplier;

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
 * この骨格実装クラスの継承関係は積極的には維持されず,
 * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
 * </p>
 * 
 * @author Matsuura Y.
 * @param <TT>
 *            転置行列の型を表す. <br>
 *            サブクラスで型をバインドすることで, {@code transpose()} の戻り値型を共変で扱うために用意されている.
 */
public abstract class SkeletalAsymmetricMatrix<TT extends Matrix> implements Matrix {

    //転置行列を生成するサプライヤ
    private final Supplier<TT> transposeSupplier;

    /**
     * 唯一のコンストラクタ.
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
            throw new AssertionError("ImplSpec fault");
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
     * 公開は禁止されており, サブクラスからもコールしてはならない.
     * </p>
     *
     * @implSpec
     * 
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
     * {@code Matrix[dim:%dimension]}
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
                "Matrix[dim: %s]",
                this.matrixDimension());
    }
}
