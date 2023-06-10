package com.harish.test.opencvcamera;

/**
 * Created by hdv98 on 1/14/2018.
 */

@SuppressWarnings("JniMissingFunction")
public class NativeClass {
    public native static void faceDetection(long addRGBA);
    public native static String getMessageFromJNI();
}
