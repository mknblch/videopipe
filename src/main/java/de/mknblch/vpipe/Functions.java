package de.mknblch.vpipe;

import de.mknblch.vpipe.core.*;
import de.mknblch.vpipe.core.Image;
import de.mknblch.vpipe.functions.*;
import de.mknblch.vpipe.functions.contours.*;
import de.mknblch.vpipe.functions.ExecutionTimer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.mknblch.vpipe.core.Image.clip;

/**
 * @author mknblch
 */
public class Functions {

    private Functions() {}

    /**
     * Split computation into 2 pipes
     * @author Jiří Kraml (jkraml@avantgarde-labs.de)
     */
    public static <I, L, R> Function<I, Tuple.Two<L, R>> split(Function<I, L> leftProcessor, Function<I, R> rightProcessor) {
        return new Split.SplitTwo<>(leftProcessor, rightProcessor);
    }

    /**
     * Split computation into 3 pipes
     * @author Jiří Kraml (jkraml@avantgarde-labs.de)
     */
    public static <I, L, M, R> Function<I, Tuple.Three<L, M, R>> split(Function<I, L> leftProcessor, Function<I, M> middleProcessor, Function<I, R> rightProcessor) {
        return new Split.SplitThree<>(leftProcessor, middleProcessor, rightProcessor);
    }

    /**
     * Merge 2 pipes
     * @author Jiří Kraml (jkraml@avantgarde-labs.de)
     */
    public static <L, R, O> Function<Tuple.Two<L, R>, O> merge(BiFunction<L, R, O> mergeFunction) {
        return new Merge.MergeTwo<>(mergeFunction);
    }

    /**
     * Merge 3 pipes
     * @author Jiří Kraml (jkraml@avantgarde-labs.de)
     */
    public static <L, M, R, O> Function<Tuple.Three<L, M, R>, O> merge(Merge.TriFunction<L, M, R, O> mergeFunction) {
        return new Merge.MergeThree<>(mergeFunction);
    }

    /**
     * create contour processor using a simple filter
     * @param threshold activation threshold
     * @return contour processor
     */
    public static Function<Image.Gray, List<Contour>> contours(int threshold) {
        return contours(threshold, (perimeter, area, x0, y0, x1, y1) -> Math.abs(area) > 10);
    }

    /**
     * calculate contours of a GrayImage based on a threshold. Also
     * groups contours and assigns a (maybe) unique hash
     *
     * @param threshold a threshold between 0 and 255
     * @return a contour processor
     */
    public static Function<Image.Gray, List<Contour>> contours(int threshold, Contour.Filter filter) {
        return new Chain4(threshold, filter).andThen(new Group()).andThen(new Hash(11));
    }

    /**
     * call consumer for each element passing the pipe
     * @param consumer consumer implementation
     * @return a {@link TPipe} using the supplied consumer
     */
    public static <T> Function<T, T> peek(Consumer<T> consumer) {
        return new TPipe<>(consumer);
    }

    /**
     * print info about number of contours and its perimeter
     * @return
     */
    public static Function<List<Contour>, List<Contour>> info() {
        return peek(contours -> {
            System.out.printf("%d contours with perimeter %d and area %d%n",
                    contours.size(),
                    contours.stream().mapToInt(Contour::perimeter).sum(),
                    contours.stream().mapToInt(Contour::area).sum());
        });
    }

    /**
     * encapsulate a function into an {@link ExecutionTimer} for
     * evaluating its computation duration
     */
    public static <I, O> Function<I, O> timer(Function<I, O> func) {
        return new ExecutionTimer<>(func, (duration) -> {
            System.out.printf("%s ~%s%n",
                    func.getClass().getSimpleName(),
                    String.format("%d.%04ds (%d fps)", (duration / 1000) % 60, duration, 1000 / duration));
        }, 20);
    }

    /**
     * invert a GrayImage
     */
    public static Function<Image.Gray, Image.Gray> invert() {
        return new PixelProcessor.Gray2Gray(b -> 255 - b);
    }

    /**
     * Color binarization based on rgb-mean
     * @param threshold
     */
    public static Function<Image.Color, Image.Gray> binarization_rgb(int threshold) {
        return new PixelProcessor.Color2Gray((r, g, b) -> (r + g + b) / 3 >= threshold ? 255 : 0);
    }

