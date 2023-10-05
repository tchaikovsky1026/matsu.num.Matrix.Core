/**
 * 2023.8.17
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

import matsu.num.commons.Exponentiation;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;

/**
 * 縦ベクトルを扱う. 
 * 
 * <p>
 * ベクトルは不変であり, メソッドに対して関数的かつスレッドセーフである. 
 * </p>
 * 
 * <p>
 * 所望のベクトルの生成にはビルダ({@linkplain Builder})を用いる.
 * </p>
 *
 * @author Matsuura Y.
 * @version 15.0
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
        return this.operateBinary(reference, (d1, d2) -> (d1 + d2));
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
        return this.operateBinary(reference, (d1, d2) -> (d1 - d2));
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
        return this.operateBinary(reference, (d1, d2) -> (d1 + d2 * scalar));
    }

    /**
     * @throws IllegalArgumentException 計算結果が不正な場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private Vector operateBinary(final Vector reference, DoubleBinaryOperator operator) {
        if (!(this.equalDimensionTo(reference))) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "次元不一致:vector:%s, reference:%s",
                            this.vectorDimension, reference.vectorDimension));
        }

        final double[] thisEntry = this.entry;
        final double[] refEntry = reference.entry;
        final double[] resultEntry = new double[this.vectorDimension.intValue()];
        for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
            resultEntry[j] = operator.applyAsDouble(thisEntry[j], refEntry[j]);
        }
        return new Builder(vectorDimension).setEntryValue(resultEntry).build();
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

        final double[] thisEntry = this.entry;
        final double[] resultEntry = new double[this.vectorDimension.intValue()];
        for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
            resultEntry[j] = thisEntry[j] * scalar;
        }
        return new Builder(vectorDimension).setEntryValue(resultEntry).build();
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
        if (!(this.equalDimensionTo(reference))) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "次元不一致:vector:%s, reference:%s",
                            this.vectorDimension, reference.vectorDimension));
        }

        //オーバー,アンダーフロー対策でスケールする
        double thisNormMax = this.normMax();
        double refNormMax = reference.normMax();
        if (thisNormMax == 0d || refNormMax == 0d) {
            //0の場合
            return 0d;
        }
        double invThisNormMax = 1d / thisNormMax;
        double invRefNormMax = 1d / refNormMax;
        if (!(Double.isFinite(invThisNormMax) && invThisNormMax > 0d
                && Double.isFinite(invRefNormMax) && invRefNormMax > 0d)) {
            //逆数が特殊値の場合は別処理
            return this.dotAbnormal(reference);
        }

        final double[] thisEntry = this.entry;
        final double[] refEntry = reference.entry;

        final int thisDimension = vectorDimension.intValue();
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = (thisEntry[j] * invThisNormMax) * (refEntry[j] * invRefNormMax);
            v1 = (thisEntry[j + 1] * invThisNormMax) * (refEntry[j + 1] * invRefNormMax);
            v2 = (thisEntry[j + 2] * invThisNormMax) * (refEntry[j + 2] * invRefNormMax);
            v3 = (thisEntry[j + 3] * invThisNormMax) * (refEntry[j + 3] * invRefNormMax);
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = (thisEntry[j] * invThisNormMax) * (refEntry[j] * invRefNormMax);
            outputValue += v0;
        }

        if (outputValue == 0d) {
            return 0d;
        }
        return outputValue * (thisNormMax * refNormMax);
    }

    private double dotAbnormal(final Vector reference) {
        if (!(this.equalDimensionTo(reference))) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "次元不一致:vector:%s, reference:%s",
                            this.vectorDimension, reference.vectorDimension));
        }

        double thisNormMax = this.normMax();
        double refNormMax = reference.normMax();

        final double[] thisEntry = this.entry;
        final double[] refEntry = reference.entry;

        final int thisDimension = vectorDimension.intValue();
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = (thisEntry[j] / thisNormMax) * (refEntry[j] / refNormMax);
            v1 = (thisEntry[j + 1] / thisNormMax) * (refEntry[j + 1] / refNormMax);
            v2 = (thisEntry[j + 2] / thisNormMax) * (refEntry[j + 2] / refNormMax);
            v3 = (thisEntry[j + 3] / thisNormMax) * (refEntry[j + 3] / refNormMax);
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = (thisEntry[j] / thisNormMax) * (refEntry[j] / refNormMax);
            outputValue += v0;
        }

        if (outputValue == 0d) {
            return 0d;
        }
        return outputValue * (thisNormMax * refNormMax);
    }

    /**
     * 1-ノルムを計算する: ||<b>v</b>||<sub>1</sub>. <br>
     *
     * @return 1-ノルム
     */
    public double norm1() {
        final int thisDimension = vectorDimension.intValue();
        final double[] thisEntry = this.entry;
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = Math.abs(thisEntry[j]);
            v1 = Math.abs(thisEntry[j + 1]);
            v2 = Math.abs(thisEntry[j + 2]);
            v3 = Math.abs(thisEntry[j + 3]);
            outputValue += (v0 + v1) + (v2 + v3);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = Math.abs(thisEntry[j]);
            outputValue += v0;
        }
        return outputValue;
    }

    /**
     * 2-ノルム(Euclidノルム)の二乗を計算する: ||<b>v</b>||<sub>2</sub><sup>2</sup>. <br>
     *
     * @return 2-ノルムの二乗
     */
    public double norm2Square() {
        final int thisDimension = vectorDimension.intValue();
        final double[] thisEntry = this.entry;
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = thisEntry[j];
            v1 = thisEntry[j + 1];
            v2 = thisEntry[j + 2];
            v3 = thisEntry[j + 3];
            final double v0Square = v0 * v0;
            final double v1Square = v1 * v1;
            final double v2Square = v2 * v2;
            final double v3Square = v3 * v3;
            outputValue += (v0Square + v1Square) + (v2Square + v3Square);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = thisEntry[j];
            final double v0Square = v0 * v0;
            outputValue += v0Square;
        }
        return outputValue;
    }

    /**
     * 2-ノルム(Euclidノルム)を計算する: ||<b>v</b>||<sub>2</sub>. <br>
     *
     * @return 2-ノルム
     */
    public double norm2() {
        //オーバー,アンダーフロー対策でスケールする
        double normMax = this.normMax();
        if (normMax == 0d) {
            //0の場合は0
            return 0d;
        }
        double invNormMax = 1d / normMax;
        if (!(Double.isFinite(invNormMax) && invNormMax > 0d)) {
            //逆数が特殊値の場合は別処理
            return this.norm2Abnormal();
        }

        final int thisDimension = vectorDimension.intValue();
        final double[] thisEntry = this.entry;
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = thisEntry[j] * invNormMax;
            v1 = thisEntry[j + 1] * invNormMax;
            v2 = thisEntry[j + 2] * invNormMax;
            v3 = thisEntry[j + 3] * invNormMax;
            final double v0Square = v0 * v0;
            final double v1Square = v1 * v1;
            final double v2Square = v2 * v2;
            final double v3Square = v3 * v3;
            outputValue += (v0Square + v1Square) + (v2Square + v3Square);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = thisEntry[j] * invNormMax;
            final double v0Square = v0 * v0;
            outputValue += v0Square;
        }
        return normMax * Exponentiation.sqrt(outputValue);
    }

    private double norm2Abnormal() {
        double normMax = this.normMax();

        final int thisDimension = vectorDimension.intValue();
        final double[] thisEntry = this.entry;
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = thisEntry[j] / normMax;
            v1 = thisEntry[j + 1] / normMax;
            v2 = thisEntry[j + 2] / normMax;
            v3 = thisEntry[j + 3] / normMax;
            final double v0Square = v0 * v0;
            final double v1Square = v1 * v1;
            final double v2Square = v2 * v2;
            final double v3Square = v3 * v3;
            outputValue += (v0Square + v1Square) + (v2Square + v3Square);
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = thisEntry[j] / normMax;
            final double v0Square = v0 * v0;
            outputValue += v0Square;
        }
        return normMax * Exponentiation.sqrt(outputValue);
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
        final int thisDimension = vectorDimension.intValue();
        final double[] thisEntry = this.entry;
        double outputValue = 0.0;
        int j;
        for (j = 0; j < thisDimension - 3; j += 4) {
            final double v0, v1, v2, v3;
            v0 = Math.abs(thisEntry[j]);
            v1 = Math.abs(thisEntry[j + 1]);
            v2 = Math.abs(thisEntry[j + 2]);
            v3 = Math.abs(thisEntry[j + 3]);
            final double v01 = Math.max(v0, v1);
            final double v23 = Math.max(v2, v3);
            outputValue = Math.max(outputValue, Math.max(v01, v23));
        }
        for (; j < thisDimension; j++) {
            final double v0;
            v0 = Math.abs(thisEntry[j]);
            outputValue = Math.max(outputValue, v0);
        }
        return outputValue;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code @hashCode[dimension: %dimension, entry: [*,*,*, ...]]}
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
                "@%s[dimension:%s, entry:[%s]]",
                Integer.toHexString(this.hashCode()),
                this.vectorDimension, entryString.toString());
    }

    /**
     * {@link Vector}のビルダ. 
     * 
     * <p>
     * このビルダ自体はスレッドセーフでない.
     * </p>
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
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException indexが範囲外の場合
         * @throws IllegalArgumentException valueが不正な値(Infinity, NaN)である場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public Builder setValue(final int index, final double value) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.vectorDimension.isValidIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "indexが有効でない:vactor:%s, index=%d", this.vectorDimension, index));
            }
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(String.format("valueが不正な値=%.16G", value));
            }
            this.entry[index] = value;
            return this;
        }

        /**
         * ベクトルの要素を与えられた配列の値で置き換える. <br>
         * 与える配列の長さはベクトルの次元と一致する必要がある.
         *
         * @param entry 置き換えた後の値を持つ配列
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IllegalArgumentException 配列の長さがベクトルの次元と一致しない場合,
         * 不正な値(Infinity, NaN)を含む場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public Builder setEntryValue(final double... entry) {
            if (Objects.isNull(this.entry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!this.vectorDimension.equalsValueOf(entry.length)) {
                throw new IllegalArgumentException(
                        String.format(
                                "サイズ不一致:vector:%s, entry:length=%d", this.vectorDimension, entry.length));
            }
            for (int j = 0, len = vectorDimension.intValue(); j < len; j++) {
                if (!Double.isFinite(entry[j])) {
                    throw new IllegalArgumentException("不正な値を含んでいます");
                }
            }

            System.arraycopy(entry, 0, this.entry, 0, entry.length);
            return this;
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
