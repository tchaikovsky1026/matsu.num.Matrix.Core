/**
 * 2023.11.30
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.matrix.base.helper.matrix.transpose.TranspositionEntryReadable;

/**
 * 成分に<i>O</i>(1)でアクセス可能な行列. <br>
 * 成分に不正値(inf,NaN)を含んではいけない.
 * 
 * <p>
 * {@link Matrix}のクラス説明の規約に従う.
 * </p>
 * 
 * <p>
 * 値の検証には{@link #acceptValue(double) }メソッドを使用する.
 * </p>
 *
 * @author Matsuura Y.
 * @version 17.1
 * @see Matrix
 */
public interface EntryReadableMatrix extends Matrix {

    /**
     * (<i>i</i>,<i>j</i>) 要素の値を取得する.
     *
     * @param row i, 行index
     * @param column j, 列index
     * @return (i,j) 要素の値
     * @throws IndexOutOfBoundsException (i,j)が行列の内部でない場合
     */
    public double valueAt(int row, int column);

    /**
     * 成分ごとのノルムの最大ノルムを計算する.
     *
     * @return 成分ごとのノルムの最大ノルム
     */
    public double entryNormMax();

    /**
     * {@link EntryReadableMatrix}の成分として有効な値であるかを判定する.
     *
     * @param value 検証する値
     * @return 有効である場合はtrue
     */
    public static boolean acceptValue(double value) {
        return Double.isFinite(value);
    }

    /**
     * 行列の転置行列を生成する. <br>
     * {@linkplain Symmetric}が付与されている場合, 戻り値も{@linkplain Symmetric}である.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static EntryReadableMatrix createTransposedOf(EntryReadableMatrix original) {
        return TranspositionEntryReadable.instance().apply(original);
    }

    /**
     * {@linkplain EntryReadableMatrix}インターフェースを実装したクラス向けの文字列説明表現を提供する. <br>
     * ただし, サブタイプがより良い文字列表現を提供するかもしれない.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code EntryReadableMatrix[dim(%dimension), %entry]} <br>
     * {@code EntryReadableMatrix[dim(%dimension), %character1, %character2,..., %entry]}
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
    public static String toString(EntryReadableMatrix matrix, String... characters) {
        if (Objects.isNull(matrix)) {
            return "null";
        }

        StringBuilder fieldString = new StringBuilder();
        fieldString.append("dim")
                .append(matrix.matrixDimension());

        if (Objects.nonNull(characters)) {
            for (String character : characters) {
                fieldString.append(", ")
                        .append(character);
            }
        }

        fieldString.append(", ")
                .append(EntryReadableMatrix.toSimplifiedEntryString(matrix));

        return String.format(
                "EntryReadableMatrix[%s]",
                fieldString.toString());
    }

    /**
     * {@linkplain EntryReadableMatrix}の成分の値についての簡略化された文字列表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code {{*, *, ...}, {...}, ...}}
     * </p>
     * 
     * @param matrix インスタンス
     * @return 説明表現
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static String toSimplifiedEntryString(EntryReadableMatrix matrix) {

        final int maxDisplaySize = 3;

        StringBuilder entryString = new StringBuilder();

        entryString.append('{');

        //1段目
        {
            //rowIndexは0
            final int j = 0;

            entryString.append('{');
            final int columnDimension = matrix.matrixDimension().columnAsIntValue();
            final int columnDisplaySize = Math.min(maxDisplaySize, columnDimension);
            for (int k = 0; k < columnDisplaySize; k++) {
                entryString.append(matrix.valueAt(j, k));
                if (k < columnDisplaySize - 1) {
                    entryString.append(", ");
                }
            }
            if (columnDimension > columnDisplaySize) {
                entryString.append(", ...");
            }
            entryString.append('}');
        }

        //2段目以降
        final int rowDimension = matrix.matrixDimension().rowAsIntValue();
        final int rowDisplaySize = Math.min(maxDisplaySize, rowDimension);
        for (int j = 1; j < rowDisplaySize; j++) {
            entryString.append(", ")
                    .append("{...}");
        }
        if (rowDimension > rowDisplaySize) {
            entryString.append(", ...");
        }

        entryString.append('}');

        return entryString.toString();
    }

    /**
     * 行列の全成分をカンマ区切り形式に変換する.
     * 
     * <p>
     * すべての成分が文字列として出力されるため,
     * そのサイズに注意せよ.
     * </p>
     * 
     * @param matrix 行列
     * @return {@link String}形式に変換された行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static String allEntryToCSVFormat(EntryReadableMatrix matrix) {
        String sp = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, rs = matrix.matrixDimension().rowAsIntValue(); i < rs; i++) {
            for (int j = 0, cs = matrix.matrixDimension().columnAsIntValue(); j < cs; j++) {
                if (j != 0) {
                    sb.append(", ");
                }
                sb.append(matrix.valueAt(i, j));
            }
            sb.append(sp);
        }
        sb.append(sp);
        return sb.toString();
    }
}
