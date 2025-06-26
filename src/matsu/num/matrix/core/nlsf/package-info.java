/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/**
 * LU分解に準じた行列分解を使用した, 線形連立方程式の解法を扱うパッケージ.
 * 
 * <p>
 * 線形連立方程式を解くための機能は逆行列を返すという形で表現され,
 * {@link matsu.num.matrix.core.Inversion} を継承した
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver}
 * のサブタイプにより提供される. <br>
 * そしてインスタンスの生成は,
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver.Executor}
 * のサブタイプにより提供される. <br>
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver} は同時に
 * {@link matsu.num.matrix.core.Determinantable} を継承しており,
 * 行列式の計算も可能である.
 * </p>
 * 
 * <p>
 * 行列が特異の場合は逆行列が生成できないが,
 * そのことは,
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver.Executor}
 * による行列分解が {@link java.util.Optional} として空を返すという形で表現されている. <br>
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver}
 * のインスタンスは行列分解が成功していることを表しており,
 * 必ず逆行列を取得できる.
 * </p>
 * 
 * <p>
 * 行列が正定値対称行列の場合, 対称化二乗型分解:
 * A = BB<sup>T</sup> が存在する. <br>
 * {@link matsu.num.matrix.core.nlsf.LUTypeSolver}
 * のサブタイプである {@link matsu.num.matrix.core.nlsf.SymmetrizedSquareTypeSolver}
 * は, A<sup>-1</sup> だけでなく,
 * B, B<sup>-1</sup> の取得についても提供している.
 * </p>
 * 
 * <p>
 * このパッケージでは, 次のような分解方法が用意されている. <br>
 * インスタンスの生成方法を同時に提供している.
 * </p>
 * 
 * <ul>
 * <li>{@link matsu.num.matrix.core.nlsf.LUPivoting}:
 * 部分ピボッティング付きLU分解</li>
 * <li>{@link matsu.num.matrix.core.nlsf.LUBand}:
 * 帯行列向けLU分解</li>
 * <li>{@link matsu.num.matrix.core.nlsf.ModifiedCholeskyPivoting}:
 * 部分ピボッティング付き修正Cholesky分解</li>
 * <li>{@link matsu.num.matrix.core.nlsf.ModifiedCholeskyBand}:
 * 帯行列向け修正Cholesky分解</li>
 * <li>{@link matsu.num.matrix.core.nlsf.Cholesky}:
 * Cholesky分解</li>
 * <li>{@link matsu.num.matrix.core.nlsf.CholeskyBand}:
 * 帯行列向けCholesky分解</li>
 * </ul>
 * 
 */
package matsu.num.matrix.core.nlsf;
