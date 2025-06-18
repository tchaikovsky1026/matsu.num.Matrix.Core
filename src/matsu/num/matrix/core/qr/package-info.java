/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/**
 * QR分解を使用した, 線形連立方程式の最小二乗解や最小ノルム解の求解を扱うパッケージ.
 * 
 * <p>
 * 最小二乗解 &middot; や最小ノルム解の求解機能は
 * Moore-Penrose 一般化逆行列を返すという形で表現され,
 * {@link matsu.num.matrix.core.GeneralizedInversion} を継承した
 * {@link matsu.num.matrix.core.qr.QRTypeSolver}
 * のサブタイプにより提供される. <br>
 * そしてインスタンスの生成は,
 * {@link matsu.num.matrix.core.qr.QRTypeSolver.Executor}
 * のサブタイプにより提供される.
 * </p>
 * 
 * <p>
 * QR分解は通常, 正方行列あるいは縦長矩形行列に対して定義されるのに対し,
 * このパッケージで扱うQR分解は, 列フルランクのもののみを扱う. <br>
 * 列フルランクでない行列に対してQR分解が失敗することは,
 * {@link matsu.num.matrix.core.qr.QRTypeSolver.Executor}
 * による行列分解が {@link java.util.Optional} として空を返すという形で表現されている. <br>
 * {@link matsu.num.matrix.core.qr.QRTypeSolver}
 * のインスタンスは行列分解が成功していることを表しており,
 * 必ず一般化逆行列を取得できる.
 * </p>
 * 
 * <p>
 * このパッケージでは, 次のような分解方法が用意されている. <br>
 * インスタンスの生成方法を同時に提供している.
 * </p>
 * 
 * <ul>
 * <li>{@link matsu.num.matrix.core.qr.HouseholderQR}:
 * Householder 変換によるQR分解</li>
 * <li>{@link matsu.num.matrix.core.qr.HouseholderQRBand}:
 * Householder 変換による帯行列向けQR分解</li>
 * </ul>
 * 
 */
package matsu.num.matrix.core.qr;
