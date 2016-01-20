package com.gDyejeekis.aliencompanion.Utils;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by George on 8/6/2015.
 */
public class MyHtmlTagHandler implements Html.TagHandler {

    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    Stack<String> lists = new Stack<String>();
    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    Stack<Integer> olNextIndex = new Stack<Integer>();
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);

    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if(tag.equalsIgnoreCase("del")) {
            processStrike(opening, output);
        }
        else if(tag.equals("ul")) {
            if (opening) {
                lists.push(tag);
            } else {
                lists.pop();
            }
        }
        else if(tag.equals("ol")) {
            if (opening) {
                lists.push(tag);
                olNextIndex.push(Integer.valueOf(1)).toString();//TODO: add support for lists starting other index than 1
            } else {
                lists.pop();
                olNextIndex.pop().toString();
            }
        }
        else if(tag.equals("li")) {
            handleListTag(opening, output);
        }
    }

    private void handleListTag(boolean opening, Editable output) {
        if (opening) {
            if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                output.append("\n");
            }
            String parentList = lists.peek();
            if (parentList.equalsIgnoreCase("ol")) {
                start(output, new Ol());
                output.append(olNextIndex.peek().toString() + ". ");
                olNextIndex.push(Integer.valueOf(olNextIndex.pop().intValue() + 1));
            } else if (parentList.equalsIgnoreCase("ul")) {
                start(output, new Ul());
            }
        } else {
            if (lists.peek().equalsIgnoreCase("ul")) {
                if ( output.length() > 0 && output.charAt(output.length() - 1) != '\n' ) {
                    output.append("\n");
                }
                // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                int bulletMargin = indent;
                if (lists.size() > 1) {
                    bulletMargin = indent-bullet.getLeadingMargin(true);
                    if (lists.size() > 2) {
                        // This get's more complicated when we add a LeadingMarginSpan into the same line:
                        // we have also counter it's effect to BulletSpan
                        bulletMargin -= (lists.size() - 2) * listItemIndent;
                    }
                }
                BulletSpan newBullet = new BulletSpan(bulletMargin);
                end(output,
                        Ul.class,
                        new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                        newBullet);
            } else if (lists.peek().equalsIgnoreCase("ol")) {
                if ( output.length() > 0 && output.charAt(output.length() - 1) != '\n' ) {
                    output.append("\n");
                }
                int numberMargin = listItemIndent * (lists.size() - 1);
                if (lists.size() > 2) {
                    // Same as in ordered lists: counter the effect of nested Spans
                    numberMargin -= (lists.size() - 2) * listItemIndent;
                }
                end(output,
                        Ol.class,
                        new LeadingMarginSpan.Standard(numberMargin));
            }
        }
    }

    /** @see android.text.Html */
    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /** Modified from {@link android.text.Html} */
    private static void end(Editable text, Class<?> kind, Object... replaces) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len) {
            for (Object replace: replaces) {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return;
    }

    /** @see android.text.Html */
    private static Object getLast(Spanned text, Class<?> kind) {
		/*
		 * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    private void processStrike(boolean opening, Editable output) {
        int len = output.length();
        if(opening) {
            output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, StrikethroughSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * enable this method for processStrike() if needed
     */
    //private Object getLast(Editable text, Class kind) {
    //    Object[] objs = text.getSpans(0, text.length(), kind);
//
    //    if (objs.length == 0) {
    //        return null;
    //    } else {
    //        for(int i = objs.length;i>0;i--) {
    //            if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
    //                return objs[i-1];
    //            }
    //        }
    //        return null;
    //    }
    //}

    private static class Ul { }
    private static class Ol { }
}
