/**
 * 2023.8.21
 */
package matsu.num.matrix.base.helper.matrix.multiply;

import java.util.Deque;
import java.util.Objects;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.OrthogonalMatrix;

/**
 * 直交行列が行列積として表現されていることを通知可能にするインターフェース. 
 * 
 * @author Matsuura Y.
 * @version 15.1
 */
public interface MultipliedOrthogonalMatrix extends MultipliedMatrix, OrthogonalMatrix {
    /**
     * 行列積の表現を{@linkplain Deque}として返す. 
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返差なければならない. 
     * </p>
     * 
     * @return 行列積の表現
     */
    @Override
    public abstract Deque<? extends OrthogonalMatrix> toSeries();

    /**
     * {@linkplain MultipliedMatrix}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code @hashCode[dimension: %dimension, multi: %multi, orthogonal]} <br>
     * {@code @hashCode[dimension: %dimension, multi: %multi, orthogonal, %character1, %character2,...]}
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
    public static String toString(MultipliedOrthogonalMatrix matrix, String... characters) {
        if (Objects.isNull(characters)) {
            characters = new String[0];
        }

        //与えられたcharactersの最初にmulti項,orthogonalを加える
        String[] additionalCharacters = new String[characters.length + 2];
        additionalCharacters[0] = "multi:" + matrix.toSeries().size();
        additionalCharacters[1] = "orthogonal";
        System.arraycopy(characters, 0, additionalCharacters, 2, characters.length);
        return Matrix.toString(matrix, additionalCharacters);
    }
}
