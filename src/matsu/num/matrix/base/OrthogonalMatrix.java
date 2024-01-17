/**
 * 2023.12.25
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.multiply.OrthogonalMatrixMultiplication;

/**
 * <p>
 * 直交行列であることを表す.
 * </p>
 * 
 * <p>
 * 直交行列は転置行列が逆行列に一致する. <br>
 * したがって, 逆行列は必ず取得できる.
 * </p>
 * 
 * <p>
 * {@link Matrix}のクラス説明の規約に従う.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 18.0
 * @see Matrix
 */
public interface OrthogonalMatrix
        extends Matrix, Inversion {

    @Override
    public abstract OrthogonalMatrix target();

    /**
     * この行列の転置行列を返す.
     * 
     * @return 転置行列
     */
    @Override
    public abstract OrthogonalMatrix transpose();

    /**
     * <p>
     * この行列の逆行列を返す. <br>
     * {@linkplain Optional#get()} は必ず成功する.
     * </p>
     */
    @Override
    public abstract Optional<? extends OrthogonalMatrix> inverse();

    /**
     * 1個以上の直交行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 直交行列の行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix multiply(OrthogonalMatrix first, OrthogonalMatrix... following) {
        return OrthogonalMatrixMultiplication.instance().apply(first, following);
    }

    /**
     * {@linkplain OrthogonalMatrix}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code OrthogonalMatrix[dim(%dimension)]} <br>
     * {@code OrthogonalMatrix[dim(%dimension), %character1, %character2,...]}
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
    public static String toString(OrthogonalMatrix matrix, String... characters) {
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
                "OrthogonalMatrix[%s]",
                fieldString.toString());
    }

}
