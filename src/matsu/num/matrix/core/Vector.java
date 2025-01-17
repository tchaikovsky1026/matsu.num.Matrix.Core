/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.3
 */
package matsu.num.matrix.core;

import java.util.Objects;
import java.util.function.DoubleFunction;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 縦ベクトルを扱う. <br>
 * 成分に不正値を含まないことを保証する. <br>
 * インスタンスはイミュータブルであり, メソッドは関数的かつスレッドセーフである.
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
    private Vector(final Builder builder) {
        this(builder.vectorDimension, builder.entry, false);
    }

    /**
     * 内部から呼ばれる.
     * 必要なパラメータが渡される.
     * 
     * <p>
     * entry配列はコピーされないので, 参照が漏洩していないものを渡さなければならない.
     * </p>
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
        Builder.modify(result);
        return new Vector(this.vectorDimension, result, false);
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
        Builder.modify(result);
        return new Vector(this.vectorDimension, result, false);
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
        Builder.modify(result);
        return new Vector(this.vectorDimension, result, false);
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
        Builder.modify(result);
        return new Vector(this.vectorDimension, result, false);
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

        var out = new Vector(this.vectorDimension, ArraysUtil.normalizeEuclidean(this.entry), true);
        Double value1 = Double.valueOf(1d);
        out.norm2 = value1;
        out.norm2Square = value1;
        return out;
    }

    /**
     * 自身の加法逆元 (-1倍) を返す.
     * 
     * @return 加法逆元
     */
    public Vector negated() {
        var out = new Vector(this.vectorDimension, ArraysUtil.negated(this.entry), this.normalized);
        out.norm1 = this.norm1;
        out.norm2 = this.norm2;
        out.norm2Square = this.norm2Square;
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

        var entryString = new StringBuilder();
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
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #zeroBuilder(VectorDimension)} をコールする.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link Vector} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     */
    public static final class Builder {

        private final VectorDimension vectorDimension;
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
         * コピーコンストラクタ.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final Builder src) {
            this.vectorDimension = src.vectorDimension;
            this.entry = src.entry.clone();
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

        private static void modify(final double[] values) {
            for (int i = 0, len = values.length; i < len; i++) {
                values[i] = modified(values[i]);
            }
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
            this.throwISExIfCannotBeUsed();

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
         * @throws NullPointerException 例外生成器がnullかつ例外を生成しようとした場合
         * @see Vector#acceptValue(double)
         */
        public <X extends Exception> void setValueOrElseThrow(final int index, double value,
                DoubleFunction<X> invalidValueExceptionGetter) throws X {

            this.throwISExIfCannotBeUsed();

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
            this.throwISExIfCannotBeUsed();

            double[] newEntry = entry.clone();
            if (!this.vectorDimension.equalsValueOf(newEntry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%s", this.vectorDimension, newEntry.length));
            }

            modify(newEntry);

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
         * @throws NullPointerException 例外生成器がnullかつ例外を生成しようとした場合, 配列がnullの場合
         * @see Vector#acceptValue(double)
         */
        public <X extends Exception> void setEntryValueOrElseThrow(
                DoubleFunction<X> invalidValueExceptionGetter, final double... entry) throws X {
            this.throwISExIfCannotBeUsed();

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
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.entry);
        }

        /**
         * ビルド前かを判定し, ビルド後なら例外をスロー.
         */
        private void throwISExIfCannotBeUsed() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
            }
        }

        /**
         * このビルダのコピーを生成して返す.
         * 
         * @return このビルダのコピー
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Builder copy() {
            this.throwISExIfCannotBeUsed();

            return new Builder(this);
        }

        /**
         * ビルダを使用不能にする.
         */
        private void disable() {
            this.entry = null;
        }

        /**
         * ベクトルをビルドする.
         *
         * @return ベクトル
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Vector build() {
            this.throwISExIfCannotBeUsed();

            Vector out = new Vector(this);
            this.disable();

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
