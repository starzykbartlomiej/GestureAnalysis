package com.example.gestureanalysis;


import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandsResult;

public class GestureCalculations {
    private static final String TAG = "GestureCalculations";
    double[][] landmarkMatrix;
    GestureCalculations(){
        landmarkMatrix = new double[21][3];
    }

    public void logLandmarkPosition(HandsResult result, boolean showPixelValues, int position) {
        if (result.multiHandLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.NormalizedLandmark hand_position =
                result.multiHandLandmarks().get(0).getLandmarkList().get(position);

        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            int width = result.inputBitmap().getWidth();
            int height = result.inputBitmap().getHeight();
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Hand %d coordinates (pixel values): x=%f, y=%f",
                            position, hand_position.getX() * width, hand_position.getY() * height));
        } else {
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Hand %d normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                            position, hand_position.getX(), hand_position.getY()));
        }

        // WorldLandmarks
        if (result.multiHandWorldLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.Landmark wristWorldLandmark =
                result.multiHandWorldLandmarks().get(0).getLandmarkList().get(position);
        Log.i(
                TAG,
                String.format(
                        "MediaPipe Hand %d world coordinates (in meters with the origin at the hand's"
                                + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                        position, wristWorldLandmark.getX(), wristWorldLandmark.getY(), wristWorldLandmark.getZ()));
    }

    private void updateLandmarkMatrix(HandsResult result) {
        LandmarkProto.NormalizedLandmark hand_position;
        for (int i = 0; i < 21; i++) {
            hand_position = result.multiHandLandmarks().get(0).getLandmarkList().get(i);
            landmarkMatrix[i][0] = hand_position.getX();
            landmarkMatrix[i][1] = hand_position.getY();
            landmarkMatrix[i][2] = hand_position.getZ();
        }
    }

    public void detectDigit(HandsResult result, boolean isCamera) {
        if (result.multiHandLandmarks().isEmpty()) return;
        updateLandmarkMatrix(result);

        double[] p0 = new double[]{landmarkMatrix[0][1], landmarkMatrix[0][2]};

        double[] p5 = new double[]{landmarkMatrix[5][1], landmarkMatrix[5][2]};
        double[] p9 = new double[]{landmarkMatrix[9][1], landmarkMatrix[9][2]};
        double[] p13 = new double[]{landmarkMatrix[13][1], landmarkMatrix[13][2]};
        double[] p17 = new double[]{landmarkMatrix[17][1], landmarkMatrix[17][2]};

        double[] p8 = new double[]{landmarkMatrix[8][1], landmarkMatrix[8][2]};
        double[] p12 = new double[]{landmarkMatrix[12][1], landmarkMatrix[12][2]};
        double[] p16 = new double[]{landmarkMatrix[16][1], landmarkMatrix[16][2]};
        double[] p20 = new double[]{landmarkMatrix[20][1], landmarkMatrix[20][2]};

        double[] p4 = new double[]{landmarkMatrix[4][1], landmarkMatrix[4][2]};


        int thumb_f = euclideanLength(p4, p5) / euclideanLength(p5, p0) * 100 > 65 ? 1 : 0;
        int index_f = euclideanLength(p5, p8) / euclideanLength(p5, p0) * 100 > 65 ? 1 : 0;
        int middle_f = euclideanLength(p12, p9) / euclideanLength(p9, p0) * 100 > 65 ? 1 : 0;
        int ring_f = euclideanLength(p16, p13) / euclideanLength(p13, p0) * 100 > 65 ? 1 : 0;
        int pinky_f = euclideanLength(p20, p17) / euclideanLength(p17, p0) * 100 > 65 ? 1 : 0;

        int digit = thumb_f + index_f + middle_f + ring_f + pinky_f;

        Log.i(TAG, String.format("Detected: %d", digit));
    }

    private double euclideanLength(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

}
