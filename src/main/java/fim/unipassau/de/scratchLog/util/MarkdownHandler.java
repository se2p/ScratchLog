/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.util;

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
