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

import java.util.Optional;

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
 *            サブクラスで型をバインドすることで,
 *            {@code transpose()}, {@code inverse()}
 *            の戻り値型を共変で扱うために用意されている.
 * @deprecated リファクタリング中で, 一時的に非推奨とする
 */
@Deprecated
public abstract class SkeletalSymmetricOrthogonalMatrix<
        T extends SkeletalSymmetricOrthogonalMatrix<T>>
        extends matsu.num.matrix.core.helper.matrix.SkeletalSymmetricOrthogonalMatrix<T>
        implements OrthogonalMatrix, Symmetric {

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalSymmetricOrthogonalMatrix() {
        super();
    }
}
