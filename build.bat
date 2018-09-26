if not exist %cd%\bin (
	mkdir %cd%\bin
) else (
	del %cd%\bin\*
)
javac -cp .\src -d .\bin .\src\addchain\*.java
jar cvfe .\add-chain-converter.jar addchain.AddChainMain -C .\bin\ .