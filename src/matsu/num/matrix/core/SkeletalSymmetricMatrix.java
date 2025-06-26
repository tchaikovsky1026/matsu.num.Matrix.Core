/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.14
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link Symmetric} が付与された {@link Matrix} の骨格実装.
 * 
 * <p>
 * このクラスは, {@link Matrix#transpose()} の適切な実装を提供する. <br>
 * {@code transpose()} の戻り値は {@code this} である. <br>
 * ただし, 戻り値型をサブタイプに限定できるようにするため, ジェネリクスと {@code self()} メソッドの実装を要求する.
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
 * @param <T>
 *            {@code this} の具象型を表す. <br>
 *            サブクラスで型をバインドすることで, {@code transpose()} の戻り値型を共変で扱うために用意されている.
 */
public abstract class SkeletalSymmetricMatrix<T extends SkeletalSymmetricMatrix<T>>
        implements Matrix, Symmetric {

    /**
     * 唯一のコンストラクタ.
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
                "Matrix[dim: %s]", this.matrixDimension());
    }
}
