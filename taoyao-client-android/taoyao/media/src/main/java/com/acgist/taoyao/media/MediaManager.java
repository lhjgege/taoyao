package com.acgist.taoyao.media;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.acgist.taoyao.config.Config;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.Iterator;
import java.util.List;

/**
 * 媒体来源管理器
 *
 * @author acgist
 */
public class MediaManager {

    /**
     * 来源类型
     *
     * @author acgist
     */
    public enum Type {

        // 文件共享
        FILE,
        // 后置摄像头
        BACK,
        // 前置摄像头
        FRONT,
        // 屏幕共享
        SCREEN;

        /**
         * @return 是否摄像头
         */
        public boolean isCamera() {
            return this == BACK || this == FRONT;
        }


    }

    private static final MediaManager INSTANCE = new MediaManager();

    private MediaManager() {
    }

    public static final MediaManager getInstance() {
        return INSTANCE;
    }

    /**
     * 视频类型
     */
    private Type type;
    /**
     * Handler
     */
    private Handler handler;
    /**
     * 上下文
     */
    private Context context;
    /**
     * 媒体流：声音、主码流（预览流）、次码流
     */
    private MediaStream mediaStream;
    /**
     * 视频捕获
     * FileVideoCapturer
     * CameraVideoCapturer
     * ScreenCapturerAndroid
     */
    private VideoCapturer videoCapturer;
    /**
     * Peer连接工厂
     */
    private PeerConnectionFactory peerConnectionFactory;

    static {
        // 设置采样
//      WebRtcAudioUtils.setDefaultSampleRateHz();
        // 噪声消除
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        // 回声小丑
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
    }

    /**
     * 加载媒体流
     *
     * @param context 上下文
     */
    public void init(Context context) {
        this.type = Type.BACK;
        this.context = context;
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this.context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        );
        this.peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
        this.mediaStream = this.peerConnectionFactory.createLocalMediaStream("ARDAMS");
    }

    /**
     * 切换视频来源
     *
     * @param type 来源类型
     *
     * TODO：设置分享
     */
    public void exchange(Type type) {
        if(this.type == type) {
            return;
        }
        this.type = type;
        Log.i(MediaManager.class.getSimpleName(), "设置视频来源：" + type);
        if(this.type.isCamera() && type.isCamera()) {
            // TODO：测试是否需要完全重置
            final CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) this.videoCapturer;
            cameraVideoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean success) {
                }
                @Override
                public void onCameraSwitchError(String message) {
                }
            });
        } else {
            this.initVideo();
        }
    }

    /**
     * 加载音频
     */
    public void initAudio() {
        // 关闭音频
        this.closeAudioTrack();
        // 加载音频
        final MediaConstraints mediaConstraints = new MediaConstraints();
        final AudioSource audioSource = this.peerConnectionFactory.createAudioSource(mediaConstraints);
        final AudioTrack audioTrack = this.peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
        audioTrack.setEnabled(true);
        this.mediaStream.addTrack(audioTrack);
        Log.i(MediaManager.class.getSimpleName(), "加载音频：" + audioTrack.id());
    }

    /**
     * 加载视频
     */
    public void initVideo() {
        this.closeVideoTrack();
        if(this.videoCapturer != null) {
            this.videoCapturer.dispose();
        }
        if(this.type.isCamera()) {
            final CameraEnumerator cameraEnumerator = new Camera2Enumerator(this.context);
            final String[] names = cameraEnumerator.getDeviceNames();
            for(String name : names) {
                if(this.type == Type.FRONT && cameraEnumerator.isFrontFacing(name)) {
                    Log.i(MediaManager.class.getSimpleName(), "加载视频（前置摄像头）：" + name);
                    this.videoCapturer = cameraEnumerator.createCapturer(name, new MediaCameraEventsHandler());
                } else if(this.type == Type.BACK && cameraEnumerator.isBackFacing(name)) {
                    Log.i(MediaManager.class.getSimpleName(), "加载视频（后置摄像头）：" + name);
                    this.videoCapturer = cameraEnumerator.createCapturer(name, new MediaCameraEventsHandler());
                } else {
                    Log.d(MediaManager.class.getSimpleName(), "忽略摄像头：" + name);
                }
            }
        } else if(this.type == Type.FILE) {
            Log.i(MediaManager.class.getSimpleName(), "加载视频（文件）");
        } else if(this.type == Type.SCREEN) {
            Log.i(MediaManager.class.getSimpleName(), "加载视频（录屏）");
            final MediaProjectionManager mediaProjectionManager = this.context.getSystemService(MediaProjectionManager.class);
            final Intent intent = mediaProjectionManager.createScreenCaptureIntent();
            final Message message = Message.obtain(this.handler, () -> {
                this.videoCapturer = new ScreenCapturerAndroid(intent, new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        super.onStop();
                        Log.i(MediaManager.class.getSimpleName(), "停止屏幕捕获");
                    }
                });
            });
            message.obj = intent;
            message.what = Config.WHAT_SCREEN_CAPTURE;
            this.handler.sendMessage(message);
