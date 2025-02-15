/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.p2pandsfu.utils;

import org.webrtc.FileVideoCapturer;

import java.io.IOException;

import owt.base.Stream;
import owt.base.VideoCapturer;

public class OwtFileVideoCapturer extends FileVideoCapturer implements VideoCapturer {

    public OwtFileVideoCapturer(String inputFile) throws IOException {
        super(inputFile);
    }

    @Override
    public int getWidth() {
        // ignored
        return 0;
    }

    @Override
    public int getHeight() {
        // ignored
        return 0;
    }

    @Override
    public int getFps() {
        return 30;
    }

    @Override
    public Stream.StreamSourceInfo.VideoSourceInfo getVideoSource() {
        return Stream.StreamSourceInfo.VideoSourceInfo.RAW_FILE;
    }
}
