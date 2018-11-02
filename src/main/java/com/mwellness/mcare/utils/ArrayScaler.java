package com.mwellness.mcare.utils;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

/**
 * Created by dev01 on 6/28/17.
 */
public class ArrayScaler {

        /**
         * Interpolate to create a new scaled vector of length newArrayLength
         * @param originalArr input vector
         * @param newArrayLength the desired vector length
         * @return a new vector of length newArrayLength created by interpolating originalArr
         */
        public static short[] scaleArray(short[] originalArr, int newArrayLength) {

            double xd[] = new double[originalArr.length];
            for(int i = 0; i < originalArr.length; i++) {
                xd[i] = (double) originalArr[i];
            }

            double[] indices = new double[originalArr.length];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = (((double) i) / (originalArr.length - 1));
            }

            UnivariateInterpolator interpolator = new SplineInterpolator();
            UnivariateFunction interp = interpolator.interpolate(indices, xd);

            short[] stretched = new short[newArrayLength];
            for (int i = 0; i < newArrayLength; i++) {
                stretched[i] = (short) interp.value(((double) i) / newArrayLength);
            }
            return stretched;
        }


}
