/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.15
 */
package matsu.num.matrix.core;

/**
 * 成分に <i>O</i>(1) でアクセス可能な行列を表す. <br>
 * 成分に不正値 (inf, NaN) を含まない
 * (扱うことができる値は {@link #MIN_VALUE}, {@link #MAX_VALUE} で規定される).
 * 
 * <p>
 * <i>
 * ライブラリ開発向けに,
 * 成分として適当かどうかを判断する {@link #acceptValue(double)} メソッド,
 * 値の修正を行う {@link #modified(double)} メソッドが用意されている.
 * </i>
 * </p>
 * 
 * @implSpec
 *               <p>
 *               {@link Matrix} の規約に従う.
 *               </p>
 * 
 *               <p>
 *               行列の成分には, 行列サイズに比例しない定数時間でアクセスできなければならない. <br>
 *               各成分の値 ({@link #valueAt(int, int)} メソッドにより取得される値)
 *               は扱える範囲でなければならない.
 *               </p>
 *
 * @author Matsuura Y.
 */
public interface EntryReadableMatrix extends Matrix {

    /**
     * 扱うことができる成分の最大値.
     */
    public static final double MAX_VALUE = Double.MAX_VALUE;

    /**
     * 扱うことができる成分の最小値.
     */
    public static final double MIN_VALUE = -Double.MAX_VALUE;

    /**
     * (<i>i</i>, <i>j</i>) 要素の値を取得する.
     *
     * @param row <i>i</i>, 行index
     * @param column <i>j</i>, 列index
     * @return (<i>i</i>, <i>j</i>) 要素の値
     * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が行列の内部でない場合
     */
    public double valueAt(int row, int column);

    /**
     * 成分ごとのノルムの最大ノルムを返す.
     *
     * @return 成分ごとのノルムの最大ノルム
     */
    public double entryNormMax();

    /**
     * 
     * @deprecated
     *                 この共変戻り値となった {@code transpose()} は使用すべきでない. <br>
     *                 {@code Matrix.transpose()} か
     *                 {@link #transposeReadable()}
     *                 を使用すべき.
     * 
     *                 <p>
     *                 <i><u>
     *                 version 29 以降に削除され, {@code Matrix.transpose()}
     *                 が呼ばれるようになる.
     *                 </u></i>
     *                 </p>
     */
    @Deprecated(forRemoval = true, since = "28.9")
    @Override
    public abstract EntryReadableMatrix transpose();

    /**
     * この行列の転置行列を, 成分アクセス可能な形で返す.
     * 
     * @implSpec
     *               線形継承されたサブインターフェースでのみ, 型精密化を認める. <br>
     *               その他は, {@link Matrix#transpose()} に従う.
     * 
     *               <p>
     *               <i><u>
     *               version 29 以降デフォルトメソッドが削除されるので,
     *               実装側は必ずオーバーライドすること.
     *               </u></i>
     *               </p>
     * 
     * @return 転置行列
     */
    public default EntryReadableMatrix transposeReadable() {
        return transpose();
    }

    /**
     * {@link EntryReadableMatrix} の成分として有効な値であるかを判定する.
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドを呼ぶ必要があるのは,
     * 行列演算のコアな計算のライブラリ作成の状況であろう. <br>
     * ライブラリを使うユーザーが呼ぶことは, おそらく非推奨である.
     * </i>
     * </u>
     * </p>
     *
     * @param value 検証する値
     * @return 有効である場合はtrue
     */
    public static boolean acceptValue(double value) {
        return MIN_VALUE <= value && value <= MAX_VALUE;
    }

    /**
     * <p>
     * 与えられた値を成分として使用できるように修正する. <br>
     * 正常値を与えた場合はそのまま, 不正な値を与えた場合は正常な値に修正して返す.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドを呼ぶ必要があるのは,
     * 行列演算のコアな計算のライブラリ作成の状況であろう. <br>
     * ライブラリを使うユーザーが呼ぶことは, おそらく非推奨である.
     * </i>
     * </u>
     * </p>
     * 
     * @param value 元の値
     * @return 修正された値
     */
    public static double modified(double value) {
        if (acceptValue(value)) {
            return value;
        }

        //+infの場合
        if (value >= 0) {
            return MAX_VALUE;
        }

        //-infの場合
        if (value <= 0) {
            return MIN_VALUE;
        }

        //NaNの場合
        return 0d;
    }

    /**
     * {@link EntryReadableMatrix} の成分の値についての簡略化された文字列表現を返す.
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

        var entryString = new StringBuilder();

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
     * @return {@link String} 形式に変換された行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static String allEntryToCSVFormat(EntryReadableMatrix matrix) {
        var sp = System.lineSeparator();
        var sb = new StringBuilder();
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
