package io.nti.parse;

/**
 * @author Jeff Hutchins
 */
public class SimpleContextTest extends ContextTest {
    @Override
    public Context getContext(String data) {
        return new SimpleContext(data);
    }
}
