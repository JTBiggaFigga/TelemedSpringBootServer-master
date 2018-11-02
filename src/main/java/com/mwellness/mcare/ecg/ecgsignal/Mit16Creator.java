package com.mwellness.mcare.ecg.ecgsignal;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.utils.ByteArrayUtils;

/**
 * Created by dev01 on 9/19/2016.
 */
public class Mit16Creator {

    public static final int CH1 = 1;
    public static final int CH2 = 2;

    private static void log(String str) {
        AMainApp.log(Mit16Creator.class.getSimpleName() + ": " + str);
    }


    public static byte[] createMit16Format(final short[] ch1ShortArr, final short[] ch2ShortArr) {
        return createMit16Format(ByteArrayUtils.toByteArray(ch1ShortArr), ByteArrayUtils.toByteArray(ch2ShortArr));
    }

    /**
     * Create an MIT16 Frame from input bytes
     * @param ch1ByteArr
     * @param ch2ByteArr
     * @return
     * @throws IllegalArgumentException
     */
    public static byte[] createMit16Format(final byte[] ch1ByteArr, final byte[] ch2ByteArr) throws IllegalArgumentException {

        if(ch1ByteArr.length != ch2ByteArr.length) {
            throw new IllegalArgumentException("both channels should be same length");
        }

        byte[] out = new byte[ch1ByteArr.length + ch2ByteArr.length];

        for(int i = 0, j = 0; i < ch1ByteArr.length; i += 2, j += 4) {

            // ch1ShortArr
            out[j + 0] = ch1ByteArr[i + 0];
            out[j + 1] = ch1ByteArr[i + 1];

            // ch2ShortArr
            out[j + 2] = ch2ByteArr[i + 0];
            out[j + 3] = ch2ByteArr[i + 1];
        }

        return out;

    }


    /**
     * To extract channel information from MIT-16 Data
     * @param mit16ByteArr
     * @param channel can be one from @Mit16Creator.CH1 or @Mit16Creator.CH2
     * @return
     */
    public static short[] extractChannel(final byte[] mit16ByteArr, int channel) {

        log("Extracting MIT16: " + mit16ByteArr.length + " total bytes");

        short[] channelData = new short[mit16ByteArr.length / ((Short.SIZE/8) * 2)];

        for(int i = 0, j = 0; i < mit16ByteArr.length; i += 4, j++) {

            if(channel == CH1) {

                byte low = mit16ByteArr[i + 1];
                byte high = mit16ByteArr[i + 0];

                channelData[j] = ByteArrayUtils.toShort(low, high);
            }
            else if(channel == CH2) {

                byte low = mit16ByteArr[i + 3];
                byte high = mit16ByteArr[i + 2];

                channelData[j] = ByteArrayUtils.toShort(low, high);
            }
        }

        return channelData;
    }
}
