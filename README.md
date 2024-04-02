# Order files after photorec run

When your hard drive makes issues or a partition is not detected anymore [ddrescue](https://www.gnu.org/software/ddrescue/) in combination with [testdisk](https://www.cgsecurity.org/wiki/TestDisk_DE) is a beautiful tool to recover your lost files.

When all hope is lost then photorec, which is a part of testdisk, can at least recover the files even though the partitioning is broken but all names and folder sturcture is lost.
To at least sort the recovered data a little bit this small application can help.

Use at your own risk!

## How to use it

* Checkout repository
* Open your terminal in the root folder of this repository and execute the following
```bash
# windows
gradlew.bat run --args="[SOURCE_FOLDER] [TARGET_FOLDER]"
# linux
./gradlew run --args="[SOURCE_FOLDER] [TARGET_FOLDER]"
```

## What will it do
* It will sort by file type.
* It will sort `jpg` images by name original creation date (in the meta data) and otherwise uses the last modified date of the file.
* It will rename all `mp3` files to start with artist and song name.
* It will try to recover the creation date of `mp4` files and otherwise use the last modified date.