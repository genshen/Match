package com.holo.sounds;

import android.Manifest;
import android.content.Context;

import net.alhazmy13.gota.Gota;
import net.alhazmy13.gota.GotaResponse;

/**
 * Created by Alhazmy13 on 12/23/15.
 */
public class MediaRecorderDialog {

    public MediaRecorderDialog() {

    }

    public static class Builder {
        public Builder(Context context,String basePath) {
            GenralAtteribute.context = context;
            GenralAtteribute.title = "";
            GenralAtteribute.message = "";
            GenralAtteribute.basePath = basePath;
            GenralAtteribute.outPutFormat = OutputFormat.MPEG_4;
            GenralAtteribute.audioEncoder = AudioEncoder.AAC;
        }

        public Builder setTitle(String title) {
            GenralAtteribute.title = title;
            return this;
        }

        public Builder setMessage(String msg) {
            GenralAtteribute.message = msg;
            return this;
        }

        public Builder show() {
            new Gota(GenralAtteribute.context).checkPermission(new String[]{Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}, new Gota.OnRequestPermissionsBack() {
                @Override
                public void onRequestBack(GotaResponse goaResponse) {
                    new SoundDialog(GenralAtteribute.context).show();
                }
            });

            return this;
        }

        public Builder setOutputFormat(int outputFormat) {
            GenralAtteribute.outPutFormat = outputFormat;
            return this;
        }

        public Builder setAudioEncoder(int audioEncoder) {
            GenralAtteribute.audioEncoder = audioEncoder;
            return this;
        }

        public Builder setOnSaveButtonClickListener(OnSaveButtonClickListener onSaveButtonClickListener) {
            GenralAtteribute.onSaveButtonClickListener = onSaveButtonClickListener;
            return this;
        }

    }

    public final class OutputFormat {
        public static final int AAC_ADTS = 6;
        public static final int AMR_NB = 3;
        public static final int AMR_WB = 4;
        public static final int DEFAULT = 0;
        public static final int MPEG_4 = 2;
        /**
         * @deprecated
         */
        @Deprecated
        public static final int RAW_AMR = 3;
        public static final int THREE_GPP = 1;
        public static final int WEBM = 9;

        OutputFormat() {
            throw new RuntimeException("Stub!");
        }
    }

    public final class AudioEncoder {
        public static final int AAC = 3;
        public static final int AAC_ELD = 5;
        public static final int AMR_NB = 1;
        public static final int AMR_WB = 2;
        public static final int DEFAULT = 0;
        public static final int HE_AAC = 4;
        public static final int VORBIS = 6;

        AudioEncoder() {
            throw new RuntimeException("Stub!");
        }
    }


}
