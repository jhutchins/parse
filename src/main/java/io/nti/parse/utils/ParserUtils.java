package io.nti.parse.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.nti.parse.Context;
import io.nti.parse.Parser;
import io.nti.parse.ParsingException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeff Hutchins
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParserUtils {

    public static final Parser<Character> WHITE_SPACE = any(" \t\n\r");
    public static final Parser<Character> DIGIT = range('0', '9');
    public static final Parser<Character> ALPHA = or(range('a', 'z'), range('A', 'Z'));

    public static <T> Parser<T> named(@Nonnull Parser<T> parser, @Nonnull String name) {
        return new Parser<T>() {
            @Nonnull
            @Override
            public T parse(Context ctx) throws ParsingException {
                return parser.parse(ctx);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static Parser<CharSequence> seq(@Nonnull CharSequence seq) {
        return named((ctx) -> {
            if (seq.equals(ctx.peek(seq.length()))) {
                ctx.advance(seq.length());
                return seq;
            }
            throw new ParsingException();
        }, "\"" + seq + "\"");
    }

    public static Parser<CharSequence> seq(@Nonnull Parser<Character> parser) {
        return named((ctx) -> parser.parse(ctx).toString(), parser.toString());
    }

    public static <T, R> Parser<R> join(@Nonnull Parser<List<T>> parser, Function<List<T>, R> mapper) {
        return named((ctx) -> mapper.apply(parser.parse(ctx)), parser.toString());
    }

    public static <T> Parser<T> unwrap(@Nonnull Parser<Optional<T>> parser, @Nonnull T empty) {
        return named((ctx) -> parser.parse(ctx).orElse(empty), parser.toString());
    }

    @SafeVarargs
    public static <T> Parser<T> or(@Nonnull Parser<? extends T>... parsers) {
        return named((ctx) -> {
            for (Parser<? extends T> parser : parsers) {
                try {
                    return ctx.parse(parser);
                } catch (ParsingException e) {
                    // No-Op
                }
            }
            throw new ParsingException();
        }, Arrays.stream(parsers).map(Parser::toString).collect(Collectors.joining(" / ")));
    }

    @SafeVarargs
    public static <T> Parser<List<T>> and(@Nonnull Parser<? extends T>... parsers) {
        return named((ctx) -> {
            final List<T> result = new LinkedList<>();
            for (Parser<? extends T> parser : parsers) {
                try {
                    result.add(parser.parse(ctx));
                } catch (ParsingException e) {
                    throw e;
                }
            }
            return result;
        }, Arrays.stream(parsers).map(Parser::toString).collect(Collectors.joining(" ")));
    }

    public static Parser<Optional<Void>> not(@Nonnull Parser<?> parser) {
        return named((ctx) -> {
            try {
                ctx.parse(parser);
            } catch (ParsingException e) {
                return Optional.empty();
            }
            throw new ParsingException();
        }, "!" + parser);
    }

    public static <T> Parser<Optional<T>> optional(@Nonnull Parser<T> parser) {
        return named((ctx) -> {
            try {
                return Optional.of(ctx.parse(parser));
            } catch (ParsingException e) {
                return Optional.empty();
            }
        }, "[ " + parser + " ]");
    }

    public static <T> Parser<List<T>> repeat(@Nonnull Parser<? extends T> parser) {
        return named(repeat(parser, 0), "*( " + parser + " )");
    }

    public static <T> Parser<List<T>> repeat(@Nonnull Parser<? extends T> parser, int count) {
        return named(repeat(parser, count, Integer.MAX_VALUE), count + "* (" + parser + " )");
    }

    public static <T> Parser<List<T>> repeat(@Nonnull Parser<? extends T> parser, int min, int max) {
        return named((ctx) -> {
            final List<T> result = new LinkedList<>();
            try {
                while (ctx.left() > 0 && result.size() < max) {
                    result.add(ctx.parse(parser));
                }
            } catch (ParsingException e) {
                // No-Op
            }
            if (result.size() < min) {
                throw new ParsingException();
            }
            return result;
        }, min + "*" + max + "( " + parser + " )");
    }

    public static Parser<Character> range(char start, char end) {
        return named((ctx) -> {
            final CharSequence peek = ctx.peek();
            if (peek.length() == 0) {
                throw new ParsingException();
            }
            final char c = peek.charAt(0);
            if (start <= c && c <= end) {
                return ctx.advance().charAt(0);
            }
            throw new ParsingException();
        }, "%x" + Integer.toHexString(start).toUpperCase() + "-" + Integer.toHexString(end).toUpperCase());
    }

    public static Parser<Character> range(int start, int end) {
        return range((char) start, (char) end);
    }

    public static Parser<Character> any(CharSequence sequence) {
        return named((ctx) -> {
            final CharSequence peek = ctx.peek();
            if (peek.length() == 0) {
                throw new ParsingException();
            }
            final char test = peek.charAt(0);
            if (sequence.chars().anyMatch(c -> c == test)) {
                return ctx.advance().charAt(0);
            }
            throw new ParsingException();
        }, "any(" + sequence + ")");
    }

    public static <T> Parser<T> constrain(Parser<T> parser, Parser<?> constraint) {
        return named((ctx) -> {
            ctx.mark();
            try {
                ctx.parse(constraint);
                ctx.reset();
            } catch (ParsingException e) {
                ctx.clear();
                throw e;
            }
            return ctx.parse(parser);
        }, parser.toString());
    }

    public static <T> Parser constrain(Parser<T> parser, Predicate<T> contraint) {
        return named((ctx) -> {
            final T result = parser.parse(ctx);
            if (!contraint.test(result)) {
                throw new ParsingException();
            }
            return result;
        }, parser.toString());
    }
}
