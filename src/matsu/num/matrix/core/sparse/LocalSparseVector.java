/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core.sparse;

import java.util.Objects;

import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * <p>
 * 局所的に値を持つスパースなベクトルを表現する具象クラス.
 * </p>
 *
 * <p>
 * ベクトルの成分を
 * {<i>v</i><sub>0</sub>, <i>v</i><sub>1</sub>, ..., <i>v</i><sub><i>n</i> -
 * 1</sub>}
 * と表したとき,
 * <i>v</i><sub><i>p</i></sub>, <i>v</i><sub><i>p</i> + 1</sub>, ...,
 * <i>v</i><sub><i>p</i> + &ell; - 1</sub>
 * を除いて0であるようなベクトルを表現する
 * (<i>n</i> は次元). <br>
 * &ell; &ge; 1 である.
 * </p>
 * 
 * <p>
 * この具象クラスはコンストラクタが公開されていない. <br>
 * インスタンスは, {@link #of(VectorDimension, int, double[])} メソッドにより取得できる.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class LocalSparseVector implements SparseVector {

    private final VectorDimension vectorDimension;
    private final int pos;
    private final double[] entry;

    private final double normMax;
    private final boolean normalized;

    private volatile Double norm2;

    private volatile Vector viewAsVector;

    /**
     * 内部から呼ばれる.
     * 
     * <p>
     * 引数により, 開始位置{@code p}と長さベクトルの要素{@code e[]}を指定する({@code e.length = l}とする).
     * <br>
     * これはベクトルの成分に {@code [0,...,0,e[0],e[1],...,e[l-1],0,...,0]} を指定したことを意味する.
     * <br>
     * ただし, {@code e[0]}の位置indexは{@code p}である.
     * </p>
     * 
     * <p>
     * コンストラクタ内で引数チェックを行わないので, 事前チェックが必要.
     * </p>
     * 
     * <p>
     * entryには正当な値が入ったものを渡さなければならない. <br>
     * entry配列はコピーされないので, 参照が漏洩していないものを渡さなければならない.
     * </p>
     */
    private LocalSparseVector(final VectorDimension vectorDimension, int pos, double[] entry) {
        this(vectorDimension, pos, entry, false);
    }

    /**
     * 内部から呼ばれる.
     * 
     * <p>
     * コンストラクタ内で引数チェックを行わないので, 事前チェックが必要.
     * </p>
     * 
     * <p>
     * entryには正当なものを渡さなければならない.
     * entry配列はコピーされないので, 参照が漏洩していないものを渡さなければならない.
     * </p>
     */
    private LocalSparseVector(final VectorDimension vectorDimension, int pos, double[] entry, boolean normalized) {

        this.vectorDimension = vectorDimension;
        this.pos = pos;
        this.entry = entry;

        this.normMax = ArraysUtil.normMax(this.entry);
        this.normalized = normalized;
    }

    @Override
    public VectorDimension vectorDimension() {
        return this.vectorDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(int index) {
        if (!(0 <= index && index < this.vectorDimension.intValue())) {
            throw new IndexOutOfBoundsException("indexが範囲外");
        }
        int relative = index - this.pos;
        if (0 <= relative && relative < this.entry.length) {
            return this.entry[relative];
        }
        return 0;
    }

    @Override
    public double norm2() {
        Double out = this.norm2;
        if (Objects.nonNull(out)) {
            return out.doubleValue();
        }
        //シングルチェックイディオム
        out = Double.valueOf(ArraysUtil.norm2(this.entry, this.normMax));
        this.norm2 = out;
        return out.doubleValue();
    }

    @Override
    public double normMax() {
        return this.normMax;
    }

    @Override
    public double dot(Vector reference) {
        if (!this.vectorDimension.equals(reference.vectorDimension())) {
            throw new MatrixFormatMismatchException("次元が整合しない");
        }

        double dot = 0;
        for (int i = 0; i < this.entry.length; i++) {
            dot += this.entry[i] * reference.valueAt(this.pos + i);
        }
        return dot;
    }

    @Override
    public SparseVector times(double scalar) {
        double[] outEntry = this.entry.clone();
        for (int i = 0, len_i = outEntry.length; i < len_i; i++) {
            outEntry[i] *= scalar;
        }

        //値を正当なものに修正する
        canonicalize(outEntry);

        return new LocalSparseVector(this.vectorDimension, this.pos, outEntry);
    }

    @Override
    public SparseVector normalizedEuclidean() {
        if (this.normalized || this.normMax == 0d) {
            return this;
        }

        double[] normalizedEntry = this.entry.clone();
        ArraysUtil.normalizeEuclidean(normalizedEntry, this.normMax);

        LocalSparseVector out = new LocalSparseVector(vectorDimension, pos, normalizedEntry, true);
        Double value1 = Double.valueOf(1d);
        out.norm2 = value1;
        return out;
    }

    @Override
    public Vector asVector() {
        Vector out = this.viewAsVector;
        if (Objects.nonNull(out)) {
            return out;
        }

        //シングルチェックイディオム
        out = this.computeVector();
        this.viewAsVector = out;
        return out;
    }

    /**
     * 自身と同等の {@link Vector} を生成する.
     * 
     * @return 自身と同等の {@link Vector}
     */
    private Vector computeVector() {
        int dimension = this.vectorDimension.intValue();

        double[] fullEntry = new double[dimension];
        System.arraycopy(this.entry, 0, fullEntry, this.pos, this.entry.length);

        var builder = Vector.Builder.zeroBuilder(this.vectorDimension);
        builder.setEntryValue(fullEntry);

        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector plus(Vector reference) {
        if (!this.vectorDimension.equals(reference.vectorDimension())) {
            throw new MatrixFormatMismatchException("次元が整合しない");
        }

        double[] result = reference.entryAsArray();
        for (int i = 0; i < this.entry.length; i++) {
            result[this.pos + i] += this.entry[i];
        }

        Vector.Builder builder = Vector.Builder.zeroBuilder(this.vectorDimension);
        builder.setEntryValue(result);
        return builder.build();
    }

    @Override
    public SparseVector negated() {
        double[] outEntry = this.entry.clone();
        for (int i = 0, len_i = outEntry.length; i < len_i; i++) {
            outEntry[i] = -outEntry[i];
        }

        LocalSparseVector out = new LocalSparseVector(
                this.vectorDimension, this.pos, outEntry, this.normalized);
        out.norm2 = this.norm2;

        return out;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code SparseVector[dim(%dimension), {*,*,*, ...}]}
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
            entryString.append(this.valueAt(i));
            if (i < displaySize - 1) {
                entryString.append(", ");
            }
        }
        if (thisDimension > displaySize) {
            entryString.append(", ...");
        }

        return String.format(
                "SparseVector[dim:%s, {%s}]",
                this.vectorDimension, entryString.toString());
    }

    /**
     * <p>
     * このクラスのインスタンスを得るための {@code static} ファクトリ.
     * </p>
     * 
     * <p>
     * (構造はクラス説明を参照のこと.) <br>
     * 引数により, 開始位置 <i>p</i> と, 局所要素
     * {<i>v</i><sub><i>p</i></sub>, <i>v</i><sub><i>p</i> + 1</sub>, ...,
     * <i>v</i><sub><i>p</i> + &ell; - 1</sub>}
     * を指定する. <br>
     * (&ell; は局所要素のサイズである.) <br>
     * <i>p</i> &ge; 0, &ell; &ge; 1, かつ
     * <i>p</i> + &ell; &le; <i>n</i>
     * でなければならない
     * (<i>n</i> は次元).
     * </p>
     * 
     * <p>
     * 与えた成分が {@link SparseVector} で扱えない場合は,
     * 正常値に置き換えられる.
     * </p>
     * 
     * @param vectorDimension ベクトルの次元 (<i>n</i> に相当)
     * @param pos 開始位置 <i>p</i>
     * @param entry 局所要素 {<i>v</i><sub><i>p</i></sub>, <i>v</i><sub><i>p</i> +
     *            1</sub>, ...,
     *            <i>v</i><sub><i>p</i> + &ell; - 1</sub>}
     * @return 局所的に値を持つスパースなベクトル
     * @throws IllegalArgumentException 開始位置と局所要素のサイズが契約を満たさない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static LocalSparseVector of(
            final VectorDimension vectorDimension, int pos, double[] entry) {
        if (entry.length == 0) {
            throw new IllegalArgumentException("entryのサイズが0");
        }
        if (pos < 0 || entry.length == 0 || pos + entry.length > vectorDimension.intValue()) {
            throw new IllegalArgumentException(
                    String.format(
                            "entryが範囲外: 次元: %s, 局所位置: [%s,%s)",
                            vectorDimension, pos, pos + entry.length));
        }

        double[] copyOfEntry = entry.clone();
        canonicalize(copyOfEntry);

        return new LocalSparseVector(vectorDimension, pos, copyOfEntry);
    }

    /**
     * 配列の中身を正当な値に書き換える.
     */
    private static void canonicalize(double[] entry) {
        for (int i = 0, len = entry.length; i < len; i++) {
            entry[i] = canonicalizedValue(entry[i]);
        }
    }

    /**
     * 与えた値を正当なものに修正して返す.
     * 
     * @see Vector#acceptValue(double)
     */
    private static double canonicalizedValue(double value) {
        if (Vector.acceptValue(value)) {
            return value;
        }

        //+infの場合
        if (value >= 0) {
            return Vector.MAX_VALUE;
        }

        //-infの場合
        if (value <= 0) {
            return Vector.MIN_VALUE;
        }

        //NaNの場合
        return 0d;
    }
}
