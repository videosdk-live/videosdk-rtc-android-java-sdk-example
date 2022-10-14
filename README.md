<p align="center">
<img width="400" src="https://www.linkpicture.com/q/videosdk_Full-Logo_blue.png"/>
</p>

---

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started)
[![Firebase](https://img.shields.io/badge/Download%20Android-Firebase-green)](https://appdistribution.firebase.google.com/pub/i/0f3ac650239a944b)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/bGZtAbwvab)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs

## Demo App

📱 Download the Sample Android app here: https://appdistribution.firebase.google.com/pub/i/0f3ac650239a944b

## Features

- [x] Real-time Video and Audio Conferencing
- [x] Enable/disable camera
- [x] Mute/unmute mic
- [x] Switch between front and back camera
- [x] Change Audio Device
- [x] Screen share
- [x] Chat
- [x] Raise Hand
- [x] [External call detection](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/external-call-detection)
- [x] Recording

## Setup Guide

- Sign up on https://app.videosdk.live/ and visit [API Keys](https://app.videosdk.live/api-keys) section to Get your API key and Secret key.

- Get familiarized with API key and Secret key [here](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/signup-and-create-api)

- Get familiarized with Token [here](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/server-setup)

## Steps to Integrate
### Prerequisites
- Development environment requirements:
  - [Java Development Kit](https://www.oracle.com/java/technologies/downloads/)
  - Android Studio 3.0 or later
- A physical or virtual mobile device running Android 5.0 or later
- Valid [Video SDK Account](https://app.videosdk.live/)

## Run the Sample Project
### Step 1: Clone the sample project
Clone the repository to your local environment.
```js
git clone https://github.com/videosdk-live/videosdk-rtc-android-java-sdk-example.git
```

### Step 2: Modify local.properties
Generate temporary token from [Video SDK Account](https://app.videosdk.live/signup).
```js title="local.properties"
auth_token= "TEMPORARY-TOKEN"
```

### Step 3: Run the sample app
Run the android app with **Shift+F10** or the ** ▶ Run ** from toolbar.

## Key Concepts

- `Meeting` -  Meeting represents meetingId where sessions will be taken.
- `Sessions` -  A particular duration you spend in a given meeting is a referred as session, you can have multiple session of a particular meetingId.
- `Participant` -  Participant represents someone who is attending the meeting's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `Steam` -  Stream means video or audio media content that is either published by `local participant` or `remote participants`.

## Token Generation

Token is used to create and validate a meeting, as well as initializing a meeting.

- `Development Environment`: For development, you can use temporary token. Visit VideoSDK [dashboard](https://app.videosdk.live/api-keys) to generate temporary token.
- `Production Environment`: For production, you have to set up an authentication server to authorize users. Follow our official example repositories to setup authentication server, [videosdk-rtc-api-server-examples](https://github.com/videosdk-live/videosdk-rtc-api-server-examples)


## API: Create and Validate meeting

- `create meeting` - Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/create-room) to create meeting.
- `validate meeting`- Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/validate-room) to validate the meetingId.

## [Initializing a Meeting](https://docs.videosdk.live/android/api/sdk-reference/initMeeting)

- To initialize the meeting, first you have to initialize the `VideoSDK`. You can initialize the `VideoSDK` using `initialize()` method.
```js
  VideoSDK.initialize(Context context)
```

- Now, you have to set token property of `VideoSDK` class. By using `config()` method, you can set the token property of `VideoSDK` class.
```js
  VideoSDK.config(String token)
```

- Now, you can initialize the meeting using `initMeeting()` method. `initMeeting()` will generate a new `Meeting` class and the initiated meeting will be returned.
```js
  Meeting meeting = VideoSDK.initMeeting(
                       Context context,
                       String meetingId,
                       String name,
                       boolean micEnabled,
                       boolean webcamEnabled,
                       String participantId)
```

## [Mute/Unmute Local Audio](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls)

```js
// unmute mic
meeting.unmuteMic();

// mute mic
meeting.muteMic();
```

## [Change Audio Device](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls#2-change-audio-device)

- By using `meeting.getMics()` function, a participant can get all the connected mics.
```js
 // get connected mics
 Set<AppRTCAudioManager.AudioDevice> mics = meeting.getMics();
```

- Local participant can change the audio device using `changeMic(AppRTCAudioManager.AudioDevice device)` method of `meeting` class. Please refer this [documention](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls#select-audio-device) to know about which parameter you can pass to `changeMic()` method.
```js
// change mic
 meeting.changeMic(AppRTCAudioManager.AudioDevice device);
```

- When a Local participant changes the Mic, `AppRTCAudioManager.AudioManagerEvents()` is triggered which can be set to `Meeting` class by using `meeting.setAudioDeviceChangeListener()`. [Example](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls#select-audio-device)

## [Enable/Disable Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```js
// enable webcam
meeting.enableWebcam();

// disable webcam
meeting.disableWebcam();
```

## [Switch Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```js
// switch webcam
meeting.changeWebcam();
```

## [Chat](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/pubsub)

- Chat feature allows the participant to send and receive messages of the topics which he has subscribed.

```js
// publish
meeting.pubSub.publish(String topic,String message, PubSubPublishOptions pubSubPublishoptions);

// pubSubPublishoptions is an object of PubSubPublishOptions, which provides an option, such as persist, which persists message history for upcoming participants.


//subscribe
List<PubSubMessage> pubSubMessageList = meeting.pubSub.subscribe(String topic, PubSubMessageListener pubSubMessageListener)


//unsubscribe
meeting.pubSub.unsubscribe(topic, PubSubMessageListener pubSubMessageListener);


// receiving messages
// PubSubMessageListener will be invoked with onMessageReceived(PubSubMessage message)
PubSubMessageListener pubSubMessageListener = new PubSubMessageListener() {
    @Override
    public void onMessageReceived(PubSubMessage message) {
        Log.d("#message", "onMessageReceived: " + message.getMessage());
    }
};
```

## [Leave or End Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/leave-end-meeting)

```js
// to leave the meeting
meeting.leave();

// to end the meeting
meeting.end();
```

## [Setup MeetingEventListener](https://docs.videosdk.live/android/api/sdk-reference/meeting-class/meeting-event-listener-class)

VideoSDK provides callbacks to the client app about any change or update happening in the meeting after a user has joined by implementing `MeetingEventListener`.

```js
  MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingJoined() {
           // This event will be emitted when a localParticipant successfully joined the meeting.
        }

        @Override
        public void onMeetingLeft() {
           // This event will be emitted when a localParticipant left the meeting.
        }
        
        @Override
        public void onParticipantJoined(Participant participant) {
           // This event will be emitted when a new participant joined the meeting.
           // [participant]: new participant who joined the meeting
        }
        
        @Override
        public void onParticipantLeft(Participant participant) {
           // This event will be emitted when a joined participant left the meeting.
           // [participant]: participant who left the meeting
        }

        @Override
        public void onPresenterChanged(String participantId) {
           // This event will be emitted when any participant starts or stops screen sharing.
           // [participantId]: Id of participant who shares the screen.
        }
        
        @Override
        public void onSpeakerChanged(String participantId) {
           // This event will be emitted when a active speaker changed.
           // [participantId] : Id of active speaker 
        }

        @Override
        public void onRecordingStarted() {
           // This event will be emitted when recording of the meeting is started.
        }

        @Override
        public void onRecordingStopped() {
           // This event will be emitted when recording of the meeting is stopped.
        }

        @Override
        public void onExternalCallStarted() {
           // This event will be emitted when local particpant receive incoming call.
        }

        @Override
        public void onMeetingStateChanged(String state) {
           // This event will be emitted when state of meeting changes.
        }
    };
```

## [Setup ParticipantEventListener](https://docs.videosdk.live/android/api/sdk-reference/participant-class/participant-event-listener-class)

VideoSDK provides callbacks to the client app whenever any participant's video, audio or screen share stream is enabled/disabled by implementing `ParticipantEventListener`.

```js
  ParticipantEventListener participantEventListener = new ParticipantEventListener() {
       @Override
       public void onStreamEnabled(Stream stream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
       }

       @Override
       public void onStreamDisabled(Stream stream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
       }
   };

```



📖 Read the Complete Documentation here: <https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started>

## Project Description

> **Note**
> - ***master*** branch: Better UI with One-to-One and Group call experience.
> - ***v1-code-sample*** branch: Simple UI with Group call experience.

### App Behaviour with Different Meeting Types

- **One-to-One meeting** - The One-to-One meeting allows 2 participants to join a meeting in the app.
- **Group meeting** - The Group meeting allows 2 or more participants to join a meeting in the app.

### Project Structure

- We have 3 packages
  - `OneToOneCall` - OneToOneCall package includes all classes/files related to OneToOne meeting.
  - `GroupCall` - GroupCall package includes all classes/files related to Group meeting.
  - `Common` - Common package inclues all the classes/files that are used in both meeting type.

### Common package

#### Create or join Meeting

- *NetworkUtils.java* - `NetworkUtils.java` class is used to call the api to generate token,create and validate the meeting.
- *CreateOrJoinActivity.java*
  - `CreateOrJoinActivity.java` activity is used to ask permissions to the partcipant,and to initiate webcam and mic status.
  - `CreateOrJoinFragment`,`CreateMeetingFragment`,`JoinMeetingFragment` will be bound to this activity.
- *CreateOrJoinFragment.java* - `CreateOrJoinFragment.java` fragment will include
  - `Create Meeting Button` - This button will navigate to `CreateMeetingFragment`.
  - `Join Meeting Button` - This button will navigate to `JoinMeetingFragment`.
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/img_CreateOrJoinFragment.jpg"/>
  </p>

- *CreateMeetingFragment.java* - `CreateMeetingFragment.java` fragement will include
  - `Dropdown to select meeting type` - This dropdown will give choice for meeting type.
  - `EditText for ParticipantName` - This edit text will contain name of the participant.
  - `Create Meeting Button` - This button will call api for create a new meeting and navigate to `OneToOneCallActivity` or `GroupCallActivity` according to user choice.
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/Create-meeting.gif"/>
  </p>
- *JoinMeetingFragment.java* - `JoinMeetingFragment.java` fragement will include
  - `Dropdown to select meeting type` - This dropdown will give choice for meeting type.
  - `EditText for ParticipantName` - This edit text will contain name of the participant.
  - `EditText for MeetingId` - This edit text will contain the meeting Id that you want to join.
  - `Join Meeting Button` - This button will call api for join meeting with meetingId that you provided and navigate to `OneToOneCallActivity` or `GroupCallActivity` according to user choice.
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/img_JoinMeetingFragment.jpg"/>
  </p>
#### PartcipantList

- `ParticipantListAdapter.java`,`layout_participants_list_view.xml` and `item_participant_list_layout.xml` files used to show ParticipantList.
  <p align="center">
  <img width="250" height="450" src="https://www.linkpicture.com/q/img_participantList.jpg"/>
  </p>

#### Dialogs

- **MoreOptions**:
  - `MoreOptionsListAdapter.java` class,`ListItem.java` class and `more_options_list_layout.xml` files used to show `MoreOptions` dialog.
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_MoreOptionList.jpg"/>
  </p>
- **AudioDeviceList**:
  - `AudioDeviceListAdapter.java` class,`ListItem.java` class and `audio_device_list_layout.xml` files used to show `AudioDeviceList` dialog.
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_AudioDeviceList.jpg"/>
  </p>
- **LeaveOrEndDialog**:
  - `LeaveOptionListAdapter.java` class,`ListItem.java` class and `leave_options_list_layout.xml` files used to show `LeaveOrEndDialog`.
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_LeaveorEndDialog.jpg"/>
  </p>
### OneToOneCall package

- `OneToOneCallActivity.java` activity is main activity for One-to-One meeting.

### GroupCall package

- `GroupCallActivity.java` activity is main activity for Group meeting.
- `ParticipantViewFragment.java` and `ParticipantViewAdapter.java` is used to show participants in Grid.

## Examples
- [Prebuilt SDK Examples](https://github.com/videosdk-live/videosdk-rtc-prebuilt-examples)
- [JavaScript SDK Example](https://github.com/videosdk-live/videosdk-rtc-javascript-sdk-example)
- [React JS SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-sdk-example)
- [React Native SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-native-sdk-example)
- [Flutter SDK Example](https://github.com/videosdk-live/videosdk-rtc-flutter-sdk-example)
- [Android SDK Example](https://github.com/videosdk-live/videosdk-rtc-android-java-sdk-example)
- [iOS SDK Example](https://github.com/videosdk-live/videosdk-rtc-ios-sdk-example)

## Documentation
[Read the documentation](https://docs.videosdk.live/) to start using Video SDK.

## Community
- [Discord](https://discord.gg/Gpmj6eCq5u) - To get involved with the Video SDK community, ask questions and share tips.
- [Twitter](https://twitter.com/video_sdk) - To receive updates, announcements, blog posts, and general Video SDK tips.
