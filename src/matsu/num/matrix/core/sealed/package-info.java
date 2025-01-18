/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * 公開するシールインターフェースの継承先を定義するための非公開パッケージ.
 * 
 * <p>
 * 戻り値型として主に用いられる, 実装隠蔽 &middot; ポリモーフィズムのために用意された公開インターフェースをシールするため,
 * {@code permits} されるインターフェースをこのパッケージに制限する.
 * </p>
 */
package matsu.num.matrix.core.sealed;
