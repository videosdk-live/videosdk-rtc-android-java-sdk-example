# Video SDK for Android

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started)
[![Firebase](https://img.shields.io/badge/Download%20Android-Firebase-green)](https://appdistribution.firebase.google.com/pub/i/0f3ac650239a944b)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/bGZtAbwvab)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs

## Demo App
Check out demo [here](https://videosdk.live/prebuilt/)

📱 Download the Sample Android app here: https://appdistribution.firebase.google.com/pub/i/0f3ac650239a944b

## Features

- [x] Video conferencing with real-time video and audio
- [x] Enable/disable camera
- [x] Mute/unmute mic
- [x] Switch between front and back camera
- [x] Change Audio Device
- [x] Screen share
- [x] Chat
- [x] [External call detection](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/external-call-detection)

## App Behavior with Different Room Types

**One-to-One** - The One-to-One Call room type allows 2 participants to join a room in the app.

## Structure of Project

#### Create or join Meeting

- *NetworkUtils.java* - `NetworkUtils.java` class is used to call the api for create and join the meeting.
- *CreateOrJoinActivity.java* 
  - `CreateOrJoinActivity.java` activity is used to ask permissions to the partcipant,and to initiate webcam and mic status.
  - `CreateOrJoinFragment`,`CreateMeetingFragment`,`JoinMeetingFragment` will be bound to this activity.
- *CreateOrJoinFragment.java* - `CreateOrJoinFragment.java` fragment will include
  - `Create Meeting Button` - This button will navigate to `CreateMeetingFragment`.
  - `Join Meeting Button` - This button will navigate to `JoinMeetingFragment`.
  - `CreateOrJoinFragment` would be like this :
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/img_CreateOrJoinFragment.jpg"/>
  </p>

- *CreateMeetingFragment.java* - `CreateMeetingFragment.java` fragement will include
  - `EditText for ParticipantName` - This edit text will contain name of the participant.
  - `Create Meeting Button` - This button will call api for create a new meeting and navigate to `OneToOneCallActivity`.
  - `CreateMeetingFragment` would be look like this :
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/img_CreateMeetingFragment.jpg"/>
  </p>
- *JoinMeetingFragment.java* - `JoinMeetingFragment.java` fragement will include
  - `EditText for ParticipantName` - This edit text will contain name of the participant.
  - `EditText for MeetingId` - This edit text will contain the meeting Id that you want to join..
  - `Join Meeting Button` - This button will call api for join meeting with meetingId that you provided and navigate to `OneToOneCallActivity`.
  - `JoinMeetingFragment` would be look like this : 
  <p align="center">
  <img width="230" height="450" src="https://www.linkpicture.com/q/img_JoinMeetingFragment.jpg"/>
  </p>
#### PartcipantList

- `ParticipantListAdapter.java`,`layout_participants_list_view.xml` and `item_participant_list_layout.xml` files used to show ParticipantList.
- Call `openParticipantList()` method to show PartcipantList.
- `PartcipantList` dialog would be look like this : 
  <p align="center">
  <img width="250" height="450" src="https://www.linkpicture.com/q/img_participantList.jpg"/>
  </p>

#### Dialogs

- **MoreOptions**:
  - `MoreOptionsListAdapter.java` class,`ListItem.java` class and `more_options_list_layout.xml` files used to show `MoreOptions` dialog.
  - Call `showMoreOptionsDialog()` method to show `MoreOptions` dialog.
  - `MoreOptions` dialog would be look like this : 
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_MoreOptionList.jpg"/>
  </p>
- **AudioDeviceList**:
  - `AudioDeviceListAdapter.java` class,`ListItem.java` class and `audio_device_list_layout.xml` files used to show `AudioDeviceList` dialog.
  - Call `showAudioInputDialog()` method to show `AudioDeviceList` dialog.
  - `AudioDeviceList` would be look like this :
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_AudioDeviceList.jpg"/>
  </p>
- **LeaveOrEndDialog**:
  - `LeaveOptionListAdapter.java` class,`ListItem.java` class and `leave_options_list_layout.xml` files used to show `LeaveOrEndDialog`.
  - Call `showLeaveOrEndDialog()` method to show `LeaveOrEndDialog`.
  - `LeaveOrEndDialog` would be look like this :
  <p align="center">
  <img width="350" height="250" src="https://www.linkpicture.com/q/img_LeaveorEndDialog.jpg"/>
  </p>
#### One-to-One Call Room

- `OneToOneCallActivity.java` activity is main activity for One-to-One call.

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
