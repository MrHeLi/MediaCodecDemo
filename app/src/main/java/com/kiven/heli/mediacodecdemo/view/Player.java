package com.kiven.heli.mediacodecdemo.view;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Player extends Thread {
    private MediaExtractor extractor;
    private MediaCodec codec;
    private String playerSource;
    private Surface surface;

    public Player(Surface surface, String playPath) {
        this.surface = surface;
        this.playerSource = playPath;
    }

    @Override
    public void run() {
        super.run();
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(playerSource);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat trackFormat = extractor.getTrackFormat(i);
            String mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);

                try {
                    codec = MediaCodec.createDecoderByType(mime);
                    codec.configure(trackFormat, surface,
                            null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (codec == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        codec.start();//启动media codec 等待传入数据。

        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean isEos = false;
        long startMs = System.currentTimeMillis();

        while(!Thread.interrupted()) {
            if (!isEos) {
                int bufferIndex = codec.dequeueInputBuffer(10000);
                if (bufferIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[bufferIndex];
                    int sampleData = extractor.readSampleData(buffer, 0);

                    if (sampleData < 0) {
                        //流读取完毕
                        codec.queueInputBuffer(bufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEos = true;
                    } else {
                        codec.queueInputBuffer(bufferIndex, 0, sampleData,
                                extractor.getSampleTime(), MediaCodec.CRYPTO_MODE_UNENCRYPTED);
                        extractor.advance();
                    }
                }

                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        outputBuffers = codec.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d("DecodeActivity", "New format " + codec.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        ByteBuffer buffer = outputBuffers[outputBufferIndex];

                        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        codec.releaseOutputBuffer(outputBufferIndex, true);
                        break;
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }
        }

        codec.stop();
        codec.release();
        extractor.release();


    }
}