    public static Function<Image.Color, Image.Color> colorFilter(Color color, double threshold) {
        return ColorPassFilterBuilder.build(color, threshold);
    }

    public static Function<Image.Color, Image.Gray> whiteFilter(int threshold) {
        return whiteFilter(threshold, WhiteFilter.Mode.SIMPLE);
    }

    public static Function<Image.Color, Image.Gray> whiteFilter(int threshold, WhiteFilter.Mode mode) {
        return new WhiteFilter(mode, threshold);
    }

    /**
     * Gray binarization
     * @param threshold
     */
    public static Function<Image.Gray, Image.Gray> binarization(int threshold) {
        return new PixelProcessor.Gray2Gray(p -> p > threshold ? 255 :0);
    }

    /**
     * Mean RGB
     */
    public static Function<Image.Color, Image.Gray> grayscale() {
        return grayscale(1. / 3, 1. / 3, 1. / 3);
    }

    /**
     * Mean RGB
     */
    public static Function<Image.Color, Image.Gray> grayscale(double fRed, double fGreen, double fBlue) {
        return new PixelProcessor.Color2Gray((r, g, b) -> clip(r * fRed + g * fGreen + b * fBlue));
    }

    /**
     * luminosity
     */
    public static Function<Image.Color, Image.Gray> grayLuminosity() {
        return grayscale(0.21, 0.72, 0.07);
    }

    /**
     * red
     */
    public static Function<Image.Gray, Image.Color> colorFilter(PixelProcessor.ColorIntensityFunction function) {
        return new PixelProcessor.Gray2Color(function);
    }
    /**
     * red
     */
    public static Function<Image.Color, Image.Gray> red() {
        return new PixelProcessor.Color2Gray((r, g, b) -> r);
    }

    /**
     * red
     */
    public static Function<Image.Color, Image.Gray> green() {
        return new PixelProcessor.Color2Gray((r, g, b) -> g);
    }

    /**
     * red
     */
    public static Function<Image.Color, Image.Gray> blue() {
        return new PixelProcessor.Color2Gray((r, g, b) -> b);
    }

    /**
     * gamma
     * @param a -255 - 255
     */
    public static Function<Image.Gray, Image.Gray> gamma(int a) {
        return new PixelProcessor.Gray2Gray(b -> b + a);
    }

    public static Function<Image.Color, Image.Color> mask(Image.Gray mask) {
        return in -> Images.mul(in, mask);
    }

    /**
     * contrast
     * @param f contrast factor
     */
    public static Function<Image.Gray, Image.Gray> contrast(double f) {
        return new PixelProcessor.Gray2Gray(b -> (int)((b - 128) * f) + 128);
    }

    /**
     * closing operation
     */
    public static Function<Image.Gray, Image.Gray> closing() {
        return dilation().andThen(erosion());
    }

    /**
     * opening operation
     */
    public static Function<Image.Gray, Image.Gray> opening() {
        return erosion().andThen(dilation());
    }

    /**
     * pixel dilation
     */
    public static Function<Image.Gray, Image.Gray> dilation() {
        return new Morphological.Dilation();
    }

    /**
     * pixel erosion
     */
    public static Function<Image.Gray, Image.Gray> erosion() {
        return new Morphological.Erosion();
    }

    /**
     * spatial convolution
     * @param kernel a convolution kernel
     */
    public static Function<Image.Gray, Image.Gray> convolution(Convolution.Kernel kernel) {
        return new Convolution(kernel);
    }

    /**
     * transform Image to BufferedImage
     */
    public static <I extends Image> Function<I, BufferedImage> toBufferedImage() {
        return new BufferedImageConverter<>();
    }

    /**
     * render contours and colorize by contour depth
     */
    public static Function<List<Contour>, BufferedImage> renderDepth(int width, int height) {
        return new Renderer.Depth(width, height);
    }

    /**
     * render contour and visualize some of its properties
     */
    public static Function<List<Contour>, BufferedImage> renderAll(int width, int height) {
        return new Renderer.All(width, height);
    }

    /**
     * render minimal enclosing bounding box of each contour
     */
    public static Function<List<Contour>, BufferedImage> renderBoundingBox(int width, int height) {
        return new Renderer.BoundingBox(width, height);
    }

}
