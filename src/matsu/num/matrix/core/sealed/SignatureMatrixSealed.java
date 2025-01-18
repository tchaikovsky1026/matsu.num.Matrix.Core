/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.18
 */
package matsu.num.matrix.core.sealed;

import matsu.num.matrix.core.SignatureMatrix;

/**
 * {@link SignatureMatrix} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link SignatureMatrix} の実装を不可視なクラスで実現するため,
 * パッケージプライベートなシールインターフェースを定義する.
 * 
 * @author Matsuura Y.
 */
public non-sealed interface SignatureMatrixSealed extends SignatureMatrix {

}
