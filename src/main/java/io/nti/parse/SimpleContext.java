package io.nti.parse;

import java.util.Stack;

import lombok.RequiredArgsConstructor;

/**
 * @author Jeff Hutchins
 */
@RequiredArgsConstructor
public class SimpleContext implements Context {

    private int ptr = 0;
    private final Stack<Integer> marks = new Stack<>();

    private final CharSequence sequence;

    @Override
    public int pos() {
        return ptr;
    }

    @Override
    public int mark() {
        marks.push(ptr);
        return marks.size();
    }

    @Override
    public int reset() {
        ptr = marks.pop();
        return marks.size();
    }

    @Override
    public int clear() {
        marks.pop();
        return marks.size();
    }

    protected int inc(int length) {
        ptr += length;
        return ptr;
    }

    @Override
    public CharSequence peek(int length) {
        final int min = Math.min(sequence.length() - pos(), length);
        return sequence.subSequence(pos(), min + pos());
    }

    @Override
    public CharSequence advance(int length) {
        final CharSequence result = peek(length);
        inc(result.length());
        return result;
    }

    @Override
    public int left() {
        return sequence.length() - pos();
    }

    @Override
    public String toString() {
        return sequence.subSequence(pos(), sequence.length()).toString();
    }
}
