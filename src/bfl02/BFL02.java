package bfl02;

import bf.BFSource;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * BrainFuck補助関数系02低級言語
 * このクラスのメソッドを呼び出すと
 * それらの意味する命令が記述されたBFソースが出てくる。
 * <p>
 * ユーザーが呼び出せる命令は必ず命令の呼び出し前と呼び出し後とでポインタのいる番地が同じでないといけない。
 */
public class BFL02 {
    //一部の命令実行時に一時的に使用する番地。この番地を変数の番地として使用してはならない。
    private int T;

    /**
     * 使用する変数の番地を指定してBFソースの製作を開始する。
     * <p>
     * 注意
     * ここで指定した番地以外の番地を変数をとして使うと、
     * 一部の命令が実行時に使用する番地と衝突しバグの原因になる。
     *
     * @param V 使用する変数の番地
     */
    public BFL02(List<Integer> V) {
        if (V.isEmpty()) T = 0;
        else {
            V = new ArrayList<>(V);
            //TはVの中心の近くにある方がよい
            List<Integer> candidates = new ArrayList<>();
            V.sort(Comparator.comparingInt(Integer::intValue));
            //変数番地領域の左端
            int min = V.get(0);
            if (min != 0) candidates.add(min - 1);
            //変数番地領域の右端
            int max = V.get(V.size() - 1);
            candidates.add(max + 1);
            //変数番地領域の中
            int before_v = min;
            for (int v : V) {
                for (int i = before_v + 1; i < v; i++)
                    candidates.add(i);
                before_v = v;
            }


            //各候補地と変数番地領域の重心の距離を求める
            double g = V.stream().collect(DoubleSummaryStatistics::new, DoubleSummaryStatistics::accept, DoubleSummaryStatistics::combine).getAverage();
            Double dists[] = candidates.stream()
                    .map(p -> Math.abs(p - g))
                    .toArray(Double[]::new);
            //最も重心に近い候補地を出す
            int nearestCandidateIndex = 0;
            double nearestDist = dists[0];
            for (int index = 0; index < dists.length; index++) {
                if (nearestDist > dists[index]) nearestCandidateIndex = index;
            }
            T = candidates.get(nearestCandidateIndex);
        }
    }

    /**
     * 決められた番地へポインタを動かす。が戻ってはこないことに注意。
     * p = P
     */
    private BFSource go(int P) {
        if (P == 0) return new BFSource();
        int absP = Math.abs(P);
        char mover = P > 0 ? '>' : '<';
        return new BFSource(StringUtil.repeatChar(mover, absP));
    }

    /**
     * 決められた番地へポインタを動かし、その番地で作業を行い、元の番地へポインタを戻す。
     * ORIGINAL = p
     * p = P
     * process
     * p = ORIGINAL
     *
     * @param process 動かした先の番地で行う作業
     */
    private BFSource GOSUB(int P, BFSource process) {
        return go(P).append(process).append(go(-P));
    }

    /**
     * 変数を0にする。
     *
     * @param P 対象の変数の番地
     */
    public BFSource ZERO(int P) {
        return GOSUB(P, new BFSource("[-]"));
    }

    /**
     * 今ポインタが指し示している値を加算または減算する。
     * *p += N
     *
     * @param N 加算または減算する値 正負可
     */
    private BFSource createAdder(int N) {
        if (N == 0) return new BFSource();
        int absN = Math.abs(N);
        char operator = N > 0 ? '+' : '-';
        return new BFSource(StringUtil.repeatChar(operator, absN));
    }

    /**
     * 変数を加算する。
     * *P += N
     *
     * @param P 対象の変数の番地
     * @param N 加算または減算する値 正負可
     */
    public BFSource ADDCONST(int P, int N) {
        if (N == 0) return new BFSource();
        return GOSUB(P, createAdder(N));
    }

    /**
     * 変数を定数に設定する。
     * *P = N
     *
     * @param P 対象の変数の番地
     * @param N 設定値 正のみ
     */
    public BFSource SETCONST(int P, int N) {
        return ZERO(P).append(ADDCONST(P, N));
    }

    /**
     * 変数が0でない限り、ENDWHILE Pまでの処理を繰り返し行う。
     * while *P{
     *
     * @param P 対象の変数の番地
     */
    public BFSource WHILE(int P) {
        return GOSUB(P, new BFSource("["));
    }

    /**
     * 対応するWHILE Pを終了させる。
     * <p>
     * 備考
     * WHILE P
     * middle
     * ENDWHILE P
     * は以下のように解釈されるため、[は繰り返し*Pの値確認を行うことができる。
     * GO(p)[GO(-P) middle GO(P)]GO(-P)
     * <p>
     * 注意
     * WHILE-ENDWHILEやIF-ENDIFの内包関係を壊してはいけない。
     *
     * @param P これに対応するWHILEで指定したP
     */
    public BFSource ENDWHILE(int P) {
        return GOSUB(P, new BFSource("]"));
    }

