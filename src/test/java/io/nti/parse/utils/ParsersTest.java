package io.nti.parse.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Test;

import io.nti.parse.Context;
import io.nti.parse.Parser;
import io.nti.parse.ParsingException;
import io.nti.parse.SimpleContext;

import static io.nti.parse.utils.Parsers.*;
import static io.nti.parse.utils.Parsers.not;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Jeff Hutchins
 */
public class ParsersTest {

    @Test
    public void testSeq() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final String sequence = "abcd";
        final Parser<CharSequence> parser = seq(sequence);
        assertThat(parser.parse(ctx)).isEqualTo(sequence);
        assertThat(ctx.pos()).isEqualTo(sequence.length());
    }

    @Test
    public void testOtherSeq() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final String sequence = "cbda";
        final Parser<CharSequence> parser = seq(any(sequence));
        assertThat(parser.parse(ctx)).isEqualTo("a");
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test
    public void testSeqToLong() {
        final Context ctx = new SimpleContext("abc");
        final String sequence = "abcd";
        final Parser<CharSequence> parser = seq(sequence);
        try {
            parser.parse(ctx);
            fail("Expected Exception");
        } catch (ParsingException e) {
            // No-Op
        }
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testOr() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<CharSequence> parser = Parsers.or(seq("cd"), seq("ab"));

        assertThat(parser.parse(ctx)).isEqualTo("ab");
        assertThat(ctx.pos()).isEqualTo(2);

        assertThat(parser.parse(ctx)).isEqualTo("cd");
        assertThat(ctx.pos()).isEqualTo(4);

        try {
            parser.parse(ctx);
            fail("Expected Exception");
        } catch (ParsingException e) {
            // No-Op
        }
        assertThat(ctx.pos()).isEqualTo(4);
    }

    @Test
    public void testAnd() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<List<CharSequence>> parser = and(seq("ab"), seq("cd"));

        assertThat(parser.parse(ctx)).hasSize(2).containsExactly("ab", "cd");
        assertThat(ctx.pos()).isEqualTo(4);
    }

    @Test
    public void testNotMatches() {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Optional<Void>> parser = not(seq("abc"));

        try {
            ctx.parse(parser);
            fail("Expected Exception");
        } catch (ParsingException e) {
            // No-Op
        }
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testNotNoMatches() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Optional<Void>> parser = not(seq("xyz"));

        assertThat(parser.parse(ctx)).isEmpty();
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testOptionalMatches() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Optional<CharSequence>> parser = optional(seq("abc"));

        assertThat(parser.parse(ctx)).isPresent().contains("abc");
        assertThat(ctx.pos()).isEqualTo(3);
    }

    @Test
    public void testOptionalNoMatch() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Optional<CharSequence>> parser = optional(seq("xyz"));

        assertThat(parser.parse(ctx)).isEmpty();
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testRepeat() throws ParsingException {
        final Context ctx = new SimpleContext("abababac");
        final Parser<List<CharSequence>> parser = repeat(seq("ab"));

        assertThat(parser.parse(ctx)).hasSize(3).contains("ab", "ab", "ab");
        assertThat(ctx.pos()).isEqualTo(6);
    }

    @Test
    public void testRepeatMinMet() throws ParsingException {
        final Context ctx = new SimpleContext("abababac");
        final Parser<List<CharSequence>> parser = repeat(seq("ab"), 2);

        assertThat(parser.parse(ctx)).hasSize(3).contains("ab", "ab", "ab");
        assertThat(ctx.pos()).isEqualTo(6);
    }

    @Test(expected = ParsingException.class)
    public void testRepeatMinNotMet() throws ParsingException {
        final Context ctx = new SimpleContext("abababacdef");
        final Parser<List<CharSequence>> parser = repeat(seq("ab"), 4);
        parser.parse(ctx);
    }

    @Test
    public void testRange() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Character> parser = range('a', 'd');
        assertThat(parser.parse(ctx)).isEqualTo('a');
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test
    public void testRangeInt() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Character> parser = range(0x61, 0x64);
        assertThat(parser.parse(ctx)).isEqualTo('a');
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test
    public void testAny() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Character> parser = any("%$#aTG");
        assertThat(parser.parse(ctx)).isEqualTo('a');
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test(expected = ParsingException.class)
    public void testAnyNoMore() throws ParsingException {
        final Context ctx = new SimpleContext("");
        final Parser<Character> parser = any("%$#aTG");
        parser.parse(ctx);
    }

    @Test
    public void testConstrain() throws ParsingException {
        final Context ctx = new SimpleContext("abc-d-");
        final Parser<Character> parser = or(ALPHA, constrain(any("-"), and(any("-"), ALPHA)));
        assertThat(parser.parse(ctx)).isEqualTo('a');
        assertThat(parser.parse(ctx)).isEqualTo('b');
        assertThat(parser.parse(ctx)).isEqualTo('c');
        assertThat(parser.parse(ctx)).isEqualTo('-');
        assertThat(parser.parse(ctx)).isEqualTo('d');
        try {
            parser.parse(ctx);
            fail("Expected Exception");
        } catch (ParsingException e) {
            // No-Op
        }
        assertThat(ctx.pos()).isEqualTo(5);
    }

    @Test
    public void testPredicateConstraintPass() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Character> parser = constrain(any("abcd"), (Predicate<Character>)(c) -> true);
        assertThat(parser.parse(ctx)).isEqualTo('a');
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test(expected = ParsingException.class)
    public void testPredicateConstraintFail() throws ParsingException {
        final Context ctx = new SimpleContext("abcdefg");
        final Parser<Character> parser = constrain(any("abcd"), (Predicate<Character>)(c) -> false);
       parser.parse(ctx);
    }

    @Test
    public void moreTests() throws ParsingException {
        final Context ctx = new SimpleContext("<%----%><%--\nTest\n--%>");
        final Parser<CharSequence> start = seq("<%--");
        final Parser<Character> Char = Parsers.or(any("\t\n\r"), range((char) 0x20, (char) 0xD7FF), range((char) 0xE000, (char) 0xFFFD));
        final Parser<CharSequence> end = seq("--%>");
        final Parser<CharSequence> middle = join(unwrap(optional(repeat(constrain(Char, not(end)))), Collections.emptyList()), list -> {
            final StringBuilder builder = new StringBuilder();
            list.stream().forEach(builder::append);
            return builder.toString();
        });
        final Parser<CharSequence> jspComment = join(and(start, middle, end), list -> String.join("", list));
        assertThat(jspComment.parse(ctx)).isEqualTo("<%----%>");
        assertThat(jspComment.parse(ctx)).isEqualTo("<%--\nTest\n--%>");
    }
}
