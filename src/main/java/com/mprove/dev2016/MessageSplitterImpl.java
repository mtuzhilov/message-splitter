package com.mprove.dev2016;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSplitterImpl implements MessageSplitter {

    private static final int GSM_SEGMENT_SIZE = 160;
    private static final int UTF_16_SEGMENT_SIZE = 70;
    private static final Pattern GSM_CHARACTERS_PATTERN = Pattern.compile("^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EÆæßÉ!\"#$%&amp;'()*+,\\-./:;&lt;=&gt;?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$");
    private static final String GSM_14BIT_CHARACTERS = "\f^{}\\[~]€]";

    public String[] split(final String messageIn) {
        int segmentSize = UTF_16_SEGMENT_SIZE;
        StringTokenizer stringTokenizer = new StringTokenizer(messageIn, " ");
        List<String> segments = new ArrayList<String>();
        StringBuilder currSegment = new StringBuilder();
        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();

            if (segmentSize == UTF_16_SEGMENT_SIZE) {
                Matcher matcher = GSM_CHARACTERS_PATTERN.matcher(currSegment.toString() + word);
                if (matcher.matches()) {
                    segmentSize = GSM_SEGMENT_SIZE;
                }
            }

            boolean wordPut = false;
            while (!wordPut) {
                int wordSize = word.length();

                //Charset.forName("UTF-8").newEncoder().canEncode(messageIn)
                isValidGSM(messageIn.getBytes());

                if (currSegment.length() + wordSize <= segmentSize) {
                    currSegment.append(word + " ");
                    wordPut = true;
                } else {
                    if (wordSize > segmentSize) {
                        int segmentLengthLeft = segmentSize - currSegment.length();
                        String firstWordPart = word.substring(0, segmentLengthLeft);
                        currSegment.append(firstWordPart);
                        word = word.substring(segmentLengthLeft);
                    }
                    segments.add(currSegment.toString().trim());
                    currSegment = new StringBuilder();
                }
            }
        }

        if (currSegment.length() > 0) {
            segments.add(currSegment.toString().trim());
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    public boolean isValidGSM(final byte[] bytes) {
        try {
            Charset.forName("IBM930").newDecoder().decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }

}
