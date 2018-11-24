@echo off
if not exist build\ngoy (
	echo Extracting ngoy binaries...
	call gradle extractNgoy
)
java -cp build\ngoy\* ngoy.Ngoy %*