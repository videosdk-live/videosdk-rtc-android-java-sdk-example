# Video SDK Android(Java) Code Sample

## What is it?

This code sample demonstrates a one-to-one and group video call application built with [Video SDK RTC Android SDK](https://docs.videosdk.live/docs/realtime-communication/sdk-reference/android-sdk/setup)

- Built for serverless video calling experience in Android.
- Scale it upto 5,000 participants with low code.
- 10,000 minutes free on monthly basis

![video-sdk-mobile.jpg](https://static.zujonow.com/github/video-sdk-mobile.jpg)

## Features

- [x] Video API with real-time audio, video and data streams
- [x] 5,000+ participants support
- [ ] Chat support with rich media.
- [ ] Screen sharing with HD and Full HD.
- [ ] Play realtime video in meeting
- [ ] Connect it with social media such as Facebook, Youtube etc (RTMP out support).
- [x] Intelligent speaker switch
- [x] Record your meetings on cloud
- [x] Customise UI and build other rich features with our new data streams such as whiteboard, poll, Q & A etc.

## Device support

Visit our official [documentation](https://docs.videosdk.live/docs/realtime-communication/sdk-reference/android-sdk/setup) for more information

## Prerequisites

You must have the following installed:

- Android Studio
- Android SDK
- Emulator or physical android device

## Running the Authentication server

Before running app, you need to run the authentication server

Use our official [videosdk-rtc-nodejs-sdk-example](https://github.com/videosdk-live/videosdk-rtc-nodejs-sdk-example) to perform server authentication.

```sh
$ git clone https://github.com/videosdk-live/videosdk-rtc-nodejs-sdk-example
```

Add API Key and Secret in `.env` of your project.

```sh
VIDEOSDK_API_KEY=''
VIDEOSDK_SECRET_KEY=''
VIDEOSDK_API_ENDPOINT=https://api.zujonow.com
```

Visit, [https://www.videosdk.live/](https://www.videosdk.live/) to generate API keys and secret.

## Authentication server url in app

Set the authentication server base url in `local.properties` file in the android project.

```
api_server_url='http://192.168.0.101:9000'
```

Related

- [Video SDK RTC Prebuillt No Code App](https://github.com/videosdk-live/videosdk-rtc-js-prebuilt-embedded-example)
- [Video SDK RTC React App](https://github.com/videosdk-live/videosdk-rtc-react-sdk-example)
- [Video SDK RTC Node JS App](https://github.com/videosdk-live/videosdk-rtc-nodejs-sdk-example)
