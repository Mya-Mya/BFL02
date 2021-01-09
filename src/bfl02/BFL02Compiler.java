package bfl02;

import bf.BFSource;
import bf.Optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BFL02Compiler {
    private BFL02 bfl02;
    private boolean showComment = false;

    public BFL02Compiler(BFL02 bfl02) {
        this(bfl02, true);
    }

    public BFL02Compiler(BFL02 bfl02, boolean showComment) {
        this.bfl02 = bfl02;
        this.showComment = showComment;
    }

    public BFSource compile(String text) {
        return compile(text, false);
    }

    public BFSource compile(String text, boolean optimize) {
        BFSource BFSource = new BFSource();
        for (String line : text.split("\n")) {
            BFSource.append(compileLine(line));
        }
        return optimize ? Optimizer.optimized(BFSource) : BFSource;
    }

    public BFSource compileLine(String line) {
        line = line.replaceAll("^ *", "")
                .replaceAll("\t", "");
        String words[] = line.split(" ");
        if (words.length == 0) return new BFSource();
        String commandName = words[0];
        List<String> params = getParameters(words);

        switch (commandName) {
            case "ZERO":
                return bfl02.ZERO(toInt(params.get(0)));
            case "ADDCONST":
                return bfl02.ADDCONST(toInt(params.get(0)), toInt(params.get(1)));
            case "SETCONST":
                return bfl02.SETCONST(toInt(params.get(0)), toInt(params.get(1)));
            case "WHILE":
                return bfl02.WHILE(toInt(params.get(0)));
            case "ENDWHILE":
                return bfl02.ENDWHILE(toInt(params.get(0)));
            case "EQCONST":
                return bfl02.EQCONST(toInt(params.get(0)), toInt(params.get(1)));
            case "MOV":
                return bfl02.MOV(toInt(params.get(0)), toIntList(skipped(params, 1)));
            case "ADDVAR":
                return bfl02.ADDVAR(toInt(params.get(0)), toIntList(skipped(params, 1)));
            case "SUBVAR":
                return bfl02.SUBVAR(toInt(params.get(0)), toInt(params.get(1)));
            case "CPY":
                return bfl02.CPY(toInt(params.get(0)), toIntList(skipped(params, 1)));
            case "IN":
                return bfl02.IN(toInt(params.get(0)));
            case "OUT":
                return bfl02.OUT(toInt(params.get(0)));
            case "IF":
                return bfl02.IF(toInt(params.get(0)));
            case "ENDIF":
                return bfl02.ENDIF(toInt(params.get(0)));
            case "MES":
                return bfl02.MES(mergeToOneParam(params, " "));
            case "WRAP":
                return bfl02.WRAP();
        }
        return new BFSource(showComment ? "\n" + line + "\n" : "");
    }

    private int toInt(String s) {
        return Integer.parseInt(s);
    }

    private List<Integer> toIntList(List<String> stringList) {
        return stringList.stream()
                .mapToInt(this::toInt)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private List<String> getParameters(String words[]) {
        if (words.length <= 1) return new ArrayList<>();
        int n = words.length - 1;
        try {
            return Stream.of(words)
                    .skip(1)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException(
                "命令とパラメーター" +
                        Arrays.stream(words).collect(Collectors.joining(",")) +
                        "付近でのエラー"
        );
    }

    private List<String> skipped(List<String> array, int n) {
        return array.stream()
                .skip(n)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private String mergeToOneParam(List<String> params, String delimiter) {
        return params.stream().collect(
                () -> new StringJoiner(delimiter),
                StringJoiner::add,
                StringJoiner::merge
        ).toString();
    }
}
