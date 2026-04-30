/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.4.30
 */
package matsu.num.matrix.core;

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
 * @deprecated
 *                 この骨格実装は version 29 以降に削除される. <br>
 *                 代替となるクラスは公開されていない.
 */
@Deprecated(forRemoval = true, since = "28.6")
public abstract class SkeletalSymmetricMatrix<T extends SkeletalSymmetricMatrix<T>>
        extends matsu.num.matrix.core.helper.matrix.SkeletalSymmetricMatrix<T>
        implements Matrix, Symmetric {

    /**
     * 唯一のコンストラクタ.
     */
    protected SkeletalSymmetricMatrix() {
        super();
    }
}
