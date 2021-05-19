package fim.unipassau.de.scratch1984.util;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Utility class for converting Markdown to HTML.
 */
public final class MarkdownHandler {

    /**
     * The {@link Parser} used to parse Markdown via the commonmark-java library.
     */
    private static final Parser PARSER = Parser.builder().build();

    /**
     * The {@link HtmlRenderer} used to render HTML from Markdown via the commonmark-java library.
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().escapeHtml(true).build();

    /**
     * Prevents instantiation of this utility class.
     */
    private MarkdownHandler() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses and renders the given Markdown String to HTML.
     *
     * @param md The input formatted as Markdown.
     * @return The parsed and rendered equivalent HTML Output.
     */
    public static String toHtml(final String md) {
        if (md == null) {
            throw new IllegalArgumentException("Invalid String for Markdown parsing!");
        }
        return RENDERER.render(PARSER.parse(md));
    }

}
