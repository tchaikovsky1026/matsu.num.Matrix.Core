/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.18
 */
package matsu.num.matrix.core.sealed;

import matsu.num.matrix.core.ZeroMatrix;

/**
 * {@link ZeroMatrix} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link ZeroMatrix} の実装を不可視なクラスで実現するため,
 * 非公開パッケージで継承先を定義する.
 * 
 * @author Matsuura Y.
 */
public non-sealed interface ZeroMatrixSealed extends ZeroMatrix {

}
