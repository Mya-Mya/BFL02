package bf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BrainFuckソースの最適化を行う。
 */
public class Optimizer {
    private static final List<OptimizeWay> ways = List.of(
            new OptimizeWay("><", "ポインタの無駄な移動",
                    s -> {
                        while (s.contains("><")) s = s.replace("><", "");
                        return s;
                    }),
            new OptimizeWay("<>", "ポインタの無駄な移動",
                    s -> {
                        while (s.contains("<>")) s = s.replace("<>", "");
                        return s;
                    }),
            new OptimizeWay("+-", "値の無駄な加減算",
                    s -> {
                        while (s.contains("+-")) s = s.replace("+-", "");
                        return s;
                    }),
            new OptimizeWay("-+", "値の無駄な加減算",
                    s -> {
                        while (s.contains("-+")) s = s.replace("-+", "");
                        return s;
                    }),
            new OptimizeWay("-+-+[-]", "ZERO前の無駄な加減算",
                    s -> {
                        while (s.contains("+[-]") || s.contains("-[-]")) s = s.replaceAll("[+-]*\\[-\\]", "[-]");
                        return s;
                    })
    );

    public static BFSource optimized(BFSource source) {
        String text = source.getText();
        boolean stillOptimizing = true;
        while (stillOptimizing) {
            String beforeText = text;
            for (OptimizeWay way : ways) {
                text = way.optimize.apply(text);
            }
            stillOptimizing = !beforeText.equals(text);
        }
        return new BFSource(text);
    }

    public static String description() {
        return ways.stream()
                .map(way -> String.format("%-10s%s", way.pattern, way.description))
                .collect(Collectors.joining("\n"));
    }

    private static class OptimizeWay {
        public final String pattern;
        public final String description;
        public final Function<String, String> optimize;

        public OptimizeWay(String pattern, String description, Function<String, String> optimize) {
            this.pattern = pattern;
            this.description = description;
            this.optimize = optimize;
        }
    }
}
