/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.27
 */
package matsu.num.matrix.core;

/**
 * {@link SignatureMatrix} の実装をこのモジュール内に制限するためのヘルパーインターフェース. <br>
 * {@link SignatureMatrix} の実装を不可視なクラスで実現するため,
 * パッケージプライベートなシールインターフェースを定義する.
 * 
 * @author Matsuura Y.
 * @version 23.1
 */
non-sealed interface SignatureMatrixSealed extends SignatureMatrix {

}
