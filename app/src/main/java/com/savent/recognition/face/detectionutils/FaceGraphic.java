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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.savent.recognition.face.R;

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
  private static final float BOX_STROKE_WIDTH = 5.0f;
  private final Paint facePositionPaint;
  private final Paint boxPaints;

  private volatile Face face;

  FaceGraphic(GraphicOverlay overlay, Face face) {
    super(overlay);

    this.face = face;
    int color = overlay.getContext().getColor(R.color.primary_blue);

    facePositionPaint = new Paint();
    facePositionPaint.setColor(color);
    boxPaints = new Paint();
    boxPaints.setColor(color/* background color */);
    boxPaints.setStyle(Paint.Style.STROKE);
    boxPaints.setStrokeWidth(BOX_STROKE_WIDTH);

  }

  /** Draws the face annotations for position on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    Face face = this.face;
    if (face == null) {
      return;
    }

    Rect rect = new Rect(face.getBoundingBox());
    canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, boxPaints);

  }


}
