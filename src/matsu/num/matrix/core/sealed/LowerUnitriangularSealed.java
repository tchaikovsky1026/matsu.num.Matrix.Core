/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.20
 */
package matsu.num.matrix.core.sealed;

import matsu.num.matrix.core.LowerUnitriangular;

/**
 * {@link LowerUnitriangular} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link LowerUnitriangular} の実装を不可視なクラスで実現するため,
 * 非公開パッケージで継承先を定義する.
 * 
 * <p>
 * このインターフェースはおそらく実装されない. <br>
 * シールが"網羅的"にならないように用意する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public non-sealed interface LowerUnitriangularSealed extends LowerUnitriangular {

}
