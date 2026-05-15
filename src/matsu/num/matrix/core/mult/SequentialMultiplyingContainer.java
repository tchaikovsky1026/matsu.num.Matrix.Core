/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.15
 */
package matsu.num.matrix.core.mult;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 逐次的に行列積の計算を実行する機能を扱う.
 * 
 * <p>
 * このクラスはミュータブルなクラスである. <br>
 * 最初にベースとなる行列を与え, コンテナを用意する
 * ({@link #basedOn(EntryReadableMatrix)} メソッド). <br>
 * その後, 左右から行列を乗算するメソッド
 * {@link #operateLeftSide(Matrix)},
 * {@link #operateRightSide(Matrix)}
 * をコールし, 状態を更新していく. <br>
 * 状態更新が完了したら, ビルドメソッド
 * {@link #build()} をコールし, 行列を構築する. <br>
 * 構築された行列は, {@link ProductMatrix} 型となる.
 * </p>
 * 
 * <p>
 * ビルダはビルド後でも再利用可能である.
 * </p>
 * 
 * <p>
 * 状態更新する行列は, 正方行列でなければならない. <br>
 * これにより, 状態を更新しても行列のサイズ (次元) が不変であることが担保される.
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
public final class SequentialMultiplyingContainer {

    private final BaseMultiplyingContainer container;

    /**
     * 非公開のコンストラクタ. <br>
     * 引数のバリデーションが必要.
     */
    private SequentialMultiplyingContainer(BaseMultiplyingContainer container) {
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
    public void operateLeftSide(Matrix matrix) {
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
    public void operateRightSide(Matrix matrix) {
        container.operateRightSide(matrix);
    }

    /**
     * 乗算結果を行列として返す.
     * 
     * @return 乗算結果
     */
    public ProductMatrix build() {
        return ProductMatrix.of(container.build());
    }

    /**
     * 与えた行列をベースとする, 乗算コンテナを作成する.
     * 
     * @param base ベースとなる行列
     * @return 乗算コンテナ
     * @throws NullPointerException 引数がnullの場合
     */
    public static SequentialMultiplyingContainer basedOn(EntryReadableMatrix base) {
        return new SequentialMultiplyingContainer(BaseMultiplyingContainer.basedOn(base));
    }
}
