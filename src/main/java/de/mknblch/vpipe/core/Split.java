package de.mknblch.vpipe.core;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author Jiří Kraml (jkraml@avantgarde-labs.de)
 */
public final class Split {

    private Split() {}

    /**
     * split computation by evaluating the input
     * value with each sub processor.
     */
    public static class SplitTwo<I, L, R> implements Function<I, Tuple.Two<L, R>> {
        private final Function<I, L> leftProcessor;
        private final Function<I, R> rightProcessor;

        public SplitTwo(Function<I, L> leftProcessor, Function<I, R> rightProcessor) {
            requireNonNull(leftProcessor, "leftProcess must not be null");
            requireNonNull(rightProcessor, "rightProcess must not be null");

            this.leftProcessor = leftProcessor;
            this.rightProcessor = rightProcessor;
        }

        @Override
        public Tuple.Two<L, R> apply(I in) {
            return Tuple.from(
                    leftProcessor.apply(in),
                    rightProcessor.apply(in)
            );
        }
    }

    /**
     * split computation by evaluating the input
     * value with each sub processor.
     */
    public static class SplitThree<I, L, M, R> implements Function<I, Tuple.Three<L, M, R>> {
        private final Function<I, L> leftProcessor;
        private final Function<I, M> middleProcessor;
        private final Function<I, R> rightProcessor;

        public SplitThree(Function<I, L> leftProcessor, Function<I, M> middleProcessor, Function<I, R> rightProcessor) {
            requireNonNull(leftProcessor, "leftProcess must not be null");
            requireNonNull(middleProcessor, "middleProcess must not be null");
            requireNonNull(rightProcessor, "rightProcess must not be null");

            this.leftProcessor = leftProcessor;
            this.middleProcessor = middleProcessor;
            this.rightProcessor = rightProcessor;
        }

        @Override
        public Tuple.Three<L, M, R> apply(I in) {
            return Tuple.from(
                    leftProcessor.apply(in),
                    middleProcessor.apply(in),
                    rightProcessor.apply(in)
            );
        }
    }
}
