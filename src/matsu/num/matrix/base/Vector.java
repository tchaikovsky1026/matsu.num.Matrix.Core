/**
 * 2023.12.4
 */
package matsu.num.matrix.base;

import java.util.Objects;

import matsu.num.commons.ArraysUtil;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * 縦ベクトルを扱う. <br>
 * 成分に不正値(inf,NaN)を含んではいけない.
 * 
 * <p>
 * ベクトルは不変であり, メソッドに対して関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * 所望のベクトルの生成にはビルダ({@linkplain Builder})を用いる. <br>
 * 値の検証には{@link #acceptValue(double) }メソッドを使用する.
 * </p>
 *
 * @author Matsuura Y.
 * @version 17.2
 */
public final class Vector {

    private final VectorDimension vectorDimension;
    private final double[] entry;

    private final double normMax;

    private Vector(final VectorDimension vectorDimension, final double[] entry) {
        this.vectorDimension = vectorDimension;
        this.entry = entry;

        this.normMax = this.calcNormMax();
    }

    /**
     * ベクトルの要素<i>i</i>の値を返す.
     *
     * @param index i, index
     * @return 要素iの値
     * @throws IndexOutOfBoundsException indexが範囲外の場合
     */
    public double valueAt(final int index) {
        if (!this.vectorDimension.isValidIndex(index)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "indexが有効でない:vactor:%s, index=%d", this.vectorDimension, index));
        }
        return this.entry[index];
    }

    /**
     * ベクトルの要素を扱う配列への参照を返す.
     * 
     * <p>
     * このメソッドは内部の配列への参照を公開するので危険であり, 決してpublicにしてはいけない. <br>
     * また, 使い方には十分注意する.
     * </p>
     *
     * @return ベクトルの要素配列への参照
     */
    double[] entry() {
        return entry;
    }

    /**
     * ベクトルの成分のビューを配列の形で返す.
     *
     * @return ベクトルの要素のビュー
     */
    public double[] entryAsArray() {
        return entry.clone();
    }

    /**
     * ベクトルの次元を取得する.
     *
     * @return ベクトルの次元
     */
    public VectorDimension vectorDimension() {
        return vectorDimension;
    }

    /**
     * 与えられたベクトルが自身と同じ次元かどうかを判定する.
     *
     * @param other 比較対象
     * @return 同じ次元なら, true
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public boolean equalDimensionTo(Vector other) {
        return this.vectorDimension.equals(other.vectorDimension);
    }

    /**
     * 他のベクトルとの和を計算する: <b>w</b> = <b>v</b> + <b>u</b>. <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <b>w</b>: 計算結果.
     *
     * @param reference u, 作用ベクトル
     * @return 計算結果
     * @throws MatrixFormatMismatchException thisと作用ベクトルの次元が一致しない場合
     * @throws IllegalArgumentException 計算結果が不正な場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector plus(final Vector reference) {
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.add(result, reference.entry);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 他のベクトルとの差を計算する: <b>w</b> = <b>v</b> - <b>u</b>.
     * <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <b>w</b>: 計算結果.
     *
     * @param reference u, 作用ベクトル
     * @return 計算結果
     * @throws MatrixFormatMismatchException thisと作用ベクトルの次元が一致しない場合
     * @throws IllegalArgumentException 計算結果が不正な場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector minus(final Vector reference) {
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.subtract(result, reference.entry);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 他のベクトルのスカラー倍との和を計算する: <b>w</b> = <b>v</b> +
     * <i>c</i><b>u</b>.
     * <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <i>c</i>: スカラー.
     * <b>w</b>: 計算結果.
     *
     * @param reference u, 作用ベクトル
     * @param scalar c, スカラー
     * @return 計算結果
     * @throws MatrixFormatMismatchException thisと作用ベクトルの次元が一致しない場合
     * @throws IllegalArgumentException 計算結果が不正な場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector plusCTimes(final Vector reference, final double scalar) {
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.addCTimes(result, reference.entry, scalar);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 自身のスカラー倍を計算する: <b>w</b> = <i>c</i><b>v</b>.
     * <br>
     * <b>v</b>: {@code this}, <br>
     * <i>c</i>: スカラー.
     * <b>w</b>: 計算結果.
     *
     * @param scalar c, スカラー
     * @return 計算結果
     * @throws IllegalArgumentException 計算結果が不正な場合
     */
    public Vector times(final double scalar) {
        double[] result = this.entry.clone();
        ArraysUtil.multiply(result, scalar);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 内積を計算する: <b>u</b>&middot;<b>v</b>.
     * <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル.
     *
     * @param reference u, 作用ベクトル
     * @return 内積
     * @throws MatrixFormatMismatchException {@code this}と作用ベクトルの次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public double dot(final Vector reference) {
        this.validateDimensionMatch(reference);

        return ArraysUtil.dot(this.entry, reference.entry);
    }

    /**
     * 次元の整合性を検証する. <br>
     * 例外をスローすることが責務である.
     * 
     * @param reference 判定対象
     * @throws MatrixFormatMismatchException {@code this}と作用ベクトルの次元が一致しない場合
     */
    private void validateDimensionMatch(Vector reference) {
        if (!(this.equalDimensionTo(reference))) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "次元不一致:this:%s, reference:%s",
                            this.vectorDimension, reference.vectorDimension));
        }
    }

    /**
     * 1-ノルムを計算する: ||<b>v</b>||<sub>1</sub>. <br>
     *
     * @return 1-ノルム
     */
    public double norm1() {
        return ArraysUtil.norm1(this.entry);
    }

    /**
     * 2-ノルム(Euclidノルム)の二乗を計算する: ||<b>v</b>||<sub>2</sub><sup>2</sup>. <br>
     *
     * @return 2-ノルムの二乗
     */
    public double norm2Square() {
        return ArraysUtil.norm2Square(this.entry);
    }

    /**
     * 2-ノルム(Euclidノルム)を計算する: ||<b>v</b>||<sub>2</sub>. <br>
     *
     * @return 2-ノルム
     */
    public double norm2() {
        return ArraysUtil.norm2(this.entry);
    }

    /**
     * 最大値ノルムを計算する: ||<b>v</b>||<sub>&infin;</sub>. <br>
     *
     * @return 最大値ノルム
     */

    public double normMax() {
        return this.normMax;
    }

    private double calcNormMax() {
        return ArraysUtil.normMax(this.entry);
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Vector[dim(%dimension), {*,*,*, ...}]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        final int maxDisplaySize = 3;

        StringBuilder entryString = new StringBuilder();
        final int thisDimension = this.vectorDimension.intValue();
        final int displaySize = Math.min(maxDisplaySize, thisDimension);
        for (int i = 0; i < displaySize; i++) {
            entryString.append(this.entry[i]);
            if (i < displaySize - 1) {
                entryString.append(", ");
            }
        }
        if (thisDimension > displaySize) {
            entryString.append(", ...");
        }

        return String.format(
                "Vector[dim(%s), {%s}]",
                this.vectorDimension, entryString.toString());
    }

    /**
     * {@link Vector}の成分として有効な値であるかを判定する.
     *
     * @param value 検証する値
     * @return 有効である場合はtrue
     */
    public static boolean acceptValue(double value) {
        return Double.isFinite(value);
    }

    /**
     * {@link Vector}のビルダ.
     * 
     * <p>
     * このビルダはミュータブルである. <br>
     * また, スレッドセーフでない.
     * </p>
     * 
     */
    public static final class Builder {

        private VectorDimension vectorDimension;
        private double[] entry;

        /**
         * 与えられた次元のビルダを生成する. <br>
         * 成分は全て0である.
         *
         * @param vectorDimension 生成するベクトルの次元
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final VectorDimension vectorDimension) {
            this.vectorDimension = Objects.requireNonNull(vectorDimension);
            this.entry = new double[vectorDimension.intValue()];
        }

        /**
         * 与えられた次元と成分を持つのビルダを生成する.
         * 
         * <p>
         * 配列は防御的コピーがされていない. <br>
         * このコンストラクタは公開してはいけない.
         * </p>
         *
         * @param vectorDimension 生成するベクトルの次元
         * @param entry 成分
         * @throws IllegalArgumentException 次元が整合しない場合, 不正な値を含む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final VectorDimension vectorDimension, double[] entry) {
            this.vectorDimension = vectorDimension;
            this.entry = entry;

            if (!this.vectorDimension.equalsValueOf(this.entry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%d", this.vectorDimension, entry.length));
            }
            for (int j = 0, len = this.vectorDimension.intValue(); j < len; j++) {
                if (!Vector.acceptValue(this.entry[j])) {
                    throw new IllegalArgumentException("不正な値を含んでいます");
                }
            }
        }

        /**
         * 与えられたソースからビルダを生成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final Vector src) {
            this.vectorDimension = src.vectorDimension;
            this.entry = src.entry.clone();
        }

        /**
         * ベクトルの要素<i>i</i>を与えられた値で置き換える.
         *
         * @param index i, index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException indexが範囲外の場合
         * @throws IllegalArgumentException valueが不正な値(Infinity, NaN)である場合
         */
        public void setValue(final int index, final double value) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.vectorDimension.isValidIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "indexが有効でない:vactor:%s, index=%d", this.vectorDimension, index));
            }
            if (!Vector.acceptValue(value)) {
                throw new IllegalArgumentException(String.format("valueが不正な値=%.16G", value));
            }
            this.entry[index] = value;
        }

        /**
         * ベクトルの要素を与えられた配列の値で置き換える. <br>
         * 与える配列の長さはベクトルの次元と一致する必要がある.
         *
         * @param entry 置き換えた後の値を持つ配列
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IllegalArgumentException 配列の長さがベクトルの次元と一致しない場合,
         *             不正な値(Infinity, NaN)を含む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public void setEntryValue(final double... entry) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.vectorDimension.equalsValueOf(entry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%d", this.vectorDimension, entry.length));
            }
            double[] newEntry = entry.clone();
            for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
                if (!Vector.acceptValue(newEntry[j])) {
                    throw new IllegalArgumentException("不正な値を含んでいます");
                }
            }
            this.entry = newEntry;
        }

        /**
         * ベクトルをビルドする.
         *
         * @return ベクトル
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Vector build() {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            Vector out = new Vector(this.vectorDimension, this.entry);
            this.entry = null;
            return out;
        }

        /**
         * 与えられた次元を持つ, 零ベクトルで初期化されたビルダを生成する.
         *
         * @param vectorDimension 生成するベクトルの次元
         * @return 零ベクトルで初期化されたビルダ
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zeroBuilder(final VectorDimension vectorDimension) {
            return new Builder(vectorDimension);
        }

        /**
         * 与えられたソースからビルダを生成する.
         *
         * @param src ソース
         * @return 元ベクトルと等価なビルダ
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder from(final Vector src) {
            return new Builder(src);
        }

    }
}
