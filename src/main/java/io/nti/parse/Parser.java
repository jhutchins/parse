package io.nti.parse;

import javax.annotation.Nonnull;

/**
 * @author Jeff Hutchins
 */
public interface Parser<T> {

    @Nonnull
    T parse(Context ctx) throws ParsingException;
}
