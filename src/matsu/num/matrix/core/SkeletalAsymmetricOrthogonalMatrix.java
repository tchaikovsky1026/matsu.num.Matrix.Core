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
 * 非対称な {@link OrthogonalMatrix} の骨格実装. <br>
 * {@link OrthogonalMatrix#transpose()}, {@link OrthogonalMatrix#inverse()}
 * の実装の提供が主な効果である.
 * 
 * <p>
 * <i> <u>
 * 具象クラスが {@link Symmetric} を付与されるなら,
 * {@link SkeletalSymmetricOrthogonalMatrix} を使用しなければならない. <br>
 * (このクラスからの継承は禁止である.)
 * </u> </i>
 * </p>
 * 
 * <p>
 * このクラスは, {@link OrthogonalMatrix#transpose()},
 * {@link OrthogonalMatrix#inverse()}
 * の適切な実装を提供する. <br>
 * 初めてそれらが呼ばれたときに転置行列を
 * {@link #createTranspose()} によって生成,
 * その {@link Optional} をキャッシュし, 以降はそのキャッシュを戻す.
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
 *            サブクラスで型をバインドすることで,
 *            {@code transpose()}, {@code inverse()}
 *            の戻り値型を共変で扱うために用意されている.
 * @deprecated リファクタリング中で, 一時的に非推奨とする
 */
@Deprecated
public abstract class SkeletalAsymmetricOrthogonalMatrix<TT extends OrthogonalMatrix>
        extends matsu.num.matrix.core.helper.matrix.SkeletalAsymmetricOrthogonalMatrix<TT>
        implements OrthogonalMatrix {

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
    protected SkeletalAsymmetricOrthogonalMatrix() {
        super();
    }
}
