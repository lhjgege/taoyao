#pragma once

#include <map>
#include <string>
#include <iostream>

#include "jni.h"
#include "Log.hpp"
#include "LocalClient.hpp"
#include "RemoteClient.hpp"
#include "mediasoupclient.hpp"

#include "sdk/android/src/jni/pc/peer_connection.h"
#include "sdk/android/native_api/jni/scoped_java_ref.h"

namespace acgist {

    class Room {
    public:
        mediasoupclient::Device* device;
        mediasoupclient::PeerConnection* peerConnection;
        acgist::LocalClient* localClient;
        std::map<std::string, acgist::RemoteClient*> remoteClients;
        jstring roomId;
    public:
        /**
         * 新建Transport回调
         */
        jmethodID newCallback;
        /**
         * 房间关闭回调
         */
        jmethodID closeCallback;
    public:
        Room(jstring roomId);
        virtual ~Room();
    public:
        void load(std::string rtpCapabilities, webrtc::PeerConnectionFactoryInterface* factory, webrtc::PeerConnectionInterface::RTCConfiguration& configuration);
        void closeLocalClient();
        void closeRemoteClient();
        void close();
    };

}