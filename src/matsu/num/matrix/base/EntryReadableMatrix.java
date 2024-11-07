/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.7
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.helper.matrix.transpose.TranspositionEntryReadable;

/**
 * <p>
 * 成分に <i>O</i>(1) でアクセス可能な行列を表す. <br>
 * 成分に不正値 (inf, NaN) を含まない
 * (扱うことができる値は {@link #MIN_VALUE}, {@link #MAX_VALUE} で規定される).
 * </p>
 * 
 * <p>
 * インスタンスの構築などで成分として適当かどうかを判断するには {@link #acceptValue(double)} メソッドを使用する. <br>
 * もし値の修正を行うならば {@link #modified(double)} メソッドを使用する.
 * </p>
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * {@link Matrix} の規約に従う.
 * </p>
 *
 * @author Matsuura Y.
 * @version 22.2
 */
public interface EntryReadableMatrix extends Matrix {

    /**
     * 扱うことができる成分の最小値.
     */
    public static final double MAX_VALUE = Double.MAX_VALUE;

    /**
     * 扱うことができる成分の最大値.
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

    @Override
    public abstract EntryReadableMatrix transpose();

    /**
     * {@link EntryReadableMatrix} の成分として有効な値であるかを判定する.
     *
     * @param value 検証する値
     * @return 有効である場合はtrue
     */
    public static boolean acceptValue(double value) {
        return MIN_VALUE <= value && value <= MAX_VALUE;
    }

    /**
     * 与えられた値を成分として使用できるように修正する. <br>
     * 正常値を与えた場合はそのまま, 不正な値を与えた場合は正常な値に修正して返す.
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
     * @return {@link String} 形式に変換された行列
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

    /**
     * 与えられた行列の転置行列を生成する.
     * 
     * <p>
     * 引数 {@code original}, 戻り値 {@code returnValue} について,
     * {@code returnValue.transpose() == original} が {@code true} である.
     * <br>
     * {@code original} に {@link Symmetric} が付与されている場合,
     * {@code returnValue == original} が {@code true} である.
     * </p>
     * 
     * <p>
     * <i>
     * <u>このメソッドの利用について</u> <br>
     * {@link EntryReadableMatrix} およびそのサブタイプから転置行列を得るには,
     * {@link #transpose()} を呼ぶことが推奨される. <br>
     * このメソッドは {@link #transpose()} や
     * {@link SkeletalAsymmetricMatrix#createTranspose()}
     * の戻り値の生成を補助するために用意されている. <br>
     * (ただし, {@link #transpose()}
     * の複数回の呼び出しで同一のインスタンスを返すようにキャッシュすることが推奨される.)
     * </i>
     * </p>
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static EntryReadableMatrix createTransposedOf(EntryReadableMatrix original) {
        return TranspositionEntryReadable.instance().apply(original);
    }
}
