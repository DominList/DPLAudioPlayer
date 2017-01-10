# DPLAudioPlayer

What's new? : 
-Seekbar handled by Handler in Runnable thread
-buttons next / prev / forward / backward
-time counter
-screen locker
-improvements

Current version has the base ability to show music files from android music directory (works fine with Android 4.0-4.3).
Files are synchornized with ListView by Adapter.
Application is prepared to use Android Service for MediaPlayer actions in the future.
It joins simplicity and nice appearance.

Warning:
This version will crash if you don't have music in your /sdcard/music directory on your device.
