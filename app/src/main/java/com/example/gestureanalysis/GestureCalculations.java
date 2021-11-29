package com.example.gestureanalysis;


import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.util.stream.IntStream;

public class GestureCalculations {
    private static final String TAG = "GestureCalculations";
    int[][] landmarkMatrix;
    int _digit = -1;
    int[] _digits;
    int _current_index;

    GestureCalculations() {
        landmarkMatrix = new int[21][2];
        _digits = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        _current_index = 0;
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
        int width = result.inputBitmap().getWidth();
        int height = result.inputBitmap().getHeight();
        for (int i = 0; i < 21; i++) {
            hand_position = result.multiHandLandmarks().get(0).getLandmarkList().get(i);
            landmarkMatrix[i][0] = (int) (hand_position.getX() * width);
            landmarkMatrix[i][1] = (int) (hand_position.getY() * height);
        }
    }

    public void detectDigit(HandsResult result, boolean isCamera) {
        if (result.multiHandLandmarks().isEmpty()) return;
        updateLandmarkMatrix(result);
        int[] tipids = new int[]{4, 8, 12, 16, 20};
        int[] fingerlist = new int[]{0, 0, 0, 0, 0};
        //thumb and dealing with flipping of hands
        if (landmarkMatrix[5][0] > landmarkMatrix[17][0]) {
            // thumb to right
            if (landmarkMatrix[tipids[0]][0] > landmarkMatrix[tipids[0] - 1][0])
                fingerlist[0] = 1;

            if (landmarkMatrix[9][1] > landmarkMatrix[0][1]) {
                //to_top
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipids[id]][1] > landmarkMatrix[tipids[id] - 2][1])
                        fingerlist[id] = 1;
                    else
                        fingerlist[id] = 0;
                }
            } else {
                //o_bottom
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipids[id]][1] < landmarkMatrix[tipids[id] - 2][1])
                        fingerlist[id] = 1;
                    else
                        fingerlist[id] = 0;
                }
            }
        } else {
            //thumb to left
            if (landmarkMatrix[tipids[0]][0] < landmarkMatrix[tipids[0] - 1][0])
                fingerlist[0] = 1;

            if (landmarkMatrix[9][1] > landmarkMatrix[0][1]) {
                //to_top
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipids[id]][1] > landmarkMatrix[tipids[id] - 2][1])
                        fingerlist[id] = 1;
                    else
                        fingerlist[id] = 0;
                }
            } else {
                //to_bottom
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipids[id]][1] < landmarkMatrix[tipids[id] - 2][1])
                        fingerlist[id] = 1;
                    else
                        fingerlist[id] = 0;
                }
            }
        }

        int fingercount = -1;
        int finger_sum = fingerlist[0] + fingerlist[1] + fingerlist[2] + fingerlist[3] + fingerlist[4];
        if (finger_sum == 0)
            fingercount = 0;
        else if (finger_sum == 1 && fingerlist[1] == 1)
            fingercount = 1;
        else if (finger_sum == 2 && fingerlist[1] == 1 && fingerlist[2] == 1)
            fingercount = 2;
        else if (finger_sum == 3 && fingerlist[0] == 1 && fingerlist[1] == 1 && fingerlist[2] == 1)
            fingercount = 3;
        else if (finger_sum == 4 && fingerlist[1] == 1 && fingerlist[2] == 1 && fingerlist[3] == 1 && fingerlist[4] == 1)
            fingercount = 4;
        else if (finger_sum == 5 && fingerlist[0] == 1 && fingerlist[1] == 1 && fingerlist[2] == 1 && fingerlist[3] == 1 && fingerlist[4] == 1)
            fingercount = 5;
        else if (finger_sum == 3 && fingerlist[1] == 1 && fingerlist[2] == 1 && fingerlist[3] == 1)
            fingercount = 6;
        else if (finger_sum == 3 && fingerlist[1] == 1 && fingerlist[2] == 1 && fingerlist[4] == 1)
            fingercount = 7;
        else if (finger_sum == 3 && fingerlist[1] == 1 && fingerlist[3] == 1 && fingerlist[4] == 1)
            fingercount = 8;
        else if (finger_sum == 3 && fingerlist[2] == 1 && fingerlist[3] == 1 && fingerlist[4] == 1)
            fingercount = 9;

        _digits[_current_index] = fingercount;
        if(_current_index >= 9)
            _current_index = 0;
        else
            _current_index++;

        int count = 1, tempCount;
        int popular = _digits[0];
        int temp = 0;
        for (int i = 0; i < (_digits.length - 1); i++)
        {
            temp = _digits[i];
            tempCount = 0;
            for (int j = 1; j < _digits.length; j++)
            {
                if (temp == _digits[j])
                    tempCount++;
            }
            if (tempCount > count)
            {
                popular = temp;
                count = tempCount;
            }
        }
        _digit = popular;
    }
}
