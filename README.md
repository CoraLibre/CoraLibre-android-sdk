CoraLibre-android-sdk
======================

This code is based on the [DP-3T prestandard android-sdk code](https://github.com/DP-3T/dp3t-sdk-android/tree/prestandard).
It aims to be compatible with the Apple/Google developed [Privacy-Preserving Contact Tracing](https://www.apple.com/covid19/contacttracing/) (PPCP) protocoll.
So far it is behaving more like DP-3T but we aim to transform it to be PPCP compatible. (We are makeing progress, the new CryptoModule seems to work right already.)

## Todo

This repository is based on prestandard DP-3T. With this project we have the aim to create a [Privacy-Preserving Contact Tracing](https://www.apple.com/covid19/contacttracing/) compatible sdk in order to create a libre alternative to the part of the GMS services that conducts the bluetooth tracing.

The discussion about tasks that need to be done can be found in [this](https://github.com/corona-warn-app/cwa-app-android/issues/75) thread.
This sdk should become part of the CoraLibre app which should become compatible with the German [Corona-Warn-App](https://github.com/corona-warn-app/cwa-app-android).

## Documentation

The Bluetoth specification as well es the cryptography specification is documented here:
https://www.apple.com/covid19/contacttracing/

The Android API is documented here:
https://www.google.com/covid19/exposurenotifications/

Old DP-3T is documented here:
https://github.com/DP-3T/documents/blob/master/DP3T%20White%20Paper.pdf

## How to get Started

So far everything is developed by Unit Testing. Before you can start make sure you know android and be able to handle working with git(hub). 
Here are a few steps to "boot" you up into the project:

1. First read the documents that are listed above in the [documentation](#documentation) section.
2. Clone this repository and read/run the unit tests. It will give you a good starting point about understanding the functionality of the sdk.
3. When you have a good overview about the sdk take a look at the [kanban board](https://github.com/theScrabi/CoraLibre-android-sdk/projects/1) to see what is left to do or who is working on what already. If you have questions about each task read the corresponding issue or feel free to ask questions in there.

## License
This project is licensed under the terms of the MPL 2 license. See the [LICENSE](LICENSE) file.
