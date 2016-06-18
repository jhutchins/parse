package io.nti.parse;

/**
 * @author Jeff Hutchins
 */
public interface Context {

    default CharSequence peek() {
        return peek(1);
    }

    CharSequence peek(int length);

    int pos();

    default CharSequence advance() {
        return advance(1);
    }

    int mark();

    int reset();

    int clear();

    CharSequence advance(int length);

    int left();

    default <T> T parse(Parser<T> parser) throws ParsingException {
        mark();
        T result;
        try {
            result = parser.parse(this);
        } catch (ParsingException e) {
            reset();
            throw e;
        }
        clear();
        return result;
    }
}
