main class is com.khoosty.Runner

input files :

resources/div_info.csv - table given in the pdf in csv format
resources/trades.csv - trades in csv format, this is my assumption as to how the input is given

As Volume Weighted Stock price is restricted to last 15 mins, timestamps in trades.csv should be updated OR
comment/uncomment lines com/khoosty/Runner.java:45 to hardcode the current time.

There is a (very) simple junit test to check the math in CalcUtilsTest.java