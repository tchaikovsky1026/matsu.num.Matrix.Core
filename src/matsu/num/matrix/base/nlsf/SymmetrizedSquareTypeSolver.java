/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;

/**
 * <p>
 * 対称化二乗型分解:
 * 正定値対称行列 A について A = BB<sup>T</sup> と分解された
 * {@linkplain LUTypeSolver}. <br>
 * B に関する線形連立方程式解法の機能も提供する. <br>
 * 主に, Cholesky分解の抽象化.
 * </p>
 * 
 * <p>
 * {@linkplain #target()} によって返される行列 A,
 * {@linkplain #inverse()} によって返される行列 A<sup>-1</sup> には,
 * {@linkplain Symmetric} が付与されている.
 * </p>
 * 
 * <p>
 * 紐づけられる {@linkplain Matrix} はインスタンスの生成と同時に決定される. <br>
 * {@linkplain Matrix} が不変であるので, このインターフェースにかかわる属性は実質的に不変であり,
 * 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public interface SymmetrizedSquareTypeSolver extends LUTypeSolver {

    /**
     * <p>
     * 非対称平方根を返す. <br>
     * すなわち, A = BB<sup>T</sup>と分解したときの B を返す.
     * </p>
     * 
     * @return 非対称平方根 B
     */
    public abstract Matrix asymmSqrt();

    /**
     * <p>
     * 非対称平方根の逆行列を返す. <br>
     * すなわち, A = BB<sup>T</sup>と分解したときの B<sup>-1</sup> を返す.
     * </p>
     * 
     * @return 非対称平方根の逆行列 B<sup>-1</sup>
     */
    public abstract Matrix inverseAsymmSqrt();
}
