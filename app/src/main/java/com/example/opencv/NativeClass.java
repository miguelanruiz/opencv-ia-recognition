package com.example.opencv;

/**
 * Created by Dell on 24/01/18.
 */

@SuppressWarnings("JniMissingFunction")
public class NativeClass {
    public native static void openCVVersion();
    public native static void train(long[] imageMats, int numImages, String internalDirPath);
    public native static long compareAndMarkOwner(long addrFrame);
}
