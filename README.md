![Java CI with Gradle](https://github.com/a-kraschitzer/intellij-time-tracker/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)

### Issues
#### Timestamps
The following date formats are not supported and will be parsed as follows:
* `MM/dd/yyyy` will be parsed as `dd/MM/yyyy`
* `yy-MM-dd` will be parsed as `dd-MM-yyyy`
* `MM/dd/yy` and `yy/MM/dd` will be parsed as `dd/MM/yy`
