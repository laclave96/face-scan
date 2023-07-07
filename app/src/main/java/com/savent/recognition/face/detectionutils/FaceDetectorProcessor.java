/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.savent.recognition.face.detectionutils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;


/**
 * Face Detector Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectorProcessor";

    private final FaceDetector detector;
    private List<Face> faces;

    private OnCompletionListener _listener = null;

    public interface OnCompletionListener {
        void onSuccess(List<Face> faces);
        void onFailure(Exception e);
    }

    public void addCompletionListener(OnCompletionListener listener) {
        _listener = listener;
    }
    public void removeCompletionListener() {
        _listener = null;
    }

    public FaceDetectorProcessor(Context context) {
        this(
                context,
                new FaceDetectorOptions.Builder()
                        .build());
    }

    public FaceDetectorProcessor(Context context, FaceDetectorOptions options) {
        super(context);
        Log.v(MANUAL_TESTING_LOG, "Face detector options: " + options);
        detector = FaceDetection.getClient(options);
    }


    @Override
    public void stop() {
        super.stop();
        detector.close();
    }


    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected  void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
        this.faces = faces;
        for (Face face : faces) {
            graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
        }
        if(_listener != null) _listener.onSuccess(faces);
    }

    @Override
    public List<Face> getFaces() {
        return faces;
    }


    @Override
    protected void onFailure(@NonNull Exception e) {
        if(_listener != null) _listener.onFailure(e);
    }
}
