package com.mprove.dev2016;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MessageSplitterImpl implements MessageSplitter {

    private static final int GSM_SEGMENT_SIZE = 160;
    private static final int UTF_16_SEGMENT_SIZE = 70;

    public String[] split(final String messageIn) {
        int segmentSize = UTF_16_SEGMENT_SIZE;
        StringTokenizer stringTokenizer = new StringTokenizer(messageIn, " ");
        List<String> segments = new ArrayList<String>();
        StringBuilder currSegment = new StringBuilder();
        while (stringTokenizer.hasMoreTokens()) {
            String word = stringTokenizer.nextToken();

            boolean gsm = false;
            if (segmentSize == UTF_16_SEGMENT_SIZE) {
                if (isValidGSM(currSegment.toString() + word)) {
                    segmentSize = GSM_SEGMENT_SIZE;
                    gsm = true;
                }
            }
            boolean wordPut = false;
            while (!wordPut) {
                int wordSize;
                int currSegmentSize;
                if (gsm) {
                    wordSize = getGsmStringSize(word);
                    currSegmentSize = getGsmStringSize(currSegment.toString());
                } else {
                    wordSize = getUTF16StringSize(word);
                    currSegmentSize = getUTF16StringSize(currSegment.toString());
                }

                if (currSegmentSize + wordSize <= segmentSize) {
                    currSegment.append(word + " ");
                    wordPut = true;
                } else {
                    if (wordSize > segmentSize) {
                        int segmentLengthLeft;
                        if (gsm) {
                            segmentLengthLeft = segmentSize - currSegmentSize - (wordSize - word.length());
                        } else {
                            segmentLengthLeft = (Character.isLowSurrogate(word.charAt(segmentSize))) && segmentSize > 0 ? segmentSize - 1 : segmentSize;
                        }
                      
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

    private int getGsmStringSize(String str) {
        return str.getBytes(Charset.forName("X-Gsm7Bit")).length;
    }

    private int getUTF16StringSize(String str) {
        //using UTF-16LE little-endian, no byte-order marker added. Divide by 2 because of 2 byte characters
        return (int) Math.ceil(str.getBytes(Charset.forName("UTF-16LE")).length / 2);
    }

    private boolean isValidGSM(String str) {
        String gsmStr = new String(str.getBytes(Charset.forName("X-Gsm7Bit")), Charset.forName("X-Gsm7Bit"));
        return str.equals(gsmStr);
    }
}
