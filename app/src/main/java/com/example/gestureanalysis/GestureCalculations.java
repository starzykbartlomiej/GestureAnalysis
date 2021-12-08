package com.example.gestureanalysis;

import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandsResult;

public class GestureCalculations {
    final int[][] landmarkMatrix;
    int _digit = -1;
    int[] _digits;
    int _current_index;

    GestureCalculations() {
        landmarkMatrix = new int[21][2];
        _digits = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        _current_index = 0;
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
        if (result.multiHandLandmarks().isEmpty()) {
            _digit = -1;
            return;
        }
        updateLandmarkMatrix(result);
        int[] tipIds = new int[]{4, 8, 12, 16, 20};
        int[] fingerList = new int[]{0, 0, 0, 0, 0};
        // thumb to right
        if (landmarkMatrix[5][0] > landmarkMatrix[17][0]) {
            // thumb to right
            if (landmarkMatrix[tipIds[0]][0] > landmarkMatrix[tipIds[0] - 2][0] ||
                    landmarkMatrix[tipIds[0]][0] > landmarkMatrix[5][0])
                fingerList[0] = 1;

            if (landmarkMatrix[9][1] > landmarkMatrix[0][1]) {
                //to_top
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipIds[id]][1] > landmarkMatrix[tipIds[id] - 2][1])
                        fingerList[id] = 1;
                    else
                        fingerList[id] = 0;
                }
            } else {
                //o_bottom
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipIds[id]][1] < landmarkMatrix[tipIds[id] - 2][1])
                        fingerList[id] = 1;
                    else
                        fingerList[id] = 0;
                }
            }
            // thumb to left
        } else if (landmarkMatrix[5][0] < landmarkMatrix[17][0]) {
            if (landmarkMatrix[tipIds[0]][0] < landmarkMatrix[tipIds[0] - 2][0] ||
                    landmarkMatrix[tipIds[0]][0] < landmarkMatrix[5][0])
                fingerList[0] = 1;

            if (landmarkMatrix[9][1] > landmarkMatrix[0][1]) {
                //to_top
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipIds[id]][1] > landmarkMatrix[tipIds[id] - 2][1])
                        fingerList[id] = 1;
                    else
                        fingerList[id] = 0;
                }
            } else {
                //to_bottom
                for (int id = 1; id < 5; id++) {
                    if (landmarkMatrix[tipIds[id]][1] < landmarkMatrix[tipIds[id] - 2][1])
                        fingerList[id] = 1;
                    else
                        fingerList[id] = 0;
                }
            }
        }


        int fingerCount = -1;
        int finger_sum = fingerList[0] + fingerList[1] + fingerList[2] + fingerList[3] + fingerList[4];
        if (finger_sum == 0)
            fingerCount = 0;
        else if (finger_sum == 1 && fingerList[1] == 1)
            fingerCount = 1;
        else if (finger_sum == 2 && fingerList[1] == 1 && fingerList[2] == 1)
            fingerCount = 2;
        else if (finger_sum == 3 && fingerList[0] == 1 && fingerList[1] == 1 && fingerList[2] == 1)
            fingerCount = 3;
        else if (finger_sum == 4 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[3] == 1
                && fingerList[4] == 1)
            fingerCount = 4;
        else if (finger_sum == 5 && fingerList[0] == 1 && fingerList[1] == 1 && fingerList[2] == 1
                && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 5;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[3] == 1)
            fingerCount = 6;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[4] == 1)
            fingerCount = 7;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 8;
        else if (finger_sum == 3 && fingerList[2] == 1 && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 9;

        if (fingerCount == -1) {
            fingerList = new int[]{0, 0, 0, 0, 0};
            if (landmarkMatrix[5][1] < landmarkMatrix[17][1]) {
                // thumb to top
                if (landmarkMatrix[tipIds[0]][1] < landmarkMatrix[tipIds[0] - 2][1] ||
                        landmarkMatrix[tipIds[0]][1] < landmarkMatrix[5][1])
                    fingerList[0] = 1;

                if (landmarkMatrix[9][0] < landmarkMatrix[0][0]) {
                    for (int id = 1; id < 5; id++) {
                        if (landmarkMatrix[tipIds[id]][0] < landmarkMatrix[tipIds[id] - 2][0])
                            fingerList[id] = 1;
                        else
                            fingerList[id] = 0;
                    }
                } else if (landmarkMatrix[9][0] > landmarkMatrix[0][0]) {
                    for (int id = 1; id < 5; id++) {
                        if (landmarkMatrix[tipIds[id]][0] > landmarkMatrix[tipIds[id] - 2][0])
                            fingerList[id] = 1;
                        else
                            fingerList[id] = 0;
                    }
                }
            } else if (landmarkMatrix[5][1] > landmarkMatrix[17][1]) {
                //thumb to bottom
                if (landmarkMatrix[tipIds[0]][1] > landmarkMatrix[tipIds[0] - 2][1] ||
                        landmarkMatrix[tipIds[0]][1] > landmarkMatrix[5][1])
                    fingerList[0] = 1;

                if (landmarkMatrix[9][0] < landmarkMatrix[0][0]) {
                    for (int id = 1; id < 5; id++) {
                        if (landmarkMatrix[tipIds[id]][0] > landmarkMatrix[tipIds[id] - 2][0])
                            fingerList[id] = 1;
                        else
                            fingerList[id] = 0;
                    }
                } else if (landmarkMatrix[9][0] > landmarkMatrix[0][0]) {
                    for (int id = 1; id < 5; id++) {
                        if (landmarkMatrix[tipIds[id]][0] < landmarkMatrix[tipIds[id] - 2][0])
                            fingerList[id] = 1;
                        else
                            fingerList[id] = 0;
                    }
                }
            }
        }

        fingerCount = -1;
        finger_sum = fingerList[0] + fingerList[1] + fingerList[2] + fingerList[3] + fingerList[4];
        if (finger_sum == 0)
            fingerCount = 0;
        else if (finger_sum == 1 && fingerList[1] == 1)
            fingerCount = 1;
        else if (finger_sum == 2 && fingerList[1] == 1 && fingerList[2] == 1)
            fingerCount = 2;
        else if (finger_sum == 3 && fingerList[0] == 1 && fingerList[1] == 1 && fingerList[2] == 1)
            fingerCount = 3;
        else if (finger_sum == 4 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[3] == 1
                && fingerList[4] == 1)
            fingerCount = 4;
        else if (finger_sum == 5 && fingerList[0] == 1 && fingerList[1] == 1 && fingerList[2] == 1
                && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 5;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[3] == 1)
            fingerCount = 6;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[2] == 1 && fingerList[4] == 1)
            fingerCount = 7;
        else if (finger_sum == 3 && fingerList[1] == 1 && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 8;
        else if (finger_sum == 3 && fingerList[2] == 1 && fingerList[3] == 1 && fingerList[4] == 1)
            fingerCount = 9;

        _digits[_current_index] = fingerCount;
        if (_current_index >= 9)
            _current_index = 0;
        else
            _current_index++;

        if (!isCamera)
            _digit = fingerCount;
        else {
            int count = 1, tempCount;
            int popular = _digits[0];
            int temp;
            for (int i = 0; i < (_digits.length - 1); i++) {
                temp = _digits[i];
                tempCount = 0;
                for (int j = 1; j < _digits.length; j++) {
                    if (temp == _digits[j])
                        tempCount++;
                }
                if (tempCount > count) {
                    popular = temp;
                    count = tempCount;
                }
            }
            _digit = popular;
        }

    }

    public void logLandmarkMatrix() {
        Log.i("5)", String.format("x=%d, y=%d", landmarkMatrix[5][0], landmarkMatrix[5][1]));
        Log.i("17", String.format("x=%d, y=%d", landmarkMatrix[17][0], landmarkMatrix[17][1]));
    }
}
