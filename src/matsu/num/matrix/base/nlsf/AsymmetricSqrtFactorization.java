/**
 * 2023.8.18
 */
package matsu.num.matrix.base.nlsf;

import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;

/**
 * 正定値対称行列AについてA = BB<sup>T</sup>と分解する{@linkplain LinearEquationSolving}. <br>
 * Bに関する線形連立方程式解法の機能も提供する.
 * 
 * <p>
 * Cholesky分解の抽象化を想定している. 
 * </p>
 * 
 * <p>
 * 紐づけられる{@linkplain Matrix}はインスタンスの生成と同時に決定される. <br>
 * {@linkplain Matrix}が不変であるので, このインターフェースにかかわる属性は実質的に不変であり, 
 * 全てのメソッドは関数的かつスレッドセーフである. 
 * </p>
 * 
 * @author Matsuura Y.
 * @version 15.0
 * @param <T> 紐づけられた行列の型パラメータ
 */
public interface AsymmetricSqrtFactorization<T extends Matrix> extends LinearEquationSolving<T> {

    /**
     * {@inheritDoc } 
     * 
     * <p>
     * {@linkplain Symmetric}が付与されている.
     * </p>
     *  
     * @see Symmetric
     */
    @Override
    public abstract Optional<? extends Matrix> inverse();

    /**
     * 非対称平方根を返す. <br>
     * すなわち, A = BB<sup>T</sup>と分解したときのBに関する仕組みを返す.
     * 
     * @return 非対称平方根に関する仕組み
     */
    public abstract LinearEquationSolving<Matrix> asymmetricSqrtSystem();
}
