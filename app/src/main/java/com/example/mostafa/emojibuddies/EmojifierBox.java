package com.example.mostafa.emojibuddies;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.IOException;
import java.util.List;

/**
 * Created by Mostafa on 4/5/2018.
 */

public class EmojifierBox {
    // This class uses Mobile Vision Library to : detect faces ,
    // recognize the facial expression , iterate over detected faces
    // and place the appropriate emojis on them .


    /*
    *
    * A Simple function that determines whether a mouth is opened or closed based on a given faces
    * @param face Face
    * @return a boolean which determined whether mouth is opened or closed .
    * */
    private static final String LOG_TAG = EmojifierBox.class.getSimpleName();
    private static boolean isTheMouthOpenedOrClosed(Face face) {
        if ((contains(face.getLandmarks(), Landmark.RIGHT_MOUTH) != 99)
                && (contains(face.getLandmarks(), Landmark.BOTTOM_MOUTH) != 99)
                && (contains(face.getLandmarks(), Landmark.LEFT_MOUTH) != 99)
                ) {
            //for bottom mouth
            int cBottomMouthY = (int) (face.getLandmarks().get(contains(face.getLandmarks(),Landmark.BOTTOM_MOUTH )).getPosition().y);
            //for Left mouth
            int cLeftMouthY = (int) (face.getLandmarks().get(contains(face.getLandmarks(), Landmark.LEFT_MOUTH)).getPosition().y);
            //for Right mouth
            int cRightMouthY = (int) (face.getLandmarks().get(contains(face.getLandmarks(), Landmark.RIGHT_MOUTH)).getPosition().y);
            float centerPointY = (float) (((cLeftMouthY + cRightMouthY) / 2) - 0.06*face.getHeight());
            float differenceY = cBottomMouthY - centerPointY;
            return differenceY > 0.12 * face.getHeight();
        }
        return false;
    }
    private static int contains(List<Landmark> list, int name) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == name) {
                return i;
            }
        }
        return 99;
    }
    private enum EmojiDecision {
        // Emojis that can be detected  according to whether eyes are opened , closed or winking
        // , mouth is opened or closed and the face is smiling or frown .
        EYES_OPENED_MOUTH_OPENED_SMILING,
        EYES_OPENED_MOUTH_OPENED_FROWN,
        EYES_OPENED_MOUTH_CLOSED_SMILING,
        EYES_OPENED_MOUTH_CLOSED_FROWN,
        EYES_CLOSED_MOUTH_OPENED_SMILING,
        EYES_CLOSED_MOUTH_OPENED_FROWN,
        EYES_CLOSED_MOUTH_CLOSED_SMILING,
        EYES_CLOSED_MOUTH_CLOSED_FROWN,
        EYES_WINKING_MOUTH_OPENED_SMILING,
        EYES_WINKING_MOUTH_OPENED_FROWN,
        EYES_WINKING_MOUTH_CLOSED_SMILING,
        EYES_WINKING_MOUTH_CLOSED_FROWN
    }

    private static EmojiDecision determineAppropriateEmoji(Face face) {
        boolean theFaceIsSmiling = face.getIsSmilingProbability() > 0.2;
        int eyesOpenedClosedOrWinking = determineEyesState(face.getIsLeftEyeOpenProbability() < 0.5
                ,face.getIsRightEyeOpenProbability() < 0.5);
        boolean theMouthisOpened = isTheMouthOpenedOrClosed(face);
        EmojiDecision emoji=null;
        if(theFaceIsSmiling) {
        switch (eyesOpenedClosedOrWinking) {
            // Eyes opened
            case 0 : if (theMouthisOpened) emoji = EmojiDecision.EYES_OPENED_MOUTH_OPENED_SMILING;
            else emoji=EmojiDecision.EYES_OPENED_MOUTH_CLOSED_SMILING;
            break;
            // Eyes closed
            case 1 : if (theMouthisOpened) emoji = EmojiDecision.EYES_CLOSED_MOUTH_OPENED_SMILING;
            else emoji=EmojiDecision.EYES_CLOSED_MOUTH_CLOSED_SMILING;
            break;
            //Eyes winking
            case 2 : if (theMouthisOpened) emoji = EmojiDecision.EYES_WINKING_MOUTH_OPENED_SMILING;
            else emoji=EmojiDecision.EYES_WINKING_MOUTH_CLOSED_SMILING;
            break;
        }
        } else {
        switch (eyesOpenedClosedOrWinking){
            // Eyes opened
            case 0 : if (theMouthisOpened) emoji = EmojiDecision.EYES_OPENED_MOUTH_OPENED_FROWN;
            else emoji=EmojiDecision.EYES_OPENED_MOUTH_CLOSED_FROWN;
            break;
            // Eyes closed
            case 1 : if (theMouthisOpened) emoji = EmojiDecision.EYES_CLOSED_MOUTH_OPENED_FROWN;
            else emoji=EmojiDecision.EYES_CLOSED_MOUTH_CLOSED_FROWN;
            break;
            // Eyes winking
            case 2 : if (theMouthisOpened) emoji = EmojiDecision.EYES_WINKING_MOUTH_OPENED_FROWN;
            else emoji=EmojiDecision.EYES_WINKING_MOUTH_CLOSED_FROWN;
            break;
        }
        }
        // Log the chosen Emoji
        Log.d(LOG_TAG, "emoji : " + emoji.name());
        return emoji;
    }
    private static int determineEyesState(boolean leftEyeClosed, boolean rightEyeClosed) {
        // true means the eye is closed
        if ((leftEyeClosed && !rightEyeClosed) || (rightEyeClosed && !leftEyeClosed)) return 2;
        else if (leftEyeClosed) return 1;
        else return 0;
    }
    static Bitmap detectFacesandPutEmojisonThem(Context context, Bitmap picture) throws IOException {
        // Create the face detector, disable tracking and enable classifications
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(picture).build();
        SparseArray<Face> faces = detector.detect(frame);
        Log.d(LOG_TAG, "detectFaces: number of faces = " + faces.size());
        // Initialize result bitmap to original picture
        Bitmap resultBitmap = picture;
        if(!detector.isOperational())Toast.makeText(context, R.string.face_detector_not_operational,
                Toast.LENGTH_SHORT).show();
        else if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        } else {
            // Iterate through the faces
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                Bitmap emojiBitmap=null;
                SharedPreferences sharedPreferences=context
                        .getSharedPreferences(SelectEmojiSet.SHARED_PREF_KEY,Context.MODE_PRIVATE);
                String emojiSetName=sharedPreferences.getString(SelectEmojiSet.RADIO_BUTTON_TAG_KEY,"EmojiOne");
                String emojiSet=emojiSetName+"/";
                switch (determineAppropriateEmoji(face)) {
                    case EYES_OPENED_MOUTH_OPENED_SMILING :
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Grinning_Face_With_Big_Eyes.png"));
                        break;
                    case EYES_OPENED_MOUTH_OPENED_FROWN :
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Frowning_Face_With_Open_Mouth.png"));
                        break;
                    case EYES_OPENED_MOUTH_CLOSED_SMILING :
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Slightly_Smiling_Face.png"));
                        break;
                    case EYES_OPENED_MOUTH_CLOSED_FROWN:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Worried_Face.png"));
                        break;
                    case EYES_CLOSED_MOUTH_OPENED_SMILING:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Grinning_Face_With_Smiling_Eyes.png"));
                        break;
                    case EYES_CLOSED_MOUTH_OPENED_FROWN:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Weary_Face.png"));
                        break;
                    case EYES_CLOSED_MOUTH_CLOSED_SMILING:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Smiling_Face_With_Smiling_Eyes.png"));
                        break;
                    case EYES_CLOSED_MOUTH_CLOSED_FROWN:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Persevering_Face.png"));
                        break;
                    case EYES_WINKING_MOUTH_OPENED_SMILING:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Winking_Face_With_Tongue.png"));
                        break;
                    case EYES_WINKING_MOUTH_OPENED_FROWN:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Winking_Face_With_Tongue.png"));
                        break;
                    case EYES_WINKING_MOUTH_CLOSED_SMILING:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Winking_Face.png"));
                        break;
                    case EYES_WINKING_MOUTH_CLOSED_FROWN:
                        emojiBitmap = BitmapFactory.decodeStream(context.getAssets().
                                open(emojiSet+"Winking_Face.png"));
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                        // TODO : Open gallery and make him choose one and the result should return in resultBitmap
                }
                // Add the emojiBitmap to the proper position in the original image
                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
            }
        }
        // Release the detector
        detector.release();
        return resultBitmap;
    }
    public static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());
        // An appropriate scale factor to scale the emoji so it looks better on face .
        float scaleFactor = .9f;
        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);
        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);
        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;
        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);
        return resultBitmap;
    }

}
