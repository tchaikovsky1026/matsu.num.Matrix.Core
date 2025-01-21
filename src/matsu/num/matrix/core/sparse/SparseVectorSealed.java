/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.21
 */
package matsu.num.matrix.core.sparse;

/**
 * {@link SparseVector} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link SparseVector} の実装を不可視なクラスで実現するため,
 * パッケージプライベートで継承先を定義する.
 * 
 * @author Matsuura Y.
 */
non-sealed interface SparseVectorSealed extends SparseVector {

}
