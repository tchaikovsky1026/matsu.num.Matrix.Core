/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.18
 */
package matsu.num.matrix.core.helper.matrix.householder;

import matsu.num.matrix.core.HouseholderMatrix;

/**
 * {@link HouseholderMatrix} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link HouseholderMatrix} の実装を不可視なクラスで実現するため,
 * 非公開パッケージで継承先を定義する.
 * 
 * @author Matsuura Y.
 */
public non-sealed interface HouseholderMatrixSealed extends HouseholderMatrix {

}