    /**
     * 番地Sにある値だけ番地Dにある値へ加算し、番地Sにある値は0にする。
     * *Di += *S
     * *S = 0
     *
     * @param S 加算する変数の番地
     * @param D 加算される変数の番地 複数可
     */
    public BFSource MOV(int S, List<Integer> D) {
        BFSource source = WHILE(S);
        for (int d : D)
            source.append(GOSUB(d, new BFSource("+")));

        source.append(GOSUB(S, new BFSource("-")));
        source.append(ENDWHILE(S));
        return source;
    }

    /**
     * 番地Sにある値だけ番地Dにある値へ加算する。番地Sにある値は保持される。
     * *Di += *S
     * *S = *S
     *
     * @param S 加算する変数の番地
     * @param D 加算される変数の番地 複数可
     */
    public BFSource ADDVAR(int S, List<Integer> D) {
        List<Integer> directions = new ArrayList<>(D);
        directions.add(S);
        return ZERO(T)
                .append(MOV(S, List.of(T)))
                .append(MOV(T, directions));
    }

    /**
     * 番地Sにある値だけ番地Dにある値を減算する。番地Sにある値は保持される。
     * *Di -= *S
     * *S = *S
     *
     * @param S 減算する変数の番地
     * @param D 減算される変数の番地 複数可
     */
    public BFSource SUBVAR(int S, int D) {
        return ZERO(T)
                .append(MOV(S, List.of(T)))
                .append(WHILE(T))
                .append(GOSUB(T, new BFSource("-")))
                .append(GOSUB(D, new BFSource("-")))
                .append(GOSUB(S, new BFSource("+")))
                .append(ENDWHILE(T));
    }

    /**
     * 番地Sにある値を番地Dへ設定する。番地Sにある値は保持される。
     * *Di = *S
     * *S = *S
     *
     * @param S 設定する変数の番地
     * @param D 設定される変数の番地 複数可
     */
    public BFSource CPY(int S, List<Integer> D) {
        BFSource source = new BFSource();
        for (int d : D)
            source.append(ZERO(d));
        return source.append(ADDVAR(S, D));
    }

    /**
     * 変数を入力情報に設定する。
     *
     * @param P 入力情報を受け取る変数の番地
     */
    public BFSource IN(int P) {
        return GOSUB(P, new BFSource(","));
    }

    /**
     * 変数を出力する。
     *
     * @param P 出力情報とする変数の番地
     */
    public BFSource OUT(int P) {
        return GOSUB(P, new BFSource("."));
    }

    /**
     * 変数が0でない限り、ENDIF Pまでの処理を1回行う。
     * while *P{
     *
     * @param P 対象の変数の番地
     */
    public BFSource IF(int P) {
        return WHILE(P);
    }

    /**
     * IF Pを終了させる。
     * <p>
     * 備考
     * IF P
     * middle
     * ENDIF P
     * は以下のように解釈されるため、[]構文でIFの動きを実装できる。
     * GO(P)[GO(-P) middle *TEMP_POINTER=*P *P=0 GO(P)]*P=*TEMP_POINTER GO(-P)
     * <p>
     * 注意
     * WHILE-ENDWHILEやIF-ENDIFの内包関係を壊してはいけない。
     *
     * @param P これに対応するIFで指定したP
     */
    public BFSource ENDIF(int P) {
        return ZERO(T)
                .append(MOV(P, List.of(T)))
                .append(ENDWHILE(P))
                .append(MOV(T, List.of(P)));
    }

    /**
     * ASCII文字列を表示させる。
     *
     * @param X ASCII文字列
     */
    public BFSource MES(String X) {
        //注意!! operationはpがTEMP_POINTERにいる状態でずっと行われる
        BFSource operation = new BFSource("[-]");//TEMP_POINTERの初期化
        int before = 0;
        for (Character X_i : X.toCharArray()) {
            int code = (int) X_i;
            operation.append(createAdder(code - before));
            operation.append(".");
            before = code;
        }
        return GOSUB(T, operation);
    }

    /**
     * 出力を改行する。
     */
    public BFSource WRAP() {
        return MES("\n");
    }

    /**
     * 何もしない。
     */
    public BFSource NONE() {
        return new BFSource();
    }

    /**
     * 変数と定数が同じ数か調べ、同じであれば1を、異なれば0をその変数へ設定する。
     *
     * @param P 対象の変数の番地
     * @param N 比較したい定数
     */
    public BFSource EQCONST(int P, int N) {
        return ZERO(T)
                .append(MOV(P, List.of(T)))
                .append(ADDCONST(P, 1))
                .append(ADDCONST(T, -N))
                .append(GOSUB(T, new BFSource("[")))//*P!=Nの時だけこのwhileに入る
                .append(GOSUB(P, new BFSource("-")))
                .append(ZERO(T))
                .append(GOSUB(T, new BFSource("]")));
    }
}