//          this.handler.dispatchMessage(message);
        }
        this.initVideoTrack();
    }

    /**
     * 加载视频
     */
    private void initVideoTrack() {
        // 设置预览
        final SurfaceViewRenderer surfaceViewRenderer = new SurfaceViewRenderer(this.context);
        surfaceViewRenderer.setMirror(true);
        surfaceViewRenderer.setEnableHardwareScaler(true);
        // 加载视频
        final EglBase eglBase = EglBase.create();
        final SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("MediaVideoThread", eglBase.getEglBaseContext());
        final VideoSource videoSource = this.peerConnectionFactory.createVideoSource(this.videoCapturer.isScreencast());
        this.videoCapturer.initialize(surfaceTextureHelper, this.context, videoSource.getCapturerObserver());
        this.videoCapturer.startCapture(640, 480, 30);
        final VideoTrack videoTrack = this.peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.setEnabled(true);
        this.mediaStream.addTrack(videoTrack);
        Log.i(MediaManager.class.getSimpleName(), "加载视频：" + videoTrack.id());
    }

    public void pauseAudio() {
        synchronized (this.mediaStream.audioTracks) {
            this.mediaStream.audioTracks.forEach(a -> a.setEnabled(false));
        }
    }

    public void resumeAudio() {
        synchronized (this.mediaStream.audioTracks) {
            this.mediaStream.audioTracks.forEach(a -> a.setEnabled(true));
        }
    }

    public void pauseVideo() {
        synchronized (this.mediaStream.videoTracks) {
            this.mediaStream.videoTracks.forEach(v -> v.setEnabled(false));
        }
        synchronized (this.mediaStream.preservedVideoTracks) {
            this.mediaStream.preservedVideoTracks.forEach(v -> v.setEnabled(false));
        }
    }

    public void resumeVideo() {
        synchronized (this.mediaStream.videoTracks) {
            this.mediaStream.videoTracks.forEach(v -> v.setEnabled(true));
        }
        synchronized (this.mediaStream.preservedVideoTracks) {
            this.mediaStream.preservedVideoTracks.forEach(v -> v.setEnabled(true));
        }
    }

    /**
     * 关闭声音
     */
    private void closeAudioTrack() {
        synchronized (this.mediaStream.audioTracks) {
            AudioTrack track;
            final Iterator<AudioTrack> iterator = this.mediaStream.audioTracks.iterator();
            while(iterator.hasNext()) {
                track = iterator.next();
                iterator.remove();
                track.dispose();
            }
        }
    }

    /**
     * 关闭视频
     */
    private void closeVideoTrack() {
        // 次码流
        this.closeVideoTrack(this.mediaStream.videoTracks);
        // 主码流
        this.closeVideoTrack(this.mediaStream.preservedVideoTracks);
    }

    /**
     * 关闭视频
     *
     * @param list 视频列表
     */
    private void closeVideoTrack(List<VideoTrack> list) {
        synchronized (list) {
            VideoTrack track;
            final Iterator<VideoTrack> iterator = list.iterator();
            while(iterator.hasNext()) {
                track = iterator.next();
                iterator.remove();
                track.dispose();
            }
        }
    }

    /**
     * 释放资源
     */
    public void close() {
        if(this.videoCapturer != null) {
            this.videoCapturer.dispose();
            this.videoCapturer = null;
        }
        if(this.mediaStream != null) {
            this.mediaStream.dispose();
            this.mediaStream = null;
        }
        if(this.peerConnectionFactory != null) {
            this.peerConnectionFactory.dispose();
            this.peerConnectionFactory = null;
        }
    }

    /**
     * 摄像头事件
     *
     * @author acgist
     */
    private static class MediaCameraEventsHandler implements CameraVideoCapturer.CameraEventsHandler {

        @Override
        public void onCameraError(String message) {
        }

        @Override
        public void onCameraDisconnected() {
        }

        @Override
        public void onCameraFreezed(String message) {
        }

        @Override
        public void onCameraOpening(String message) {
        }

        @Override
        public void onFirstFrameAvailable() {
        }

        @Override
        public void onCameraClosed() {
        }
    }

}
