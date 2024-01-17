/**
 * 2023.12.25
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;

/**
 * <p>
 * LU分解型の, 線形連立方程式求解を扱う. <br>
 * 求解のための逆行列の提供と, ターゲット行列の行列式計算をサポートする.
 * </p>
 * 
 * <p>
 * LU分解とは, 正則な正方行列 A を, <br>
 * A = LU, <br>
 * A = LDU, <br>
 * A = PLDU, <br>
 * といった形に分解することである. <br>
 * ただし, L は下三角行列, U は上三角行列, D は対角行列, P は置換行列を表す. <br>
 * A についての逆行列
 * (つまり, 係数行列 A に対する線形連立方程式の解法)
 * と, A の行列式が得られるのがこの分解の特徴である.
 * </p>
 * 
 * <p>
 * このインターフェースを実装したクラスは生成時 (コンストラクタ呼び出し) の時点で行列分解を行う. <br>
 * したがって, 生成された時点で逆行列を返せることが確定し, 行列式は0でない.
 * </p>
 * 
 * <p>
 * 紐づけられる {@linkplain Matrix} はインスタンスの生成と同時に決定される. <br>
 * {@linkplain Matrix} が不変であるので, このインターフェースにかかわる属性は実質的に不変であり,
 * 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 18.0
 */
public interface LUTypeSolver
        extends Inversion, Determinantable {

    /**
     * {@linkplain LUTypeSolver} インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %分解方法[target: %matrix]}
     * </p>
     * 
     * <p>
     * {@code instance} が {@code null} の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     * 
     * @param instance インスタンス
     * @param methodName 分解方法
     * @return 説明表現
     */
    public static String toString(LUTypeSolver instance, String methodName) {
        if (Objects.isNull(instance)) {
            return "null";
        }

        return String.format(
                "%s[target:%s]",
                Objects.nonNull(methodName) ? methodName : "AnonymousSolver",
                instance.target());
    }

    /**
     * {@linkplain LUTypeSolver} インターフェースを実装した,
     * 手法非公開のクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code AnonymousSolver[target: %matrix]}
     * </p>
     * 
     * <p>
     * {@code instance} が {@code null} の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     * 
     * @param instance インスタンス
     * @return 説明表現
     */
    public static String toString(LUTypeSolver instance) {
        return LUTypeSolver.toString(instance, null);
    }

}
