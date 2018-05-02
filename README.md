# EmojiBuddies

Very Important :
The first time that an app using face API is installed on a device, GMS will download a native library to 
the device in order to do detection.  Usually this completes before the app is run for the first time.
But if that download has not yet completed, then the app will not detect any faces , and will output a toast
saying that no faces were detected although the photo may contain faces , so before running the app on a real
device for the first time , make sure that the device is connected to the interent .

Although the device was connected to the internet , the face detection still didn't work ?
1- Check for low storage , If there is low storage , the native library will not be downloaded,
so detection will not become operational.
2- Try clearing cache and data from download manager and google play services, check the following link to do so :
https://support.google.com/googleplay/answer/7512202?hl=en
