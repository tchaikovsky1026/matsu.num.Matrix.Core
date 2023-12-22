/**
 * 2023.8.18
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;

/**
 * 紐づけられた行列を係数とする線形連立方程式についての, 解法向け行列分解を扱う. 
 * 
 * <p>
 * このインターフェースを実装したクラスは生成時(コンストラクタ呼び出し)の時点で行列分解を行う. 
 * したがって, 生成された時点で逆行列を返せることが確定する.
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
public interface LinearEquationSolving<T extends Matrix> extends Inversion, Determinantable {

    /**
     * このインターフェースが紐づく行列を返す. 
     * 
     * @return このインターフェースが紐づく行列
     */
    @Override
    public abstract T target();

    /**
    * {@inheritDoc } 
    * 
    * <p>
    * {@linkplain LinearEquationSolving}の仕様により逆行列を返せることが確定しているため, 空ではない.
    * </p>
    * 
    */
    @Override
    public abstract Optional<? extends Matrix> inverse();

    /**
     * {@inheritDoc } 
     * 
     * <p>
     * {@linkplain LinearEquationSolving}の仕様により逆行列が存在することが確定しているため, 
     * このメソッドの戻り値は0にならない.
     * </p>
     * 
     * @return 行列式の符号, 行列式の値が正, 負のときそれぞれ1, -1
     */
    @Override
    public abstract int signOfDeterminant();

    /**
     * {@linkplain LinearEquationSolving}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %分解方法[target: %matrix]}
     * </p>
     * 
     * <p>
     * {@code instance}が{@code null}の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     *  
     * @param instance インスタンス 
     * @param methodName 分解方法
     * @return 説明表現
     */
    public static String toString(LinearEquationSolving<? extends Matrix> instance, String methodName) {
        if (Objects.isNull(instance)) {
            return "null";
        }

        return String.format(
                "%s[target:%s]",
                Objects.nonNull(methodName) ? methodName : "AnonymousSolver",
                instance.target());
    }

    /**
     * {@linkplain LinearEquationSolving}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code AnonymousSolver[target: %matrix]}
     * </p>
     * 
     * <p>
     * {@code instance}が{@code null}の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     *  
     * @param instance インスタンス 
     * @return 説明表現
     */
    public static String toString(LinearEquationSolving<? extends Matrix> instance) {
        return LinearEquationSolving.toString(instance, null);
    }

}
