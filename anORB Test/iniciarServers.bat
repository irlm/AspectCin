@echo off
start C:\Temp\gkmo\jdk1.5.0\bin\java -cp calculadora.jar;anOrb.jar calculadora.SomaServer %1
start C:\Temp\gkmo\jdk1.5.0\bin\java -cp calculadora.jar;anOrb.jar calculadora.SubtracaoServer %1
start C:\Temp\gkmo\jdk1.5.0\bin\java -cp calculadora.jar;anOrb.jar calculadora.MultiplicacaoServer %1
start C:\Temp\gkmo\jdk1.5.0\bin\java -cp calculadora.jar;anOrb.jar calculadora.DivisaoServer %1