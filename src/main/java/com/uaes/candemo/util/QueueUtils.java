package com.uaes.candemo.util;



import java.util.Vector;


public class QueueUtils {
    private static Vector<Byte> queue;

    private static int mMaxFrame = 0;
    private static final int BUCKET_SIZE = 22;
//    private static final int BUCKET_SIZE = 120;
    private static final int FRAME_SIZE = 20;
    private static int DEFAULT_MAX_SIZE = 10 * 1024 * 1024 / FRAME_SIZE; // 10MB

    private static final String SP_NAME = "iot";
    private static final String KEY_QUEUE = "queue";

    public static void init() {
        init(DEFAULT_MAX_SIZE);
    }

    public static void init(int maxFrame) {
        mMaxFrame = maxFrame;
        queue = new Vector<>();
    }


    public static void add(byte[] item) {
        if ( queue.size() >= mMaxFrame * FRAME_SIZE) {
            return;
        }

        for (byte i : item) {
            queue.add(i);
        }

    }

    public static boolean canTake() {
        return queue.size() >= BUCKET_SIZE * FRAME_SIZE;
    }

    public static byte[] take() {
        int size = Math.min(BUCKET_SIZE, queue.size() / FRAME_SIZE);
        byte[] tmpBuffer = new byte[size * FRAME_SIZE];
        for (int i = 0; i < tmpBuffer.length; i++) {
            tmpBuffer[i] = queue.remove(0);
        }
        System.out.print("Take " + size + " frames, remain " + queue.size() + "bytes");
        return tmpBuffer;
    }
}
