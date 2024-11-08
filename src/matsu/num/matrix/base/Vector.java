/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.8
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.function.DoubleFunction;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 縦ベクトルを扱う. <br>
 * 成分に不正値を含まないことを保証する. <br>
 * インスタンスはイミュータブルであり, メソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * {@link Vector} は identity に基づく equality を提供する. <br>
 * すなわち, {@link Object#equals(Object)} メソッドの実装に準じる.
 * </p>
 * 
 * <p>
 * 所望のベクトルの生成にはビルダ ({@link Builder}) を用いる. <br>
 * 値の検証にはstaticメソッド {@link #acceptValue(double) } を使用する.
 * </p>
 *
 * @author Matsuura Y.
 * @version 22.3
 */
public final class Vector {

    /**
     * 扱うことができる成分の最小値.
     */
    public static final double MAX_VALUE = Double.MAX_VALUE;

    /**
     * 扱うことができる成分の最大値.
     */
    public static final double MIN_VALUE = -Double.MAX_VALUE;

    private final VectorDimension vectorDimension;
    private final double[] entry;

    private final double normMax;
    private final boolean normalized;

    //遅延初期化
    private volatile Double norm1;
    private volatile Double norm2;
    private volatile Double norm2Square;

    /**
     * ビルダから呼ばれる.
     */
    private Vector(final VectorDimension vectorDimension, final double[] entry) {
        this(vectorDimension, entry, false);
    }

    /**
     * 内部から呼ばれる.
     * 必要なパラメータが渡される.
     */
    private Vector(final VectorDimension vectorDimension, final double[] entry, boolean normalized) {
        this.vectorDimension = vectorDimension;
        this.entry = entry;

        this.normMax = this.calcNormMax();
        this.normalized = normalized;
    }

    /**
     * ベクトルの要素 <i>i</i> の値を返す.
     *
     * @param index <i>i</i>
     * @return 要素 <i>i</i> の値
     * @throws IndexOutOfBoundsException indexが範囲外の場合
     */
    public double valueAt(final int index) {
        if (!this.vectorDimension.isValidIndex(index)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "indexが有効でない:vactor:%s, index=%s", this.vectorDimension, index));
        }
        return this.entry[index];
    }

    /**
     * ベクトルの要素を扱う配列への参照を返す.
     * 
     * <p>
     * このメソッドは内部の配列への参照を公開するため, 決してpublicにしてはいけない. <br>
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
     * @return 同じ次元なら {@code true}
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public boolean equalDimensionTo(Vector other) {
        return this.vectorDimension.equals(other.vectorDimension);
    }

    /**
     * 他のベクトルとの和を計算して返す: <b>w</b> = <b>v</b> + <b>u</b>. <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <b>w</b>: 計算結果.
     *
     * @param reference <b>u</b>, 作用ベクトル
     * @return 計算結果
     * @throws MatrixFormatMismatchException {@code this} と作用ベクトルの次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector plus(final Vector reference) {
        //ここで例外が発生する可能性がある
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.add(result, reference.entry);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 他のベクトルとの差を計算して返す: <b>w</b> = <b>v</b> - <b>u</b>.
     * <br>
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <b>w</b>: 計算結果.
     *
     * @param reference <b>u</b>, 作用ベクトル
     * @return 計算結果
     * @throws MatrixFormatMismatchException {@code this} と作用ベクトルの次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector minus(final Vector reference) {
        //ここで例外が発生する可能性がある
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.subtract(result, reference.entry);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 他のベクトルのスカラー倍との和を計算して返す:
     * <b>w</b> = <b>v</b> + <i>c</i> <b>u</b>. <br>
     * 
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル, <br>
     * <i>c</i>: スカラー.
     * <b>w</b>: 計算結果.
     *
     * @param reference <b>u</b>, 作用ベクトル
     * @param scalar <i>c</i>, スカラー
     * @return 計算結果
     * @throws MatrixFormatMismatchException {@code this} と作用ベクトルの次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector plusCTimes(final Vector reference, final double scalar) {
        //ここで例外が発生する可能性がある
        this.validateDimensionMatch(reference);

        double[] result = this.entry.clone();
        ArraysUtil.addCTimes(result, reference.entry, scalar);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * 自身のスカラー倍を計算して返す:
     * <b>w</b> = <i>c</i> <b>v</b>. <br>
     * 
     * <b>v</b>: {@code this}, <br>
     * <i>c</i>: スカラー.
     * <b>w</b>: 計算結果.
     *
     * @param scalar <i>c</i>, スカラー
     * @return 計算結果
     */
    public Vector times(final double scalar) {
        double[] result = this.entry.clone();
        ArraysUtil.multiply(result, scalar);
        return new Builder(this.vectorDimension, result).build();
    }

    /**
     * ベクトルの内積:
     * <b>u</b> &middot; <b>v</b>. <br>
     * 
     * <b>v</b>: {@code this}, <br>
     * <b>u</b>: 作用ベクトル.
     *
     * @param reference <i>u</i>, 作用ベクトル
     * @return 内積
     * @throws MatrixFormatMismatchException {@code this} と作用ベクトルの次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public double dot(final Vector reference) {
        //ここで例外が発生する可能性がある
        this.validateDimensionMatch(reference);

        return ArraysUtil.dot(this.entry, reference.entry);
    }

    /**
     * 次元の整合性を検証する. <br>
     * 例外をスローすることが責務である.
     * 
     * @param reference 判定対象
     * @throws MatrixFormatMismatchException {@code this} と作用ベクトルの次元が一致しない場合
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
     * 1-ノルム:
     * ||<b>v</b>||<sub>1</sub>.
     *
     * @return 1-ノルム
     */
    public double norm1() {
        Double out = this.norm1;
        if (Objects.nonNull(out)) {
            return out.doubleValue();
        }

        //シングルチェックイディオム
        out = ArraysUtil.norm1(this.entry);
        this.norm1 = out;
        return out.doubleValue();
    }

    /**
     * 2-ノルム (Euclidノルム) の二乗:
     * ||<b>v</b>||<sub>2</sub><sup>2</sup>.
     *
     * @return 2-ノルムの二乗
     */
    public double norm2Square() {
        Double out = this.norm2Square;
        if (Objects.nonNull(out)) {
            return out.doubleValue();
        }

        //シングルチェックイディオム
        out = ArraysUtil.norm2Square(this.entry);
        this.norm2Square = out;
        return out.doubleValue();
    }

    /**
     * 2-ノルム (Euclidノルム):
     * ||<b>v</b>||<sub>2</sub>.
     *
     * @return 2-ノルム
     */
    public double norm2() {
        Double out = this.norm2;
        if (Objects.nonNull(out)) {
            return out.doubleValue();
        }

        //シングルチェックイディオム
        out = ArraysUtil.norm2(this.entry);
        this.norm2 = out;
        return out.doubleValue();
    }

    /**
     * 最大値ノルム:
     * ||<b>v</b>||<sub>&infin;</sub>.
     *
     * @return 最大値ノルム
     */

    public double normMax() {
        return this.normMax;
    }

    /**
     * 最大値ノルムを計算する.
     */
    private double calcNormMax() {
        return ArraysUtil.normMax(this.entry);
    }

    /**
     * Euclidノルムにより自身を規格化したベクトル:
     * <b>v</b> / ||<b>v</b>||<sub>2</sub>. <br>
     * 自身の大きさが厳密に0である場合のみ, 規格化されずに0を返す.
     * 
     * @return 自身を規格化したベクトル
     */
    public Vector normalizedEuclidean() {
        if (this.normalized || this.normMax == 0d) {
            return this;
        }

        Vector out = new Vector(this.vectorDimension, ArraysUtil.normalizeEuclidean(this.entry), true);
        Double value1 = Double.valueOf(1d);
        out.norm2 = value1;
        out.norm2Square = value1;
        return out;
    }

    /**
     * 自身と相手とが等価であるかどうかを判定する. <br>
     * identity に基づく equality である.
     * 
     * @param obj 相手
     * @return 相手が自信と等しいなら true
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * ハッシュコードを返す.
     * 
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        return super.hashCode();
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
                "Vector[dim:%s, {%s}]",
                this.vectorDimension, entryString.toString());
    }

    /**
     * {@link Vector} の成分として有効な値であるかを判定する.
     *
     * @param value 検証する値
     * @return 有効である場合はtrue
     */
    public static boolean acceptValue(double value) {
        return MIN_VALUE <= value && value <= MAX_VALUE;
    }

    /**
     * {@link Vector} のビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
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
                                "サイズ不一致:vector:%s, entry:length=%s", this.vectorDimension, entry.length));
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
         * <p>
         * ベクトルの要素 <i>i</i> を与えられた値で置き換える. <br>
         * ただし, 不正な値を与えた場合, 正常な値に置き換えられる.
         * </p>
         *
         * @param index <i>i</i>, index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException indexが範囲外の場合
         * @see Vector#acceptValue(double)
         */
        public void setValue(final int index, final double value) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.vectorDimension.isValidIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "indexが有効でない:vactor:%s, index=%s", this.vectorDimension, index));
            }

            this.entry[index] = modified(value);
        }

        /**
         * ベクトルの要素 <i>i</i> を与えられた値で置き換える. <br>
         * 値が不正の場合は, 与えたファンクションにより例外を生成してスローする.
         *
         * @param <X> スローされる例外の型
         * @param index <i>i</i>, index
         * @param value 置き換えた後の値
         * @param invalidValueExceptionGetter valueが不正な値の場合にスローする例外の生成器
         * @throws IndexOutOfBoundsException indexが範囲外の場合
         * @throws X valueが不正な値である場合
         * @throws IllegalStateException すでにビルドされている場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Vector#acceptValue(double)
         */
        public <X extends Exception> void setValueOrElseThrow(final int index, double value,
                DoubleFunction<X> invalidValueExceptionGetter) throws X {

            Objects.requireNonNull(invalidValueExceptionGetter);
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!Vector.acceptValue(value)) {
                throw invalidValueExceptionGetter.apply(value);
            }
            if (!this.vectorDimension.isValidIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "indexが有効でない:vactor:%s, index=%s", this.vectorDimension, index));
            }

            this.entry[index] = value;
        }

        private static double modified(double value) {
            if (Vector.acceptValue(value)) {
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
         * ベクトルの要素を与えられた配列の値で置き換える. <br>
         * ただし, 不正な値を与えた場合, 正常な値に置き換えられる. <br>
         * 与える配列の長さはベクトルの次元と一致する必要がある.
         *
         * @param entry 置き換えた後の値を持つ配列
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IllegalArgumentException 配列の長さがベクトルの次元と一致しない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Vector#acceptValue(double)
         */
        public void setEntryValue(final double... entry) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }

            double[] newEntry = entry.clone();
            if (!this.vectorDimension.equalsValueOf(newEntry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%s", this.vectorDimension, newEntry.length));
            }

            for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
                newEntry[j] = modified(newEntry[j]);
            }
            this.entry = newEntry;
        }

        /**
         * ベクトルの要素を与えられた配列の値で置き換える. <br>
         * 値が不正の場合は, 与えたファンクションにより例外を生成してスローする. <br>
         * 与える配列の長さはベクトルの次元と一致する必要がある.
         *
         * @param <X> スローされる例外の型
         * @param entry 置き換えた後の値を持つ配列
         * @param invalidValueExceptionGetter entryの要素が不正な値の場合にスローする例外の生成器
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IllegalArgumentException 配列の長さがベクトルの次元と一致しない場合
         * @throws X valueが不正な値である場合
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Vector#acceptValue(double)
         */
        public <X extends Exception> void setEntryValueOrElseThrow(
                DoubleFunction<X> invalidValueExceptionGetter, final double... entry) throws X {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }

            double[] newEntry = entry.clone();
            if (!this.vectorDimension.equalsValueOf(newEntry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%s", this.vectorDimension, newEntry.length));
            }

            for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
                double value = newEntry[j];
                if (!Vector.acceptValue(value)) {
                    throw invalidValueExceptionGetter.apply(value);
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
