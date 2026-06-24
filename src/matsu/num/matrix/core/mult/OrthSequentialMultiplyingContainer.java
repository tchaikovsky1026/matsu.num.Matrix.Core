/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.6.9
 */
package matsu.num.matrix.core.mult;

import java.util.Objects;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 直交行列に特化した, 逐次的に行列積の計算を実行する機能を扱う.
 * 
 * <p>
 * このクラスはミュータブルなクラスである. <br>
 * 最初にベースとなる, 成分アクセス可能な直交行列を与え, コンテナを用意する
 * ({@link #basedOn} メソッド). <br>
 * その後, 左右から直交行列を乗算するメソッド
 * {@link #operateLeftSide(OrthogonalMatrix)},
 * {@link #operateRightSide(OrthogonalMatrix)}
 * をコールし, 状態を更新していく. <br>
 * 状態更新が完了したら, ビルドメソッド
 * {@link #build()} をコールし, 直交行列を構築する. <br>
 * 構築された行列は, {@link ProductOrthogonalMatrix} 型となる.
 * </p>
 * 
 * <p>
 * ビルダはビルド後でも再利用可能である.
 * </p>
 * 
 * <p>
 * 状態更新する行列は, 直交行列であり,
 * 状態を更新しても行列のサイズ (次元) が不変であることが担保される.
 * </p>
 * 
 * <p>
 * このクラスはスレッドセーフでない. <br>
 * ただし, 並行実行された場合でも, ある程度の整合性は担保される
 * (おそらく, 状態更新のメソッドコールが無視されたような振る舞いになる).
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class OrthSequentialMultiplyingContainer {

    private final BaseMultiplyingContainer container;

    /**
     * 非公開のコンストラクタ. <br>
     * 引数のバリデーションが必要.
     */
    private OrthSequentialMultiplyingContainer(BaseMultiplyingContainer container) {
        super();
        this.container = container;
    }

    /**
     * 左から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    public void operateLeftSide(OrthogonalMatrix matrix) {
        container.operateLeftSide(matrix);
    }

    /**
     * 右から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    public void operateRightSide(OrthogonalMatrix matrix) {
        container.operateRightSide(matrix);
    }

    /**
     * 乗算結果を行列として返す.
     * 
     * @return 乗算結果
     */
    public ProductOrthogonalMatrix build() {
        return ProductOrthogonalMatrix.of(container.build());
    }

    /**
     * 与えた成分アクセス可能な直交行列をベースとする, 乗算コンテナを作成する.
     * 
     * @param base ベースとなる直交行列
     * @return 乗算コンテナ
     * @throws MatrixFormatMismatchException
     *             引数が直交行列 ({@link OrthogonalMatrix}
     *             のサブタイプ) でない場合
     * @throws NullPointerException 引数がnullの場合
     * @deprecated 型安全性を得るため, {@link #basedOn} を推奨する.
     */
    @Deprecated(since = "29.1")
    public static OrthSequentialMultiplyingContainer basedOnOrth(EntryReadableMatrix base) {

        if (!(Objects.requireNonNull(base) instanceof OrthogonalMatrix)) {
            throw new MatrixFormatMismatchException("not orthogonal");
        }

        return new OrthSequentialMultiplyingContainer(BaseMultiplyingContainer.basedOn(base));
    }

    /**
     * 与えた成分アクセス可能な直交行列をベースとする, 乗算コンテナを作成する.
     * 
     * @param <T> ベース行列の型パラメータ
     * @param base ベースとなる直交行列
     * @return 乗算コンテナ
     * @throws NullPointerException 引数がnullの場合
     */
    public static <T extends EntryReadableMatrix & OrthogonalMatrix>
            OrthSequentialMultiplyingContainer basedOn(T base) {

        // 通常利用では必ず成功, 原型を使った場合へのフォロー
        OrthogonalMatrix.class.cast(base);

        return new OrthSequentialMultiplyingContainer(BaseMultiplyingContainer.basedOn(base));
    }
}
