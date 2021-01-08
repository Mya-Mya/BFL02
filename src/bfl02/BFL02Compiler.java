package bfl02;

import bf.BFSource;
import bf.Optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BFL02Compiler {
    private BFL02 bfl02;

    public BFL02Compiler(BFL02 bfl02) {
        this.bfl02 = bfl02;
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
        String words[] = line.split(" ");
        if (words.length == 0) return new BFSource();
        String commandName = words[0];
        List<Integer> params = getParameters(words);

        switch (commandName) {
            case "ADDCONST":
                return bfl02.ADDCONST(params.get(0), params.get(1));
            case "SETCONST":
                return bfl02.SETCONST(params.get(0), params.get(1));
            case "WHILE":
                return bfl02.WHILE(params.get(0));
            case "ENDWHILE":
                return bfl02.ENDWHILE(params.get(0));
            case "MOV":
                return bfl02.MOV(params.get(0), skipped(params, 1));
            case "ADDVAR":
                return bfl02.ADDVAR(params.get(0), skipped(params, 1));
            case "CPY":
                return bfl02.CPY(params.get(0), skipped(params, 1));
            case "IN":
                return bfl02.IN(params.get(0));
            case "OUT":
                return bfl02.OUT(params.get(0));
        }
        throw new IllegalArgumentException(commandName + "という関数が分からない");
    }

    private int toInt(String s) {
        return Integer.parseInt(s);
    }

    private List<Integer> getParameters(String words[]) {
        if (words.length <= 1) return new ArrayList<>();
        int n = words.length - 1;
        try {
            return Stream.of(words)
                    .skip(1)
                    .mapToInt(Integer::parseInt)
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

    private List<Integer> skipped(List<Integer> array, int n) {
        return array.stream()
                .skip(n)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
