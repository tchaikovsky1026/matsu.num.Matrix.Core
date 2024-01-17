/**
 * 2023.12.22
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;
import matsu.num.matrix.base.helper.matrix.multiply.MatrixMultiplication;

/**
 * <p>
 * 矩形(長方形)の行列を扱う.
 * </p>
 * 
 * <p>
 * このインターフェースを実装した全てのクラスの属性は実質的に不変であり,
 * (このインターフェース以外を含む) 全てのメソッドは関数的かつスレッドセーフである. <br>
 * (実装者にはそのようにクラス設計することを強制し, 違反した場合は振る舞いが保証されない.)
 * </p>
 * 
 * <p>
 * 実装仕様: <br>
 * {@link Symmetric} インターフェースが付与される場合, 必ず正方形次元 (サイズ) でなければならない. <br>
 * すなわち, <br>
 * {@code this.matrixDimension().isSquare() == true} <br>
 * でなければならない.
 * </p>
 *
 * @author Matsuura Y.
 * @version 18.0
 */
public interface Matrix {

    /**
     * <p>
     * 行列の次元 (サイズ) を取得する.
     * </p>
     *
     * @return 行列の次元
     */
    public MatrixDimension matrixDimension();

    /**
     * <p>
     * 行列に右からベクトルを作用させる: <b>w</b> = M<b>v</b>.
     * <br>
     * M: 行列({@code this}). <br>
     * <b>v</b>: 右から作用させるベクトル. <br>
     * <b>w</b>: 計算結果の出力変数ベクトル.
     * </p>
     *
     * @param operand <b>v</b>, 作用ベクトル
     * @return 計算結果 <b>w</b>
     * @throws MatrixFormatMismatchException 作用ベクトル <b>v</b> の次元が行列サイズと適合しない場合
     * @throws IllegalArgumentException 計算の結果に不正値が混入し, ベクトルが生成できない場合場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector operate(Vector operand);

    /**
     * <p>
     * 行列の転置に右からベクトルを作用させる:
     * <b>w</b> = M<sup>T</sup><b>v</b>. <br>
     * M: {@code this} を転置した行列. <br>
     * <b>v</b>: 右から作用させるベクトル. <br>
     * <b>w</b>: 計算結果の出力変数ベクトル.
     * </p>
     *
     * @param operand <b>v</b>, 作用ベクトル
     * @return 計算結果 <b>w</b>
     * @throws MatrixFormatMismatchException 作用ベクトル <b>v</b> の次元が行列サイズと適合しない場合
     * @throws IllegalArgumentException 計算の結果に不正値が混入し, ベクトルが生成できない場合場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector operateTranspose(Vector operand);

    /**
     * <p>
     * この行列の転置行列を返す.
     * </p>
     * 
     * @return 転置行列
     */
    public abstract Matrix transpose();

    /**
     * 1個以上の行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix multiply(Matrix first, Matrix... following) {
        return MatrixMultiplication.instance().apply(first, following);
    }

    /**
     * 行列の対称化二乗を返す. <br>
     * すなわち, 与えた行列 A に対して, AA<sup>T</sup> を返す. <br>
     * 戻り値には {@linkplain Symmetric} が付与されている.
     * 
     * @param original 元の行列
     * @return 対称行列積
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetrizedSquare(Matrix original) {
        return MatrixMultiplication.instance().symmetrizedSquare(original);
    }

    /**
     * 対称な行列積を返す. <br>
     * すなわち, 与えた行列 L, D に対して, LDL<sup>T</sup> を返す. <br>
     * 戻り値には {@linkplain Symmetric} が付与されている. <br>
     * 与える行列Dには {@linkplain Symmetric} が付与されていなければならない.
     * 
     * @param mid 行列 D, 中央の行列
     * @param leftSide 行列 L, 左サイドの行列
     * @return 対称な行列積
     * @throws MatrixNotSymmetricException 中央の行列が対称でない場合
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetricMultiply(Matrix mid, Matrix leftSide) {
        return MatrixMultiplication.instance().symmetricMultiply(mid, leftSide);
    }

    /**
     * {@linkplain Matrix} インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim(%dimension)]} <br>
     * {@code Matrix[dim(%dimension), %character1, %character2,...]}
     * </p>
     * 
     * <p>
     * {@code matrix} が {@code null} の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     * 
     * @param matrix インスタンス
     * @param characters 付加する属性表現
     * @return 説明表現
     */
    public static String toString(Matrix matrix, String... characters) {
        if (Objects.isNull(matrix)) {
            return "null";
        }

        StringBuilder fieldString = new StringBuilder();
        fieldString.append(String.format("dim%s", matrix.matrixDimension()));

        if (Objects.nonNull(characters)) {
            for (String character : characters) {
                fieldString.append(", ")
                        .append(character);
            }
        }

        return String.format(
                "Matrix[%s]",
                fieldString.toString());
    }

}
