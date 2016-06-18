package io.nti.parse;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Jeff Hutchins
 */
public abstract class ContextTest {

    public abstract Context getContext(String data);

    @Test
    public void testPeekOverflow() {
        final String expected = "adc";
        final Context ctx = getContext(expected);
        assertThat(ctx.peek(expected.length() * 10)).isEqualTo(expected);
        assertThat(ctx.peek(expected.length() * 10)).isEqualTo(expected);
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testPeek() {
        final String expected = "adc";
        final Context ctx = getContext("0" + expected);
        ctx.advance();
        assertThat(ctx.peek(expected.length())).isEqualTo(expected);
        assertThat(ctx.peek(expected.length())).isEqualTo(expected);
        assertThat(ctx.pos()).isEqualTo(1);
    }

    @Test
    public void testDefaultPeek() {
        final String expected = "adc";
        final Context ctx = getContext(expected);
        assertThat(ctx.peek()).isEqualTo("a");
        assertThat(ctx.peek()).isEqualTo("a");
        assertThat(ctx.pos()).isEqualTo(0);
    }

    @Test
    public void testDefaultAdvance() {
        final Context ctx = getContext("abc");
        assertThat(ctx.advance()).isEqualTo("a");
        assertThat(ctx.advance()).isEqualTo("b");
        assertThat(ctx.advance()).isEqualTo("c");
        assertThat(ctx.advance()).isEqualTo("");
        assertThat(ctx.advance()).isEqualTo("");
    }

    @Test
    public void testAdvance() {
        final Context ctx = getContext("abc");
        assertThat(ctx.advance(2)).isEqualTo("ab");
        assertThat(ctx.advance(2)).isEqualTo("c");
        assertThat(ctx.advance(2)).isEqualTo("");
        assertThat(ctx.advance(2)).isEqualTo("");
    }
}
