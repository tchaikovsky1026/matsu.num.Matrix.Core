/**
 * 2023.8.20
 */
package matsu.num.matrix.base.helper.matrix.multiply;

import java.util.Deque;
import java.util.Objects;

import matsu.num.matrix.base.Matrix;

/**
 * 行列が行列積として表現されていることを通知可能にするインターフェース. 
 * 
 * @author Matsuura Y.
 * @version 15.1
 */
public interface MultipliedMatrix extends Matrix {

    /**
     * 行列積の表現を{@linkplain Deque}として返す. 
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返差なければならない. 
     * </p>
     * 
     * @return 行列積の表現
     */
    public abstract Deque<? extends Matrix> toSeries();

    /**
     * {@linkplain MultipliedMatrix}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code @hashCode[dimension: %dimension, multi: %multi]} <br>
     * {@code @hashCode[dimension: %dimension, multi: %multi, %character1, %character2,...]}
     * </p>
     * 
     * <p>
     * {@code matrix}が{@code null}の場合は, おそらくは次であろう. <br>
     * {@code null}
     * </p>
     * 
     * @param matrix インスタンス
     * @param characters 付加する属性表現
     * @return 説明表現
     */
    public static String toString(MultipliedMatrix matrix, String... characters) {
        if (Objects.isNull(characters)) {
            characters = new String[0];
        }

        //与えられたcharactersの最初にmulti項を加える
        String[] additionalCharacters = new String[characters.length + 1];
        additionalCharacters[0] = "multi:" + matrix.toSeries().size();
        System.arraycopy(characters, 0, additionalCharacters, 1, characters.length);
        return Matrix.toString(matrix, additionalCharacters);
    }
}
