/**
 * 2023.12.22
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.helper.matrix.transpose.TranspositionBand;

/**
 * 正方形の帯行列を扱う.
 * 
 * <p>
 * {@linkplain Matrix} のクラス説明の規約に従う.
 * </p>
 * 
 * <p>
 * 実装仕様: <br>
 * {@linkplain Symmetric} インターフェースが付与される場合, 必ず対称帯構造でなければならない. <br>
 * すなわち, <br>
 * {@code this.bandMatrixDimension().isSymmetric() == true} <br>
 * でなければならない.
 * </p>
 *
 * @author Matsuura Y.
 * @version 18.0
 * @see Matrix
 */
public interface BandMatrix extends EntryReadableMatrix {

    /**
     * 行列の帯行列構造を取得する.
     *
     * @return 行列の帯行列構造
     */
    public BandMatrixDimension bandMatrixDimension();

    @Override
    public default MatrixDimension matrixDimension() {
        return this.bandMatrixDimension().dimension();
    }

    /**
     * 帯行列の転置行列を生成する.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static BandMatrix createTransposedOf(BandMatrix original) {
        return TranspositionBand.instance().apply(original);
    }

    /**
     * {@linkplain BandMatrix} インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code BandMatrix[struct[%bandStructure], %entry]} <br>
     * {@code BandMatrix[struct[%bandStructure], %character1, %character2,..., %entry]}
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
    public static String toString(BandMatrix matrix, String... characters) {
        if (Objects.isNull(matrix)) {
            return "null";
        }

        StringBuilder fieldString = new StringBuilder();
        fieldString.append("struct");
        fieldString.append(matrix.bandMatrixDimension());

        if (Objects.nonNull(characters)) {
            for (String character : characters) {
                fieldString.append(", ");
                fieldString.append(character);
            }
        }
        fieldString.append(", ");
        fieldString.append(EntryReadableMatrix.toSimplifiedEntryString(matrix));

        return String.format(
                "BandMatrix[%s]",
                fieldString.toString());
    }
}
